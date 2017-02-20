package wl4;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents an indexed-graphics, 8x8 tile on the GBA
 * 
 * @author Andrew Wilder
 */
public class Tile8x8 {
	
	/** Default tile information */
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
	
	// Cache all created tiles to reduce memory usage
	private static Map<Tile8x8, Tile8x8> tileCache = new HashMap<Tile8x8, Tile8x8>();

	/** Instance data */
	private int[] pixelData = new int[8];
	public boolean flipX = false, flipY = false;
	public GBAPalette palette = DEFAULT_PALETTE;
	
	/** Graphics cache */
	private BufferedImage img = null;
	private int imghash = 0;
	
	/**
	 * Construct a new Tile8x8 object from ROM data
	 * @param data The ROM data
	 * @param ptr Pointer to the beginning of the 8x8 graphics
	 */
	private Tile8x8(byte[] data, int ptr) {
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
	 * Construct and return a new Tile8x8, or its cached equivalent if it's been created before
	 * @param data The ROM data
	 * @param ptr Pointer to the beginning of the 8x8 graphics
	 * @return The tile
	 */
	public static Tile8x8 create(byte[] data, int ptr) {
		Tile8x8 t = new Tile8x8(data, ptr);
		if(tileCache.containsKey(t)) {
			return tileCache.get(t);
		} else {
			tileCache.put(t, t);
			return t;
		}
	}
	
	/**
	 * Get the cached version of a tile, if it exists in the tile cache
	 * @param t The tile data to look for a cache hit
	 * @return The original tile, or cached version
	 */
	private static Tile8x8 getCachedTile(Tile8x8 t) {
		if(tileCache.containsKey(t)) {
			return tileCache.get(t);
		} else {
			tileCache.put(t, t);
			return t;
		}
	}
	
	/**
	 * Copy constructor
	 * @param other Another Tile8x8 to copy
	 */
	private Tile8x8(Tile8x8 other) {
		for(int i = 0; i < 8; ++i) {
			pixelData[i] = other.pixelData[i];
		}
		flipX = other.flipX;
		flipY = other.flipY;
		palette = other.palette;
	}
	
	/**
	 * Copy constructor for data, but with different properties. Also caches tile
	 * @param other Another tile to copy
	 * @param properties Properties bitvector
	 * @param pals Palette selection
	 * @return The new (or cached) tile
	 */
	public static Tile8x8 create(Tile8x8 other, short properties, GBAPalette[] pals) {
		Tile8x8 t = new Tile8x8(other);
		t.flipX = (properties & (1 << 0xA)) != 0;
		t.flipY = (properties & (1 << 0xB)) != 0;
		t.palette = pals[(properties >> 12) & 0xF];
		return getCachedTile(t);
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
		if(img != null && img.hashCode() == imghash) {
			return img;
		} else {
			BufferedImage newimg = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
			for(int y = 0; y < 8; ++y) {
				for(int x = 0; x < 8; ++x) {
					Color col = getColor(x, y);
					newimg.setRGB(x, y, col.getRGB());
				}
			}
			imghash = newimg.hashCode();
			return img = newimg;
		}
	}
	
	/**
	 * Legacy method for drawing 8x8 tiles
	 * @param g
	 * @param x
	 * @param y
	 * @param scale
	 */
	public void draw(Graphics g, int x, int y, int scale) {
		BufferedImage tileimg = getImage();
		BufferedImage scaledimg = new BufferedImage(
				tileimg.getWidth() * scale, tileimg.getHeight() * scale, BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		at.scale(scale, scale);
		AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		op.filter(tileimg, scaledimg);
		g.drawImage(scaledimg, x, y, null);
	}
	
	/**
	 * Determine if two tiles are equal
	 * If their data, flip values and palettes are the same, they are equal
	 */
	@Override
	public boolean equals(Object o) {
		if(o instanceof Tile8x8) {
			Tile8x8 t = (Tile8x8) o;
			for(int i = 0; i < 8; ++i) {
				if(pixelData[i] != t.pixelData[i]) {
					return false;
				}
			}
			return flipX == t.flipX && flipY == t.flipY && palette.equals(t.palette);
		} else {
			return false;
		}
	}
	
	/**
	 * Compute a collision-resistant hashcode value for this tile
	 */
	@Override
	public int hashCode() {
		int hash = pixelData[0];
		for(int i = 1; i < 8; ++i) {
			hash ^= ((pixelData[i] >> i) & ((1 << (32 - i)) - 1)) | (pixelData[i] << (32 - i));
		}
		if(flipX) {
			hash ^= 1;
		}
		if(flipY) {
			hash ^= 2;
		}
		return hash ^ palette.hashCode();
	}
}
