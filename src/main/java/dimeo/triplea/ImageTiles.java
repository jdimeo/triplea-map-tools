/*******************************************************************************
 * Copyright (c) 2017 Elder Research, Inc.
 * All rights reserved.
 *******************************************************************************/
package dimeo.triplea;

import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.elderresearch.commons.lang.CLIUtils;
import com.elderresearch.commons.lang.LambdaUtils;
import com.elderresearch.commons.lang.LambdaUtils.IORunnable;

import lombok.val;


public class ImageTiles {
	@Parameters(commandNames = "join", commandDescription = "Joins image tile back into the full size original image")
	public static class ImageJoiner implements IORunnable {
		@Parameter(names = "-in", description = "The input directory", required = true)
		private String inputDir;
		
		@Parameter(names = "-out", description = "The output file")
		private String outputFile = "out.png";
		
		@Override
		public void run() throws IOException {
			val in  = new File(inputDir);
			val out = new File(outputFile);
			
			val files = in.listFiles();
			
			val images = new HashMap<Point, Image>();
			
			int tileW = 0, tileH = 0, imgW = 0, imgH = 0;
			for (val f : files) {
				val xy = StringUtils.split(f.getName(), "_.");
				val x = NumberUtils.toInt(xy[0]);
				val y = NumberUtils.toInt(xy[1]);
				
				val img = ImageIO.read(f);
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
			
			ImageIO.write(img, "png", out);
		}
	}
	
	@Parameters(commandNames = "split", commandDescription = "Splits an image into equally sized square tile images")
	public static class ImageSplitter implements IORunnable {
		@Parameter(names = "-in", description = "The input image", required = true)
		private String inputFile;
		
		@Parameter(names = "-out", description = "The output directory")
		private String outputDir = "out";
		
		@Parameter(names = "-size", description = "The size of the tiles")
		private int tileSize = 256;
		
		@Override
		public void run() throws IOException {
			val in  = new File(inputFile);
			val out = new File(outputDir);
			
			BufferedImage img = ImageIO.read(in);
			
			int w = img.getWidth();
			int h = img.getHeight();
			for (int x = 0; x < w; x += tileSize) {
				int tw = Math.min(w - x, tileSize);
				for (int y = 0; y < h; y += tileSize) {
					int th = Math.min(h - x, tileSize);
					ImageIO.write(img.getSubimage(x, y, tw, th), "png", new File(out, x / tileSize + "_" + y / tileSize + ".png"));
				}
			}			
		}
	}
	
	public static void main(String[] args) throws IOException {
		LambdaUtils.IO.accept(CLIUtils.parseCommands(args, new ImageSplitter(), new ImageJoiner()), IORunnable::run);
	}
}
