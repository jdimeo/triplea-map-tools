package jdimeo.triplea.util;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.math.NumberUtils;

import lombok.val;
import lombok.extern.log4j.Log4j2;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Log4j2
@Command(name = "territory-img", description = "Generate an image showing territory polygons with transparency (so it is easier to see details than the polygon grabber tool)")
public class TerritoryImage implements Callable<Void> {
	@Parameters(description = "The folder containing centers.txt, polygons.txt and the place to write place.txt")
	private Path mapFolder;
	
	@Option(names = "--scale", description = "The scale to use when rendering the image (full size images can use a lot of memory to draw and be difficult to open in photo editing apps)")
	private float scale = 0.8f;
	
	@Override
	public Void call() throws Exception {
		val territories = TerritoryGeo.fromMapFolder(mapFolder);
		
		val props = new Properties();
		try (val is = Files.newInputStream(mapFolder.resolve("map.properties"))) {
			props.load(is);
		}
		
		val w = scale(NumberUtils.toInt(props.getProperty("map.width")));
		val h = scale(NumberUtils.toInt(props.getProperty("map.height")));
		val img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		
		val g = img.createGraphics();
		g.setTransform(AffineTransform.getScaleInstance(scale, scale));
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
			g.drawOval(t.getCenter().x - 2, t.getCenter().y - 2, 4, 4);
			g.setColor(Color.BLACK);
			g.drawString(t.getName(), t.getCenter().x - 2, t.getCenter().y + 14);
		}
		g.dispose();
		
		ImageIO.write(img, "png", new File("out.png"));
		return null;
	}
	
	private int scale(int x) {
		return Math.round(scale * x);
	}
}
