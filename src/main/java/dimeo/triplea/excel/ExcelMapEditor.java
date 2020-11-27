package dimeo.triplea.excel;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import games.strategy.util.GameCodec;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "excel", description = "Synchronizes the game file with an Excel spreadsheet to edit the unit and territory attachments and initial placements")
public class ExcelMapEditor implements Callable<Void> {
	@Option(names = {"-g", "--gameFile"}, description = "The original game XML file", required = true)
	private String gameFile;
	
	@Option(names = {"-u", "--unitFile"}, description = "The XLSX file with the new unit attributes in tabular form. If this doesn't exit, one will be saved from the game file.", required = true)
	private String unitFile;
	
	@Override
	public Void call() throws IOException {
		File gf = new File(gameFile);
		File uf = new File(unitFile);
		
		val codec = new GameCodec();
		val game  = codec.load(gf);
		new GameWorkbook(game).sync(uf);
		codec.save(game, gf);
		return null;
	}
}
