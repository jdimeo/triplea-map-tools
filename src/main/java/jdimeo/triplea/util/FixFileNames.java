package jdimeo.triplea.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import lombok.extern.log4j.Log4j2;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Log4j2
@Command(name = "fix-file-names", description = "Fix capitilzation of files in a folder, even on Windows")
public class FixFileNames implements Callable<Void> {
	@Parameters(index = "0", arity = "1", description = "The directory of files to fix")
	private Path input;
	
	@Override
	public Void call() throws IOException {
		log.info("Fixing file names in {}...", input);
		Files.walk(input).forEach(f -> {
			if (!Files.isRegularFile(f)) { return; }
			
			String orig = f.toString();
			int i = orig.lastIndexOf(File.separatorChar);
			String fixed = orig.substring(0, i + 1) + WordUtils.capitalizeFully(orig.substring(i + 1), '-', ' ').replace("Aa", "AA");
			if (!StringUtils.equals(orig, fixed)) {
				log.info("Renamed {} to {}", orig, fixed);
				try {
					Path tmp = Paths.get(orig.substring(0, i + 1) + "temp");
					Files.move(f, tmp);
					Files.move(tmp, Paths.get(fixed));
				} catch (IOException e) {
					log.warn("Error renaming file", e);
				}	
			}
		});
		log.info("Done.");
		return null;
	}
}
