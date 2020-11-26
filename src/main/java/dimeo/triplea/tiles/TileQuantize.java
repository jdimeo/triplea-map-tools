package dimeo.triplea.tiles;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "tile-quantize", description = "Quantize (standardize) the colors in an image to black (borders), white (land) and blue (sea) for base tile images")
public class TileQuantize implements Callable<Void> {
	// TODO: Parameterize the 3 colors
	private static final Color[] FIXED_COLORS = { 
		new Color(0, 0, 0), new Color(255, 255, 255), new Color(0, 127, 255)
	};
	
	@Parameters(index = "0", arity = "1", description = "The base tile image (before splitting into tiles) to quantize")
	private Path input;
	
	@Override
	public Void call() throws IOException {
		val img = ImageIO.read(input.toFile());
		Map<Color, Integer> map = new HashMap<>();
		int w = img.getWidth();
		int h = img.getHeight();
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				Color c = new Color(img.getRGB(x, y), true);
				Color newc = c;
				int closest = Integer.MAX_VALUE;
				for (Color fixed : FIXED_COLORS) {
					int delta = Math.abs(c.getRed() - fixed.getRed())
							+ Math.abs(c.getGreen() - fixed.getGreen())
							+ Math.abs(c.getBlue() - fixed.getBlue());
					if (delta < closest) {
						closest = delta;
						newc = fixed;
					}
				}
				c = newc;
				map.merge(c, 1, (i, j) -> i + j);
				img.setRGB(x, y, c.getRGB());
			}
		}
		map.forEach((c, count) -> System.out.format("%40s %,15d%n", c, count));
		ImageIO.write(img, "png", input.toFile());
		return null;
	}
}
