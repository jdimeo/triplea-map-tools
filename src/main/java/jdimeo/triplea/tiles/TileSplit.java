package jdimeo.triplea.tiles;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

import lombok.val;
import lombok.extern.log4j.Log4j2;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Log4j2
@Command(name = "tile-split", description = "Splits an image into equally sized square tile images")
public class TileSplit implements Callable<Void> {
	@Option(names = "-in", description = "The input image", required = true)
	private Path inputFile;
	
	@Option(names = "-out", description = "The output directory")
	private Path outputDir = Paths.get("out");
	
	@Option(names = "-size", description = "The size of the tiles")
	private int tileSize = 256;
	
	@Override
	public Void call() throws IOException {
		val img = ImageIO.read(inputFile.toFile());
		log.info("Splitting {}...", inputFile);
		
		int w = img.getWidth();
		int h = img.getHeight();
		for (int x = 0; x < w; x += tileSize) {
			log.info("Column {}", x);
			
			int tw = Math.min(w - x, tileSize);
			for (int y = 0; y < h; y += tileSize) {
				int th = Math.min(h - x, tileSize);
				
				val p = outputDir.resolve(x / tileSize + "_" + y / tileSize + ".png");
				ImageIO.write(img.getSubimage(x, y, tw, th), "png", p.toFile());
			}
		}
		log.info("Done.");
		
		return null;
	}
}