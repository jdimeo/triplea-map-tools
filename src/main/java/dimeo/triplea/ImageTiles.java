/*******************************************************************************
 * Copyright (c) 2017 Elder Research, Inc.
 * All rights reserved.
 *******************************************************************************/
package dimeo.triplea;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.elderresearch.commons.lang.Utilities;


public class ImageTiles {
	private static final int TILE_SIZE = 256;
	
	public static void main(String[] args) throws IOException {
		BufferedImage img = ImageIO.read(new File(Utilities.first(args)));
		
		int w = img.getWidth();
		int h = img.getHeight();
		for (int x = 0; x < w; x += TILE_SIZE) {
			int tw = Math.min(w - x, TILE_SIZE);
			for (int y = 0; y < h; y += TILE_SIZE) {
				int th = Math.min(h - x, TILE_SIZE);
				ImageIO.write(img.getSubimage(x, y, tw, th), "png", new File(x / TILE_SIZE + "_" + y / TILE_SIZE + ".png"));
			}
		}
	}
}
