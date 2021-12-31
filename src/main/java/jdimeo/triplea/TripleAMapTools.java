package jdimeo.triplea;

import jdimeo.triplea.excel.ExcelMapEditor;
import jdimeo.triplea.tiles.TileJoin;
import jdimeo.triplea.tiles.TileQuantize;
import jdimeo.triplea.tiles.TileSplit;
import jdimeo.triplea.util.CopyNotes;
import jdimeo.triplea.util.FixFileNames;
import jdimeo.triplea.util.OrderLines;
import jdimeo.triplea.util.TerritoryImage;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

@Command(name = "triplea-tools", subcommands = {
	HelpCommand.class,
	ExcelMapEditor.class,
	CopyNotes.class,
	Template.class,
	ConnectionFinder.class,
	PlacementPicker.class,
	TerritoryImage.class,
	RailroadHelper.class,
	VariableLists.class,
	FixFileNames.class,
	OrderLines.class,
	TileJoin.class,
	TileQuantize.class,
	TileSplit.class,
})
public class TripleAMapTools {
	public static void main(String... args) {
		System.exit(new CommandLine(new TripleAMapTools()).execute(args));
	}
}
