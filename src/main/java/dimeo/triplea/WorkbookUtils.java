/*******************************************************************************
 * Copyright (c) 2017 Elder Research, Inc.
 * All rights reserved.
 *******************************************************************************/
package dimeo.triplea;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.elderresearch.commons.lang.NumberExtensions;
import com.elderresearch.commons.lang.Utilities;

import generated.Player;
import generated.Unit;

public interface WorkbookUtils {
	default Sheet getOrCreate(Workbook wb, String name) {
		return getOrCreate(wb, Workbook::getSheet, Workbook::createSheet, name);
	}
	default Row getOrCreate(Sheet sheet, int i) {
		return getOrCreate(sheet, Sheet::getRow, Sheet::createRow, i);
	}
	default Cell getOrCreate(Row row, int i) {
		return getOrCreate(row, Row::getCell, Row::createCell, i);
	}
	default <T, I, O> O getOrCreate(T parent, BiFunction<T, I, O> getter, BiFunction<T, I, O> creator, I in) {
		return Optional.ofNullable(getter.apply(parent, in)).orElseGet(() -> creator.apply(parent, in));
	}
	
	default void putOrAppend(Map<String, String> map, String key, String val) {
		String existing = map.get(key);
		if (existing == null) {
			map.put(key, val);
		} else {
			map.put(key, String.format("%s|%s", existing, val));
		}
	}
	
	default String[] removeAndSplit(Map<String, String> map, String key) {
		return ArrayUtils.nullToEmpty(StringUtils.split(map.remove(key), '|'));
	}
	
	default Map<String, Map<String, Object>> readTable(Sheet ws) {
		Map<String, Map<String, Object>> ret = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		
		int fr = ws.getFirstRowNum();
		int lr = ws.getLastRowNum();
		
		// Assume first row is headers
		Row row = ws.getRow(fr);
		int fc = row.getFirstCellNum();
		int lc = row.getLastCellNum();
		
		List<String> cols = new ArrayList<>();
		for (int c = fc; c < lc; c++) {
			cols.add(StringUtils.stripToNull(row.getCell(c).getStringCellValue()));
		}
		
		for (int r = fr + 1; r <= lr; r++) {
			row = ws.getRow(r);
			if (row == null) { break; }
			
			fc = row.getFirstCellNum();
			lc = row.getLastCellNum();
			
			Map<String, Object> map = new LinkedHashMap<>();
			ret.put(StringUtils.stripToNull(row.getCell(fc).getStringCellValue()), map);
			for (int c = fc; c < lc; c++) {
				Cell cell = row.getCell(c);
				if (cell != null) {
					switch (cell.getCellType()) {
						case Cell.CELL_TYPE_NUMERIC:
							map.put(cols.get(c), cell.getNumericCellValue());
							break;
						case Cell.CELL_TYPE_BOOLEAN:
							map.put(cols.get(c), cell.getBooleanCellValue());
							break;
						case Cell.CELL_TYPE_STRING: default:
							map.put(cols.get(c), StringUtils.stripToNull(cell.getStringCellValue()));
							break;
					}
				}
			}
		}
		return ret;
	}
	
	default String asString(Object o) {
		return Objects.toString(o, null);
	}
	default boolean asBoolean(Object o) {
		return BooleanUtils.toBoolean(asString(o));
	}
	default Integer asInteger(Object o) {
		if (o == null) { return null; }
		if (o instanceof Number) { return NumberExtensions.asInteger(Utilities.cast(o)); }
		return NumberUtils.toInt(asString(o));
	}
	
	default Player asPlayer(String name) {
		return name == null? null : Player.builder().withName(name).build();
	}
	default Unit asUnit(String name) {
		return name == null? null : Unit.builder().withName(name).build();
	}
		
	default void writeTable(Sheet ws, Map<String, Map<String, String>> table) {
		Set<String> cols = new LinkedHashSet<>();
		table.values().forEach(map -> cols.addAll(map.keySet()));
		writeTable(ws, table, cols);
	}
	
	default void writeTable(Sheet ws, Map<String, Map<String, String>> table, Collection<String> cols) {
		int r = 0, c = 0;
		Row row = getOrCreate(ws, r++);
		for (String col : cols) { getOrCreate(row, c++).setCellValue(col); }
		
		for (Map<String, String> map : table.values()) {
			row = getOrCreate(ws, r++);
			
			c = 0;
			for (String col : cols) {
				String strVal = map.get(col);
				double numVal = NumberUtils.toDouble(strVal, Double.NaN);
				Cell cell = getOrCreate(row, c++);
				if (Double.isNaN(numVal)) {
					cell.setCellValue(strVal);
				} else {
					cell.setCellValue(numVal);
				}
			}
		}
	}
}
