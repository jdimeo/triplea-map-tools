package jdimeo.triplea;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import com.google.common.collect.Lists;

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
		
		log.info("Computing placements...");
		territories.parallelStream().forEach(t -> {
			
		});
		
		log.info("Writing placements...");
		Files.write(mapFolder.resolve("place.txt"), Lists.transform(territories, TerritoryGeo::asPlaceString));
		
		log.info("Done.");
		return null;
	}
}
