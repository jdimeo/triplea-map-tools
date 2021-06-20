package jdimeo.triplea;

import java.awt.Point;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jooq.lambda.Seq;
import org.locationtech.jts.geom.Coordinate;
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
	private static final double CIRCLE_PACK_DENSITY_HEX = 0.908699;
	
	@Parameters(description = "The folder containing centers.txt, polygons.txt and the place to write place.txt")
	private Path mapFolder;
	
	@Option(names = {"-d", "--diameter"}, description = "The diameter of the unit placement circles")
	private int diameter = 60;
	
	@Override
	public Void call() throws Exception {
		log.info("Loading polygons...");
		val territories = TerritoryGeo.fromPolysFile(mapFolder.resolve("polygons.txt"));

		log.info("Loading centers...");
		try (val is = Files.newInputStream(mapFolder.resolve("centers.txt"))) {
			val centers = PointFileReaderWriter.readOneToOneCenters(is);
			territories.forEach($ -> $.setCenter(centers.get($.getName())));
		}

		val factory = new GeometricShapeFactory(TerritoryGeo.GEO_FACTORY);
		factory.setSize(diameter);
		
		val ystep  = diameter * CIRCLE_PACK_DENSITY_HEX;
		val radius = diameter * 0.5;
		
		log.info("Computing placements...");
		territories.forEach(t -> {
			val geo        = t.getGeo();
			val env        = geo.getEnvelopeInternal();
			val center     = geo.getCentroid();
			val placements = t.getPlacements();
			
			for (double x = env.getMinX() + (center.getX() % diameter); x <= env.getMaxX(); x += diameter) {
				int row = 0;
				for (double y = env.getMinY() + (center.getY() % diameter); y <= env.getMaxY(); y += ystep) {
					double xoffset = x + (row % 2) * 0.5 * diameter;
					factory.setBase(new Coordinate(xoffset, y));
					val placement = factory.createCircle();
					
					// Make sure the placement isn't contained by any *other* territory, which
					// can happen if one territory is fully contained within another
					if (contains(geo, placement) && Seq.seq(territories).noneMatch($ -> overlaps($.getGeo(), placement))) {
						t.getPlacements().add(round(xoffset, y));
					}
					row++;
				}
			}
			if (placements.isEmpty()) {
				placements.add(round(center.getX() - radius, center.getY() - radius));
			}
			
			// Put placement closest to center first
			t.getPlacements().sort(Comparator.comparing($ -> center.distance(
				TerritoryGeo.GEO_FACTORY.createPoint(new Coordinate($.x, $.y)))));
		});
		
		log.info("Writing placements...");
		Files.write(mapFolder.resolve("place.txt"), Seq.seq(territories).map(TerritoryGeo::asPlaceString));
		
		log.info("Done.");
		return null;
	}
	
	private static Point round(double x, double y) {
		return new Point((int) Math.round(x),	(int) Math.round(y));
	}
	
	private static boolean contains(Geometry collection, Geometry g) {
		return test(collection, g, Geometry::covers);
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
