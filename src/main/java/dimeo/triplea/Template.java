/*******************************************************************************
 * Copyright (c) 2017 Elder Research, Inc.
 * All rights reserved.
 *******************************************************************************/
package dimeo.triplea;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.util.IOUtils;

import com.beust.jcommander.Parameter;
import com.elderresearch.commons.lang.CLIUtils;
import com.elderresearch.commons.lang.Utilities;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Template {
	@Parameter(names = "-t", description = "The template name")
	private String templateName;
	
	@Parameter(description = "Colon-separated pairs of values to plug in to the template e.g. \"key:value\"")
	private List<String> pairs = new ArrayList<>();
	
	public static void main(String... args) {
		val t = new Template();
		if (CLIUtils.parseArgs(args, t)) { System.out.println(t.apply()); }
	}
	
	public String apply() {
		val keyVals = new HashMap<String, List<String>>();
		
		for (val pair : pairs) {
			val arr = StringUtils.split(pair, ':');
			keyVals.computeIfAbsent(arr[0].toLowerCase(), k -> new ArrayList<>()).add(arr[1]);
		}
		
		val lists = new ArrayList<Pair<String, List<String>>>();
		keyVals.forEach((k, list) -> lists.add(Pair.of(k, list)));
		
		val sb = new StringBuilder();
		try (val is = Utilities.getResourceOrFile(getClass(), "/templates/" + templateName)) {
			val s = new String(IOUtils.toByteArray(is));
			
			recurse(0, lists, new LinkedList<>(), vals -> {
				String applied = s;
				for (Pair<String, String> pair : vals) {
					applied = applied.replace("${" + pair.getLeft() + "}", pair.getRight());
				}
				sb.append(applied).append(System.lineSeparator());
			});
		
		} catch (IOException e) {
			log.warn("Error reading template", e);
		}
		return sb.toString();
	}
	
	private static void recurse(int i, List<Pair<String, List<String>>> list, List<Pair<String, String>> vals, Consumer<List<Pair<String, String>>> callback) {
		if (i >= list.size()) {
			callback.accept(vals);
			return;
		}

		for (String s : list.get(i).getRight()) {
			recurse(i + 1, list, Utilities.concat(vals, Collections.singletonList(Pair.of(list.get(i).getLeft(), s))), callback);
		}
	}
}
