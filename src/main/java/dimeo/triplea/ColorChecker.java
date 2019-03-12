/*******************************************************************************
 * Copyright (c) 2017 Elder Research, Inc.
 * All rights reserved.
 *******************************************************************************/
package dimeo.triplea;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.elderresearch.commons.lang.Utilities;

public class ColorChecker {
	private static final Color[] FIXED_COLORS = { 
		new Color(0, 0, 0), new Color(255, 255, 255), new Color(0, 127, 255)
	};
	
	public static void main(String[] args) throws IOException {
		File f = new File(Utilities.first(args));
		BufferedImage img = ImageIO.read(f);
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
		ImageIO.write(img, "png", f);
	}
}
