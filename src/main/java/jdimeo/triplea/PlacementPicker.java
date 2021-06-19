package jdimeo.triplea;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import jdimeo.triplea.util.TerritoryGeo;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

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
		val territories = TerritoryGeo.fromPolysFile(mapFolder.resolve("polygons.txt"));

		
		return null;
	}
}
