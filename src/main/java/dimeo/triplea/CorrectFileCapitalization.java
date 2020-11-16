package dimeo.triplea;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import lombok.val;

public class CorrectFileCapitalization {
	public static void main(String[] args) throws IOException {
		val root = Paths.get(args[0]);
		Files.walk(root).forEach(f -> {
			if (Files.isDirectory(f)) { return; }
			
			String orig = f.toString();
			int i = orig.lastIndexOf(File.separatorChar);
			String fixed = orig.substring(0, i + 1) + WordUtils.capitalizeFully(orig.substring(i + 1), '-', ' ').replace("Aa", "AA");
			if (!StringUtils.equals(orig, fixed)) {
				System.out.println(orig + " -> " + fixed);
				try {
					Path tmp = Paths.get(orig.substring(0, i + 1) + "windows-is-dumb-sometimes.png");
					Files.move(f, tmp);
					Files.move(tmp, Paths.get(fixed));
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}
			
		});
	}
}
