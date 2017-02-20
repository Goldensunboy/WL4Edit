package wl4;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a Map16 tile, which is a concise way of pairing 4 8x8 tiles
 * 
 * @author Andrew Wilder
 */
public class Map16Tile {

	/** Instance data */
	public Tile8x8[] tiles = new Tile8x8[4];
	
	private static Map<Map16Tile, Map16Tile> tileCache = new HashMap<Map16Tile, Map16Tile>();
	
	/**
	 * Constructs a Map16 tile from a location in the ROM
	 * @param data The ROM data
	 * @param ptr Points to the start of the map 16 tile
	 * @param loadedCharset The currently loaded charset in vram
	 * @param loadedPalettes The currently loaded palettes
	 */
	private Map16Tile(byte[] data, int ptr, Tile8x8[] loadedCharset, GBAPalette[] loadedPalettes) {
		for(int i = 0; i < 4; ++i) {
			int val = (data[ptr + (i << 1)] & 0xFF) | ((data[ptr + (i << 1) + 1] & 0xFF) << 8);
			tiles[i] = Tile8x8.create(loadedCharset[val & 0x3FF], (short) val, loadedPalettes);
		}
	}
	
	/**
	 * Get the cached version of a tile, if it exists in the tile cache
	 * @param t The tile data to look for a cache hit
	 * @return The original tile, or cached version
	 */
	private static Map16Tile getCachedTile(Map16Tile t) {
		if(tileCache.containsKey(t)) {
			return tileCache.get(t);
		} else {
			tileCache.put(t, t);
			return t;
		}
	}
	
	/**
	 * Construct and return a new Map16Tile, or its cached equivalent if it's been created before
	 * @param data The ROM data
	 * @param ptr Points to the start of the map 16 tile
	 * @param loadedCharset The currently loaded charset in vram
	 * @param loadedPalettes The currently loaded palettes
	 * @return The tile
	 */
	public static Map16Tile create(byte[] data, int ptr, Tile8x8[] loadedCharset, GBAPalette[] loadedPalettes) {
		Map16Tile t = new Map16Tile(data, ptr, loadedCharset, loadedPalettes);
		return getCachedTile(t);
	}
	
	/**
	 * Gets a BufferedImage of the tile
	 * @return
	 */
	public BufferedImage getImage() {
		BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics g = img.getGraphics();
		g.drawImage(tiles[0].getImage(), 0, 0, null);
		g.drawImage(tiles[1].getImage(), 8, 0, null);
		g.drawImage(tiles[2].getImage(), 0, 8, null);
		g.drawImage(tiles[3].getImage(), 8, 8, null);
		return img;
	}
	
	/**
	 * Determine if two tiles are equal
	 * If their 8x8 tiles are the same, they are equal
	 */
	@Override
	public boolean equals(Object o) {
		if(o instanceof Map16Tile) {
			Map16Tile t = (Map16Tile) o;
			for(int i = 0; i < 4; ++i) {
				if(!tiles[i].equals(t.tiles[i])) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Compute a collision-resistant hashcode value for this tile
	 */
	@Override
	public int hashCode() {
		int hash = (tiles[0].hashCode() << 2) | ((tiles[0].hashCode() >> 30) & 3);
		for(int i = 1; i < 4; ++i) {
			hash ^= (tiles[0].hashCode() << (2 + (i << 2))) |
					((tiles[0].hashCode() >> (32 - (i << 2))) & ((1 << (i << 2)) - 1));
		}
		return hash;
	}
	
	/**
	 * Legacy method for drawing map16 tiles
	 * @param g
	 * @param x
	 * @param y
	 * @param scale
	 */
	public void draw(Graphics g, int x, int y, int scale) {
		tiles[0].draw(g, x, y, scale);
		tiles[1].draw(g, x + (scale << 3), y, scale);
		tiles[2].draw(g, x, y + (scale << 3), scale);
		tiles[3].draw(g, x + (scale << 3), y + (scale << 3), scale);
	}
}
