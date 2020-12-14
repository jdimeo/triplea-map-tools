package jdimeo.triplea.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import generated.Game;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GameWorkbook {
	private Game game;
	
	public GameWorkbook(Game game) {
		this.game = game;
	}
	
	public void sync(File f) {
		val units = new Units(game);
		val territories = new Territories(SyncLog.DEFAULT, game);
		
		Workbook wb;
		if (f.exists()) {
			try (FileInputStream fis = new FileInputStream(f)) {
				 wb = WorkbookFactory.create(fis);
			} catch (IOException e) {
				log.warn("Error reading XLSX", e);
				return;
			}
			
			units.readFrom(wb);
			territories.readFrom(wb);
		} else {
			wb = new XSSFWorkbook();
		}
		
		units.saveTo(wb);
		territories.saveTo(wb);
		
		try (FileOutputStream fos = new FileOutputStream(f)) {
			wb.write(fos);
		} catch (IOException e) {
			log.warn("Error writing XLSX", e);
		}
	}
}
