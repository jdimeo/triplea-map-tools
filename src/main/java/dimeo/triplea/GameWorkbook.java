/*******************************************************************************
 * Copyright (c) 2017 Elder Research, Inc.
 * All rights reserved.
 *******************************************************************************/
package dimeo.triplea;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.datamininglab.commons.logging.LogContext;

import generated.Game;
import lombok.val;

public class GameWorkbook {
	private Game game;
	
	public GameWorkbook(Game game) {
		this.game = game;
	}
	
	public void sync(File f) {
		val units = new Units(game);
		val territories = new Territories(game);
		
		Workbook wb;
		if (f.exists()) {
			try (FileInputStream fis = new FileInputStream(f)) {
				 wb = WorkbookFactory.create(fis);
			} catch (InvalidFormatException | IOException e) {
				LogContext.warning(e, "Error reading XLSX");
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
			LogContext.warning(e, "Error writing XLSX");
		}
	}
}
