package jdimeo.triplea.tiles;

import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "tile-join", description = "Joins image tile back into the full size original image")
public class TileJoin implements Callable<Void> {
	@Option(names = "-in", description = "The input directory", required = true)
	private Path inputDir;
	
	@Option(names = "-out", description = "The output file")
	private Path outputFile = Paths.get("out.png");
	
	@Override
	public Void call() throws IOException {
		val files = Files.walk(inputDir).filter(Files::isRegularFile).collect(Collectors.toList());
		val images = new HashMap<Point, Image>();
		
		int tileW = 0, tileH = 0, imgW = 0, imgH = 0;
		for (val f : files) {
			val xy = StringUtils.split(f.getFileName().toString(), "_.");
			val x = NumberUtils.toInt(xy[0]);
			val y = NumberUtils.toInt(xy[1]);
			
			val img = ImageIO.read(f.toFile());
			tileW = Math.max(tileW, img.getWidth());
			tileH = Math.max(tileH, img.getHeight());
			
			if (x == 0) { imgH += img.getHeight(); }
			if (y == 0) { imgW += img.getWidth(); }
			
			images.put(new Point(x, y), img);
		}
		
		val img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
		val g2d = img.createGraphics();
		for (val entry : images.entrySet()) {
			g2d.drawImage(entry.getValue(), entry.getKey().x * tileW, entry.getKey().y * tileH, null);
		}
		g2d.dispose();
		
		ImageIO.write(img, "png", outputFile.toFile());
		return null;
	}
}