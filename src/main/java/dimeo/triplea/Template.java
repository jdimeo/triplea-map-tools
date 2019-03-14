/*******************************************************************************
 * Copyright (c) 2017 Elder Research, Inc.
 * All rights reserved.
 *******************************************************************************/
package dimeo.triplea;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.text.StrSubstitutor;
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
	private List<String> pairs = new LinkedList<>();
	
	public static void main(String... args) throws IOException {
		val t = new Template();
		if (CLIUtils.parseArgs(args, t)) {
			Files.write(Paths.get("out.xml"), t.apply(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		}
	}
	
	public List<String> apply() {
		if (pairs.isEmpty()) {
			throw new IllegalArgumentException("Must specify at least one pair of values to template");
		}
		
		val keyVals = new LinkedHashMap<String, List<String>>();
		for (val pair : pairs) {
			val arr = StringUtils.split(pair, ':');
			keyVals.computeIfAbsent(arr[0], k -> new LinkedList<>()).add(arr[1]);
		}
		
		val ret = new LinkedList<String>();
		try (val is = Utilities.getResourceOrFile(getClass(), "/templates/" + templateName)) {
			val s = new String(IOUtils.toByteArray(is));
			recurse(s, keyVals, ret);
		} catch (IOException e) {
			log.warn("Error reading template", e);
		}
		return ret;
	}
	
	@SuppressWarnings("deprecation")
	private void recurse(String template, Map<String, ?> keyVals, List<String> lines) {
		val copy = new LinkedHashMap<String, Object>(keyVals);
		val anyLists = new MutableBoolean();
		keyVals.forEach((k, v) -> {
			if (v instanceof List<?>) {
				List<?> list = Utilities.cast(v);
				list.forEach(o -> {
					copy.put(k, o);
					recurse(template, copy, lines);
				});
				anyLists.setTrue();
			}
		});
		if (anyLists.isFalse()) {
			val strSub = new StrSubstitutor(copy);
			strSub.setEnableSubstitutionInVariables(true);
			lines.add(strSub.replace(template));
		}
	}
}
