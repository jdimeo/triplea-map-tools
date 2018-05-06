/*******************************************************************************
 * Copyright (c) 2017 Elder Research, Inc.
 * All rights reserved.
 *******************************************************************************/
package dimeo.triplea;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.elderresearch.commons.lang.Utilities;

import generated.Cost;
import generated.Game;
import generated.Resource;
import generated.Result;
import generated.Unit;

public class Units implements WorkbookUtils {
	private static final String SHEET = "Units";
	private static final String COL_COST = "cost", COL_COST_RESOURCE = "costResource", COL_UNIT = "unit";
	private static final String ATTACH_CLASS = "games.strategy.triplea.attachments.UnitAttachment";
	
	private Game game;
	
	public Units(Game game) {
		this.game = game;
	}
	
	public void readFrom(Workbook wb) {
		readFrom(wb.getSheet(SHEET));
	}
	
	private void readFrom(Sheet ws) {
		if (ws == null) { return; }
		
		Map<String, Map<String, Object>> units = readTable(ws);
		
		game.getAttachmentList().getAttachment().forEach(attach -> {
			/*if (attach.getJavaClass().equals(ATTACH_CLASS)) {
				Map<String, String> map = units.remove(attach.getAttachTo());
				if (map == null) { return; }
				
				
				attach.getOption().forEach(opt -> {
					String[] vals = getAndSplit(map, opt.getName());
				});
				map.entrySet().forEach(e -> {
					if (e.getKey().equals(COL_UNIT)) { return; }
					
					for (String val : )
					
				});
				
				map.put(COL_UNIT, attach.getAttachTo());
				attach.getOption().forEach(opt -> putOrAppend(map, opt.getName(), opt.getValue()));
				units.put(attach.getAttachTo(), map);
			}*/
		});
		
	}
	
	public void saveTo(Workbook wb) {
		saveTo(getOrCreate(wb, SHEET));
	}	
		
	private void saveTo(Sheet ws) {
		Map<String, Map<String, String>> units = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		
		game.getAttachmentList().getAttachment().forEach(attach -> {
			if (attach.getJavaClass().equals(ATTACH_CLASS)) {
				Map<String, String> map = new LinkedHashMap<>();
				map.put(COL_UNIT, attach.getAttachTo());
				attach.getOption().forEach(opt -> putOrAppend(map, opt.getName(), opt.getValue()));
				units.put(attach.getAttachTo(), map);
			}
		});
		
		game.getProduction().getProductionRule().forEach(prod -> {
			Result res = Utilities.first(prod.getResult());
			Cost cost = Utilities.first(prod.getCost());
			if (res.getResourceOrUnit() instanceof Unit) {
				Unit unit = Utilities.cast(res.getResourceOrUnit());
				Map<String, String> map = units.get(unit.getName());
				if (map != null) {
					map.put(COL_COST, cost.getQuantity());
					Resource resource = Utilities.cast(cost.getResource());
					map.put(COL_COST_RESOURCE, resource.getName());
				}
			}
		});

		writeTable(ws, units);
	}
}
