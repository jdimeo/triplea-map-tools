/*******************************************************************************
 * Copyright (c) 2017 Elder Research, Inc.
 * All rights reserved.
 *******************************************************************************/
package dimeo.triplea;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.datamininglab.commons.lang.LambdaUtils;
import com.datamininglab.commons.lang.Utilities;

import generated.Game;
import generated.Player;
import generated.Territory;
import generated.Unit;
import lombok.val;

public class Territories implements WorkbookUtils {
	private static final String SHEET = "Territories";
	private static final String COL_TERRITORY = "territory", COL_PROD = "production", COL_SEA = "water", COL_OWNER = "owner";
	private static final String ATTACH_CLASS = "games.strategy.triplea.attachments.TerritoryAttachment";
	
	private Game game;
	
	public Territories(Game game) {
		this.game = game;
	}
	
	public void readFrom(Workbook wb) {
		readFrom(wb.getSheet(SHEET));
	}
	
	private void readFrom(Sheet ws) {
		if (ws == null) { return; }
		
		
	}
	
	public void saveTo(Workbook wb) {
		saveTo(getOrCreate(wb, SHEET));
	}
	
	private void saveTo(Sheet ws) {
		Map<String, TerritoryData> territories = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);	
		game.getMap().getGridOrTerritory().forEach(obj -> {
			Territory t = Utilities.cast(obj);
			territories.put(t.getName(), new TerritoryData(t.getName(), BooleanUtils.toBoolean(t.getWater())));
		});
		
		game.getInitialize().getUnitInitialize().getUnitPlacement().forEach(unitInit -> {
			Unit u = Utilities.cast(unitInit.getUnitType());
			territories.get(unitInit.getTerritory()).initUnits.put(u.getName(), NumberUtils.toInt(unitInit.getQuantity()));
		});
		
		game.getInitialize().getOwnerInitialize().getTerritoryOwner().forEach(owner -> {
			Player p = Utilities.cast(owner.getOwner());
			territories.get(owner.getTerritory()).owner = p.getName();
		});
		
		game.getAttachmentList().getAttachment().forEach(attach -> {
			if (attach.getJavaClass().equals(ATTACH_CLASS)) {
				attach.getOption().forEach(opt -> {
					if (opt.getName().equals(COL_PROD)) {
						territories.get(attach.getAttachTo()).production = NumberUtils.toInt(opt.getValue());		
					}
				});
			}
		});
		
		List<String> cols = new LinkedList<>(Arrays.asList(COL_TERRITORY, COL_SEA, COL_OWNER, COL_PROD));
		game.getUnitList().getUnit().forEach(unit -> cols.add(unit.getName()));
		
		Map<String, Map<String, String>> table = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		territories.forEach((key, t) -> table.put(key, t.toMap()));
		writeTable(ws, table, cols);
	}
	
	private static class TerritoryData {
		String name, owner;
		boolean isSea;
		Map<String, Integer> initUnits = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		Integer production;
		
		TerritoryData(String name, boolean isSea) {
			this.name = name;
			this.isSea = isSea;
		}
		
		Map<String, String> toMap() {
			val ret = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
			ret.put(COL_TERRITORY, name);
			LambdaUtils.apply(production, p -> ret.put(COL_PROD, p.toString()));
			ret.put(COL_SEA, Boolean.toString(isSea));
			LambdaUtils.apply(owner, o -> ret.put(COL_OWNER, o));
			initUnits.forEach((unit, count) -> ret.put(unit, count.toString()));
			return ret;
		}
	}
}
