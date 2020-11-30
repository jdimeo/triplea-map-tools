package games.strategy.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.css.NormalOutput;
import org.fit.cssbox.io.DefaultDOMSource;
import org.fit.cssbox.io.StreamDocumentSource;
import org.xml.sax.SAXException;

import com.google.common.net.MediaType;

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
	public Void call() throws IOException, SAXException {
		if (notesFile == null) {
			notesFile = gameFile.getParent().getParent().resolve("doc").resolve("notes.html");
		}
		
		String s = Files.readString(gameFile);
		int i = s.indexOf("<![CDATA[") + 9;
		int j = s.indexOf("]]>", i);

		if (importNotes) {
			// Strip out assumed image path since TripleA assumes (/doc/images)
			val html = Files.readString(notesFile).replace("src=\"images/", "src=\"");
			
	        // Inline CSS styles 
			val bais = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
			val src = new StreamDocumentSource(bais, null, MediaType.HTML_UTF_8.toString());
	        val doc = new DefaultDOMSource(src).parse();
	        val da = new DOMAnalyzer(doc);
	        da.attributesToStyles();
	        da.addStyleSheet(null, CSSNorm.userStyleSheet(), DOMAnalyzer.Origin.AGENT);
	        da.getStyleSheets();
	        da.stylesToDomInherited();
	        
	        val baos = new ByteArrayOutputStream();
	        new NormalOutput(doc).dumpTo(baos);
	        val inlinedHtml = new String(baos.toByteArray(), StandardCharsets.UTF_8);
			
			Files.writeString(gameFile, s.substring(0, i) + inlinedHtml + s.substring(j));
		} else {
			Files.writeString(notesFile, s.substring(i, j));
		}
		return null;
	}
}
