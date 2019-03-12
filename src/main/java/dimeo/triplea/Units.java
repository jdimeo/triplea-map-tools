/*******************************************************************************
 * Copyright (c) 2017 Elder Research, Inc.
 * All rights reserved.
 *******************************************************************************/
package dimeo.triplea;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.elderresearch.commons.lang.Utilities;
import com.google.common.collect.ImmutableSet;

import generated.Attachment;
import generated.Cost;
import generated.Game;
import generated.Option;
import generated.ProductionRule;
import generated.Resource;
import generated.Result;
import generated.Unit;
import lombok.val;

public class Units implements WorkbookUtils {
	private static final String SHEET = "Units";
	private static final String COL_COST = "cost", COL_COST_RESOURCE = "costResource", COL_UNIT = "unit";
	private static final Set<String> SKIP_COLS = ImmutableSet.of(COL_COST, COL_COST_RESOURCE, COL_UNIT);
	
	private static final String ATTACH_CLASS = "games.strategy.triplea.attachments.UnitAttachment";
	private static final DecimalFormat FMT = new DecimalFormat("#");
	
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
		
		val unitAttachIdx = new MutableInt(), lastUnitAttachIdx = new MutableInt();
		game.getAttachmentList().getAttachment().forEach(attach -> {
			unitAttachIdx.increment();
			if (attach.getJavaClass().equals(ATTACH_CLASS)) {
				lastUnitAttachIdx.setValue(unitAttachIdx.intValue());
				
				val map = units.remove(attach.getAttachTo());
				if (map == null) { return; }
				
				// Update existing options
				attach.getOption().forEach(opt -> {
					val optVal = map.remove(opt.getName());
					if (optVal != null) { opt.setValue(toOptionValue(optVal)); }
				});
				// Add new options from workbook
				map.entrySet().forEach(e -> {
					if (!SKIP_COLS.contains(e.getKey())) { attach.getOption().add(toOption(e.getKey(), e.getValue())); }		
				});
			}
		});
		
		// Add new attachments for remaining units
		units.forEach((name, options) -> {
			game.getUnitList().getUnit().add(Unit.builder().withName(name).build());
			
			val attach = Attachment.builder()
				.withJavaClass(ATTACH_CLASS)
				.withAttachTo(name)
				.withName("unitAttachment")
				.withType("unitType");
			options.forEach((k, v) -> {
				if (!SKIP_COLS.contains(k)) { attach.addOption(toOption(k, v)); }	
			});
			game.getAttachmentList().getAttachment().add(lastUnitAttachIdx.getAndIncrement(), attach.build());
			
			val prod = ProductionRule.builder()
				.withName("buy" + name)
				.withCost(Cost.builder()
					.withQuantity(toOptionValue(options.get(COL_COST)))
					.withResource(Resource.builder()
						.withName(toOptionValue(options.get(COL_COST_RESOURCE)))
						.build())
					.build())
				.withResult(Result.builder()
					.withQuantity("1")
					.withResourceOrUnit(Unit.builder()
						.withName(toOptionValue(name))
						.build())
					.build());
			game.getProduction().getProductionRule().add(prod.build());
		});
	}
	
	private static String toOptionValue(Object val) {
		return val instanceof Number? FMT.format(val) : val.toString();
	}
	
	private static Option toOption(String key, Object val) {
		val ret = new Option();
		ret.setName(key);
		ret.setValue(toOptionValue(val));
		return ret;
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
