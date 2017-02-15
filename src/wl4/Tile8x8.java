package wl4;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * This class represents an indexed-graphics, 8x8 tile on the GBA
 * 
 * @author Andrew Wilder
 */
public class Tile8x8 {
	
	private static final Color[] _pal = {
		new Color(  0,   0,   0), new Color(255,   0,   0), new Color(  0, 255,   0), new Color(  0,   0, 255),
		new Color(255, 255,   0), new Color(255,   0, 255), new Color(  0, 255, 255), new Color(255, 255, 255),
		new Color(127,   0,   0), new Color(  0, 127,   0), new Color(  0,   0, 127), new Color(127, 127,   0),
		new Color(127,   0, 127), new Color(  0, 127, 127), new Color(127, 127, 127), new Color(127,   0, 255)
	};
	private static final Color[] _pal2 = {
		Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK,
		Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK
	};
	private static final GBAPalette DEFAULT_PALETTE = new GBAPalette(_pal);
	private static final GBAPalette BLACK_PALETTE = new GBAPalette(_pal2);
	public static final Tile8x8 DEFAULT_TILE = new Tile8x8();

	/** Instance data */
	private int[] pixelData = new int[8];
	public boolean flipX = false, flipY = false;
	public GBAPalette palette = DEFAULT_PALETTE;
	
	/**
	 * Construct a new Tile8x8 object from ROM data
	 * @param data The ROM data
	 * @param ptr Pointer to the beginning of the 8x8 graphics
	 */
	public Tile8x8(byte[] data, int ptr) {
		for(int i = 0; i < 8; ++i) {
			pixelData[i] = 0;
			for(int j = 0; j < 4; ++j) {
				byte val = data[ptr + (i << 2) + j];
				pixelData[i] |= (val & 0xF) << (j << 3);
				pixelData[i] |= (val & 0xF0) << (j << 3);
			}
		}
	}
	
	/**
	 * Copy constructor
	 * @param other Another Tile8x8 to copy
	 */
	public Tile8x8(Tile8x8 other) {
		for(int i = 0; i < 8; ++i) {
			pixelData[i] = other.pixelData[i];
		}
		flipX = other.flipX;
		flipY = other.flipY;
		palette = other.palette;
	}
	
	/**
	 * Default constructor for uninitialized tiles
	 */
	private Tile8x8() {
		for(int i = 0; i < 8; ++i) {
			pixelData[i] = 0;
		}
		palette = BLACK_PALETTE;
	}
	
	/**
	 * Get the color index this 8x8 tile contains at a given position
	 * @param x The X position of the pixel
	 * @param y The Y position of the pixel
	 * @return The color index 0 - F
	 */
	public int getColorIndex(int x, int y) {
		if(flipX) {
			x = 7 - x;
		}
		if(flipY) {
			y = 7 - y;
		}
		return (pixelData[y] >> (x << 2)) & 0xF;
	}
	
	/**
	 * Get the color from a given position
	 * @param x The X position of the pixel
	 * @param y The Y position of the pixel
	 * @return The Color object, using the associated palette
	 */
	public Color getColor(int x, int y) {
		return palette.colors[getColorIndex(x, y)];
	}
	
	/**
	 * Gets a BufferedImage of the tile
	 * @return
	 */
	public BufferedImage getImage() {
		BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_4BYTE_ABGR);
		for(int y = 0; y < 8; ++y) {
			for(int x = 0; x < 8; ++x) {
				Color col = getColor(x, y);
				int composite = col.getRed() | (col.getGreen() << 8) | (col.getBlue() << 16);
				img.setRGB(x, y, composite);
			}
		}
		return img;
	}
	
	/**
	 * Unfortunately, g.drawImage doesn't appear to work
	 * @param g
	 * @param x
	 * @param y
	 * @param scale
	 */
	public void draw(Graphics g, int x, int y, int scale) {
		for(int i = 0; i < 8; ++i) {
			for(int j = 0; j < 8; ++j) {
				int idx = getColorIndex(j, i);
				if(idx > 0) {
					g.setColor(palette.colors[idx]);
					g.fillRect(x + j * scale, y + i * scale, scale, scale);
				}
			}
		}
	}
}
