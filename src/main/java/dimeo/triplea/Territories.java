/*******************************************************************************
 * Copyright (c) 2017 Elder Research, Inc.
 * All rights reserved.
 *******************************************************************************/
package dimeo.triplea;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.elderresearch.commons.lang.LambdaUtils;
import com.elderresearch.commons.lang.Utilities;

import generated.Attachment;
import generated.Game;
import generated.Option;
import generated.Player;
import generated.Territory;
import generated.TerritoryOwner;
import generated.Unit;
import generated.UnitPlacement;
import lombok.val;

public class Territories implements WorkbookUtils {
	private static final String SHEET = "Territories";
	private static final String COL_TERRITORY = "territory", COL_PROD = "production", COL_SEA = "water", COL_OWNER = "owner";
	private static final String ATTACH_CLASS = "games.strategy.triplea.attachments.TerritoryAttachment";
	
	private SyncLog log;
	private Game game;
	
	public Territories(SyncLog log, Game game) {
		this.log  = log;
		this.game = game;
	}
	
	public void readFrom(Workbook wb) {
		readFrom(wb.getSheet(SHEET));
	}
	
	private void readFrom(Sheet ws) {
		if (ws == null) { return; }
		
		val table = readTable(ws);
		val territories = new HashMap<String, TerritoryData>();
		
		table.forEach((territory, map) -> {
			TerritoryData td = new TerritoryData(asString(map.remove(COL_TERRITORY)), asBoolean(map.remove(COL_SEA)));
			td.production = asInteger(map.remove(COL_PROD));
			td.owner = asString(map.remove(COL_OWNER));
			map.forEach((unit, count) -> LambdaUtils.accept(count, c -> td.initUnits.put(unit, asInteger(c))));
			territories.put(territory, td);
		});
		
		game.getMap().getGridOrTerritory().forEach(obj -> {
			Territory t = Utilities.cast(obj);
			TerritoryData td = territories.get(t.getName());
			if (td != null) {
				val isSea = String.valueOf(td.isSea);
				if (!StringUtils.equalsAnyIgnoreCase(isSea, t.getWater())) {
					log.logChange(t.getName(), "water", t.getWater(), isSea);
					t.setWater(isSea);
				}
			}
		});
		
		game.getInitialize().getOwnerInitialize().getTerritoryOwner().forEach(owner -> {
			TerritoryData td = territories.get(owner.getTerritory());
			if (td == null || StringUtils.isBlank(td.owner)) { return; }
			
			val newp = asPlayer(td.owner);
			Player oldp = Utilities.cast(owner.getOwner());
			if (!StringUtils.equalsIgnoreCase(newp.getName(), oldp.getName())) {
				log.logChange(owner.getTerritory(), "owner", oldp.getName(), newp.getName());
				owner.setOwner(newp);
			}
			td.step = 1;
		});
		territories.values().forEach(td -> {
			if (td.step < 1 && !StringUtils.isBlank(td.owner)) {
				log.logSet(td.name, "owner", td.owner);
				game.getInitialize().getOwnerInitialize().getTerritoryOwner().add(
					TerritoryOwner.builder().withOwner(asPlayer(td.owner)).withTerritory(td.name).build());
			}
			td.step = 1;
		});
		
		game.getAttachmentList().getAttachment().forEach(attach -> {
			if (attach.getJavaClass().equals(ATTACH_CLASS)) {
				TerritoryData td = territories.get(attach.getAttachTo());
				if (td == null || td.production == null) { return; }
				
				attach.getOption().forEach(opt -> {
					val p = td.production.toString();
					if (opt.getName().equals(COL_PROD)) {
						if (!StringUtils.equalsIgnoreCase(opt.getValue(), p)) {
							log.logChange(td.name, "production", opt.getValue(), p);
							opt.setValue(p);
						}
						td.step = 2;
					}
				});
			}
		});
		territories.values().forEach(td -> {
			if (td.step < 2 && td.production != null) {
				log.logSet(td.name, "production", td.production);
				game.getAttachmentList().getAttachment().add(Attachment.builder()
					.withAttachTo(td.name)
					.withJavaClass(ATTACH_CLASS)
					.withType(COL_TERRITORY)
					.withName("territoryAttachment")
					.addOption(Option.builder()
						.withName(COL_PROD)
						.withValue(td.production.toString())
						.build())
					.build());
			}
			td.step = 2;
		});
		
		game.getInitialize().getUnitInitialize().getUnitPlacement().forEach(unitInit -> {
			TerritoryData td = territories.get(unitInit.getTerritory());
			if (td == null) {
				log.warn("Can't set unit quantity for unknown territory {}", unitInit.getTerritory());
			} else {
				Unit u = Utilities.cast(unitInit.getUnitType());
				int oldq = NumberUtils.toInt(unitInit.getQuantity()); 
				int newq = ObjectUtils.defaultIfNull(td.initUnits.remove(u.getName()), 0);
				if (newq != oldq) {
					log.logChange(td.name, u.getName(), oldq, newq);
					unitInit.setQuantity(String.valueOf(newq));
				}
				
				Player oldp = Utilities.cast(unitInit.getOwner());
				if (!StringUtils.equalsIgnoreCase(td.owner, LambdaUtils.apply(oldp, Player::getName))) {
					log.logChange(td.name, u.getName() + " owner", oldp.getName(), td.owner);
					unitInit.setOwner(asPlayer(td.owner));
				}
			}
		});
		territories.values().forEach(td -> {
			td.initUnits.forEach((unit, qty) -> {
				if (qty > 0) {
					log.logSet(td.name, unit, qty);
					game.getInitialize().getUnitInitialize().getUnitPlacement().add(UnitPlacement.builder()
						.withUnitType(asUnit(unit))
						.withOwner(asPlayer(td.owner))
						.withQuantity(qty.toString())
						.withTerritory(td.name)
						.build());
				}
			});
		});
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
			LambdaUtils.accept(territories.get(unitInit.getTerritory()),
				t -> t.initUnits.put(u.getName(), NumberUtils.toInt(unitInit.getQuantity())));
		});
		
		game.getInitialize().getOwnerInitialize().getTerritoryOwner().forEach(owner -> {
			Player p = Utilities.cast(owner.getOwner());
			if (p != null) { LambdaUtils.accept(territories.get(owner.getTerritory()), t -> t.owner = p.getName()); }
		});
		
		game.getAttachmentList().getAttachment().forEach(attach -> {
			if (attach.getJavaClass().equals(ATTACH_CLASS)) {
				attach.getOption().forEach(opt -> {
					if (opt.getName().equals(COL_PROD)) {
						LambdaUtils.accept(territories.get(attach.getAttachTo()),
							t -> t.production = NumberUtils.toInt(opt.getValue()));		
					}
				});
			}
		});
		
		val cols = new LinkedList<>(Arrays.asList(COL_TERRITORY, COL_SEA, COL_OWNER, COL_PROD));
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
		
		int step;
		
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
