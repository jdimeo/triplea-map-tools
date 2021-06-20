package jdimeo.triplea;

import java.awt.Point;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import org.jooq.lambda.Seq;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
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
	@Parameters(description = "The folder containing centers.txt, polygons.txt and the place to write place.txt")
	private Path mapFolder;
	
	@Option(names = {"-d", "--diameter"}, description = "The diameter of the unit placement circles")
	private int diameter = 50;
	
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
		
		log.info("Computing placements...");
		territories.forEach(t -> {
			val geo = t.getGeo();
			val env = geo.getEnvelopeInternal();
			
			val placements = new LinkedList<Polygon>();
			for (double x = env.getMinX() + 2; x <= env.getMaxX(); x += diameter) {
				for (double y = env.getMinY() + 2; y <= env.getMaxY(); y += diameter) {
					factory.setBase(new Coordinate(x, y));
					val placement = factory.createRectangle();
					
					for (int n = 0; n < geo.getNumGeometries(); n++) {
						if (geo.getGeometryN(n).contains(placement)) {
							placements.add(placement);
							
							t.getPlacements().add(new Point(
								(int) Math.round(x),
								(int) Math.round(y)));
							break;
						}	
					}
				}
			}
		});
		
		log.info("Writing placements...");
		Files.write(mapFolder.resolve("place.txt"), Seq.seq(territories)
			.filter($ -> !$.getPlacements().isEmpty()).map(TerritoryGeo::asPlaceString));
		
		log.info("Done.");
		return null;
	}
}
