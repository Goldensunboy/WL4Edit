package wl4;

import java.awt.Graphics;

/**
 * This class represents a Map16 tile, which is a concise way of pairing 4 8x8 tiles
 * 
 * @author Andrew Wilder
 */
public class Map16Tile {

	/** Instance data */
	public Tile8x8[] tiles = new Tile8x8[4];
	
	/**
	 * Constructs a Map16 tile from a location in the ROM
	 * @param data The ROM data
	 * @param ptr Points to the start of the map 16 tile
	 * @param loadedCharset The currently loaded charset in vram
	 * @param loadedPalettes The currently loaded palettes
	 */
	public Map16Tile(byte[] data, int ptr, Tile8x8[] loadedCharset, GBAPalette[] loadedPalettes) {
		for(int i = 0; i < 4; ++i) {
			int val = (data[ptr + (i << 1)] & 0xFF) | ((data[ptr + (i << 1) + 1] & 0xFF) << 8);
			tiles[i] = new Tile8x8(loadedCharset[val & 0x3FF]);
			tiles[i].flipX = (val & (1 << 0xA)) != 0;
			tiles[i].flipY = (val & (1 << 0xB)) != 0;
			tiles[i].palette = loadedPalettes[(val >> 12) & 0xF];
		}
	}
	
	/**
	 * Unfortunately, g.drawImage doesn't appear to work
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
