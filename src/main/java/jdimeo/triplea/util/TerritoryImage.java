package jdimeo.triplea.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.math.NumberUtils;

import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "territory-img", description = "Generate an image showing territory polygons with transparency (so it is easier to see details than the polygon grabber tool)")
public class TerritoryImage implements Callable<Void> {
	@Parameters(description = "The folder containing centers.txt, polygons.txt and the place to write place.txt")
	private Path mapFolder;
	
	@Override
	public Void call() throws Exception {
		val territories = TerritoryGeo.fromMapFolder(mapFolder);
		
		val props = new Properties();
		try (val is = Files.newInputStream(mapFolder.resolve("map.properties"))) {
			props.load(is);
		}
		
		val w = NumberUtils.toInt(props.getProperty("map.width"));
		val h = NumberUtils.toInt(props.getProperty("map.height"));
		val img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		
		val g = img.createGraphics();
		for (val t : territories) {
			if (t.getName().contains("RR")) {
				g.setBackground(new Color(255, 100, 100, 100));
				g.setColor(new Color(155, 50, 50, 100));
			} else if (t.getName().contains("SZ")) {
				g.setBackground(new Color(100, 100, 255, 100));
				g.setColor(new Color(50, 50, 155, 100));
			} else {
				g.setBackground(new Color(100, 255, 100, 100));
				g.setColor(new Color(50, 155, 50, 100));
			}
			for (val p : t.getPolys()) {
				g.fill(p);
				g.draw(p);
			}
			if (t.getCenter() != null) {
				g.drawOval(t.getCenter().x - 2, t.getCenter().y - 2, 4, 4);
				g.setColor(Color.BLACK);
				g.drawString(t.getName(), t.getCenter().x - 2, t.getCenter().y + 14);
			}
		}
		g.dispose();
		
		ImageIO.write(img, "png", new File("out.png"));
		return null;
	}
}
