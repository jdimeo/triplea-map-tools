package jdimeo.triplea;

import java.awt.Point;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;

import org.jooq.lambda.Seq;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.util.GeometricShapeFactory;

import jdimeo.triplea.util.PointFileReaderWriter;
import jdimeo.triplea.util.TerritoryGeo;
import lombok.val;
import lombok.extern.log4j.Log4j2;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Log4j2
@Command(name = "placements", description = {
	"Automatically create placements from polygons.txt",
	"This differs from existing tools because it packs circles, not squares, since often the corners of unit images can overlap without causing visual problems."
})
public class PlacementPicker implements Callable<Void> {
	private static final double CIRCLE_PACK_DENSITY_HEX = 0.91;
	private static final int MAX_TO_REMOVE_IF_MANY = 1;
	
	@Parameters(description = "The folder containing centers.txt, polygons.txt and the place to write place.txt")
	private Path mapFolder;
	
	@Option(names = {"-d", "--diameter"}, description = "The diameter of the unit placement circles")
	private int diameter = 56;
	
	@Option(names = {"-s", "--step-size"}, description = "The step size of the offset from (0, 0) to try to maximize placements per territory (up to the radius)")
	private int step = 4;
	
	@Option(names = {"-p", "--preferred-per-territory"}, description = {
		"The preferred number of placements per territory.",
		"If there are more than this number of available placements (because the territory is large) one placement will be removed closest to the center to free space for country owernship markers, production values, etc."		
	})
	private int preferred = 5;
	
	private List<TerritoryGeo> territories;
	private GeometricShapeFactory factory;
	
	@Override
	public Void call() throws Exception {
		log.info("Loading polygons...");
		territories = TerritoryGeo.fromPolysFile(mapFolder.resolve("polygons.txt"));

		log.info("Loading centers...");
		try (val is = Files.newInputStream(mapFolder.resolve("centers.txt"))) {
			val centers = PointFileReaderWriter.readOneToOneCenters(is);
			territories.forEach($ -> $.setCenter(centers.get($.getName())));
		}

		factory = new GeometricShapeFactory(TerritoryGeo.GEO_FACTORY);
		factory.setSize(diameter);
		
		val radius = diameter * 0.5;
		
		log.info("Computing placements...");
		territories.parallelStream().forEach(t -> {
			val geo    = t.getGeo();
			val env    = geo.getEnvelopeInternal();
			val center = TerritoryGeo.GEO_FACTORY.createPoint(new Coordinate(t.getCenter().x - radius, t.getCenter().y - radius));
			
			// Try a some starting offsets to see which yields the most placements
			// Start at 2 so placements don't overlap/touch borders
			int max = 0;
			for (double x = 2; x <= radius; x += step) {
				for (double y = 2; y <= radius; y += step) {
					val placements = findPlacements(t, env, x, y);
					if (placements.size() > max) {
						max = placements.size();
						t.setPlacements(placements);
					}
				}
			}
			if (max == 0) {
				t.getPlacements().add(round(center.getX(), center.getY()));
			}
			
			// Put placement closest to center first
			t.getPlacements().sort(Comparator.comparing($ -> center.distance(
				TerritoryGeo.GEO_FACTORY.createPoint(new Coordinate($.x, $.y)))));
			
			// Remove closest to center first if we have a lot, which frees up space around other things
			// anchored to the center (like production values or country markers)
			for (int removed = 0; t.getPlacements().size() > preferred && removed < MAX_TO_REMOVE_IF_MANY; removed++) {
				t.getPlacements().remove(0);
			}
		});
		
		log.info("Writing placements...");
		Files.write(mapFolder.resolve("place.txt"), Seq.seq(territories).map(TerritoryGeo::asPlaceString));
		
		log.info("Done.");
		return null;
	}
	
	private List<Point> findPlacements(TerritoryGeo t, Envelope env, double startX, double startY) {
		val ystep  = diameter * CIRCLE_PACK_DENSITY_HEX;
		
		val ret = new ArrayList<Point>();
		for (double x = env.getMinX() + startX; x <= env.getMaxX(); x += diameter) {
			int row = 0;
			for (double y = env.getMinY() + startY; y <= env.getMaxY(); y += ystep) {
				double xoffset = x + (row % 2) * 0.5 * diameter;
				factory.setBase(new Coordinate(xoffset, y));
				val placement = factory.createCircle();
				
				// Make sure the placement isn't non-trivially overlapping *another* territory,
				// which can happen if one territory is fully contained within another
				if (contains(t.getGeo(), placement) && Seq.seq(territories).filter($ -> $ != t).noneMatch($ -> overlaps($.getGeo(), placement))) {
					ret.add(round(xoffset, y));
				}
				row++;
			}
		}
		return ret;
	}
	
	private static Point round(double x, double y) {
		return new Point((int) Math.round(x),	(int) Math.round(y));
	}
	
	private static boolean contains(Geometry collection, Geometry g) {
		return test(collection, g, Geometry::contains);
	}
	private static boolean overlaps(Geometry collection, Geometry g) {
		return test(collection, g, Geometry::overlaps);
	}
	
	private static boolean test(Geometry collection, Geometry g, BiFunction<Geometry, Geometry, Boolean> test) {
		for (int n = 0; n < collection.getNumGeometries(); n++) {
			if (test.apply(collection.getGeometryN(n), g)) { return true; }
		}
		return false;
	}
}
