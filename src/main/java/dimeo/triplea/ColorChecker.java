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
	private static final int[] FIXED_COLORS = { 
		new Color(0, 0, 0).getRGB(), new Color(255, 255, 255).getRGB(), new Color(0, 127, 255).getRGB()
	};
	
	public static void main(String[] args) throws IOException {
		File f = new File(Utilities.first(args));
		BufferedImage img = ImageIO.read(f);
		Map<Integer, Integer> map = new HashMap<>();
		int w = img.getWidth();
		int h = img.getHeight();
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int rgb = (img.getRGB(x, y) & 0xFFFFFF) | 0xFF000000;
				int closest = Integer.MAX_VALUE;
				for (int fixed : FIXED_COLORS) {
					if (Math.abs(fixed - rgb) < Math.abs(closest - rgb)) {
						closest = fixed;
					}
				}
				map.put(rgb, map.getOrDefault(rgb, 0) + 1);
				img.setRGB(x, y, closest);
			}
		}
		map.forEach((c, count) -> System.out.format("%40s %,15d%n", new Color(c), count));
		ImageIO.write(img, "png", f);
	}
}
