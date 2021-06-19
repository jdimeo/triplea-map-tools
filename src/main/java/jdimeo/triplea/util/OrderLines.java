package jdimeo.triplea.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import lombok.val;
import lombok.extern.log4j.Log4j2;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Log4j2
@Command(name="order-lines",
	description = "Order lines alphabetically in a file (like centers.txt or polygons.txt)")
public class OrderLines implements Callable<Void> {
	@Parameters(description = "The files to sort")
	private List<Path> paths;
	
	@Override
	public Void call() throws Exception {
		for (val p : paths) {
			log.info("Ordering {}...", p);
			val lines = Files.readAllLines(p);
			lines.sort(String.CASE_INSENSITIVE_ORDER);
			Files.write(p, lines);
		}
		return null;
	}
}
