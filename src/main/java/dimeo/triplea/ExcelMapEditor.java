package dimeo.triplea;

import java.io.File;
import java.io.IOException;

import com.beust.jcommander.Parameter;
import com.datamininglab.commons.lang.JCommanderUtils;

import lombok.val;

public class ExcelMapEditor {
	@Parameter(names = {"-g", "--gameFile"}, description = "The original game XML file", required = true)
	private String gameFile;
	
	@Parameter(names = {"-u", "--unitFile"}, description = "The XLSX file with the new unit attributes in tabular form. If this doesn't exit, one will be saved from the game file.", required = true)
	private String unitFile;
	
	public void run() throws IOException {
		File gf = new File(gameFile);
		File uf = new File(unitFile);
		
		val codec = new GameCodec();
		val game  = codec.load(gf);
		new GameWorkbook(game).sync(uf);
		codec.save(game, gf);
	}
	
	public static void main(String[] args) throws IOException {
		ExcelMapEditor uc = new ExcelMapEditor();
		if (JCommanderUtils.parseArgs(args, uc)) { uc.run(); }
	}
}
