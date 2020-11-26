package dimeo.triplea;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.poi.util.IOUtils;
import org.jooq.lambda.Seq;

import com.elderresearch.commons.lang.Utilities;

import lombok.NoArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Log4j2
@Command(name = "template", description = {
	"Plugin a matrix of values into a template",
	"This allows automating the repetitive parts of the game file (for example, the same production frontier for several players)."
})
@NoArgsConstructor
public class Template implements Callable<Void> {
	@Option(names = "-t", description = "The template name")
	private String templateName;
	
	@Option(names = "-out", description = "The output file")
	private Path outputFile = Paths.get("out.xml");

	@Parameters(description = "Colon-separated pairs of values to plug in to the template e.g. \"key:value\"")
	private List<String> pairs = new LinkedList<>();
	
	public Template(String templateResource, String... pairs) {
		templateName = "/templates/" + templateResource;
		Seq.of(pairs).forEach(this.pairs::add);
	}
	
	@Override
	public Void call() throws IOException {
		Files.write(outputFile, apply(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		return null;
	}
	
	private List<String> apply() {
		if (pairs.isEmpty()) {
			throw new IllegalArgumentException("Must specify at least one pair of values to template");
		}
		
		val keyVals = new LinkedHashMap<String, List<String>>();
		for (val pair : pairs) {
			val arr = StringUtils.split(pair, ':');
			keyVals.computeIfAbsent(arr[0], k -> new LinkedList<>()).add(arr[1]);
		}
		
		val ret = new LinkedList<String>();
		try (val is = Utilities.getResourceOrFile(getClass(), templateName)) {
			val s = new String(IOUtils.toByteArray(is));
			recurse(s, keyVals, ret);
		} catch (IOException e) {
			log.warn("Error reading template", e);
		}
		return ret;
	}
	
	private void recurse(String template, Map<String, ?> keyVals, List<String> lines) {
		val copy = new LinkedHashMap<String, Object>(keyVals);
		for (val e : keyVals.entrySet()) {
			if (e.getValue() instanceof List<?>) {
				List<?> list = Utilities.cast(e.getValue());
				list.forEach(o -> {
					copy.put(e.getKey(), o);
					recurse(template, copy, lines);
				});
				return;
			}
		}
		
		val strSub = new StringSubstitutor(copy);
		strSub.setEnableSubstitutionInVariables(true);
		lines.add(strSub.replace(template));
	}
}
