package games.strategy.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name="cp-notes",
	description = "Copy game notes into our out of the game file to a separate HTML file for easier editing")
public class CopyNotes implements Callable<Void> {
	@Option(names = {"-i", "--import"}, description = "By default, this will extract the notes to an HTML file. If this "
			+ "is true, the HTML file will be copied back into the game file (to test in the actual game)")
	private boolean importNotes;
	
	@Option(names = {"-g", "--gameFile"}, description = "The original game XML file", required = true)
	private Path gameFile;
	
	@Option(names = {"-n", "--notesFile"}, description = "The temporary HTML file to use to edit and preview the notes", required = false)
	private Path notesFile;
	
	@Override
	public Void call() throws IOException {
		if (notesFile == null) {
			notesFile = gameFile.getParent().getParent().resolve("doc").resolve("notes.html");
		}
		
		String s = Files.readString(gameFile);
		int i = s.indexOf("<![CDATA[") + 9;
		int j = s.indexOf("]]>", i);

		if (importNotes) {
			val html = Files.readString(notesFile);
			Files.writeString(gameFile, s.substring(0, i) + html + s.substring(j));
		} else {
			Files.writeString(notesFile, s.substring(i, j));
		}
		return null;
	}
}
