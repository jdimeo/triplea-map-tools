package dimeo.triplea;

import dimeo.triplea.excel.ExcelMapEditor;
import dimeo.triplea.tiles.TileJoin;
import dimeo.triplea.tiles.TileQuantize;
import dimeo.triplea.tiles.TileSplit;
import games.strategy.util.CopyNotes;
import games.strategy.util.FixFileNames;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

@Command(name = "triplea-tools", subcommands = {
	HelpCommand.class,
	ExcelMapEditor.class,
	CopyNotes.class,
	Template.class,
	ConnectionFinder.class,
	RailroadHelper.class,
	VariableLists.class,
	FixFileNames.class,
	TileJoin.class,
	TileQuantize.class,
	TileSplit.class,
})
public class TripleAMapTools {
	public static void main(String... args) {
		System.exit(new CommandLine(new TripleAMapTools()).execute(args));
	}
}
