package wl4;

import java.awt.Color;

/**
 * This class represents a collection of 16 colors to define a GBA color palette
 * It also contains helper functions for interpreting GBA data
 * 
 * @author Andrew Wilder
 */
public class GBAPalette {
	
	private static final Color TRANSPARENT = new Color(0, 0, 0, 0);
	
	/** Instance data */
	Color[] colors = new Color[16];
	
	/**
	 * Create a new GBAPalette from an address in ROM data
	 * @param data The ROM data
	 * @param ptr The address of the beginning of palette data
	 */
	public GBAPalette(byte[] data, int ptr) {
		colors[0] = TRANSPARENT;
		for(int i = 1; i < 16; ++i) {
			short c = (short) ((data[ptr + (i << 1)] & 0xFF) | ((data[ptr + (i << 1) + 1] & 0xFF) << 8));
			colors[i] = GBAtoColor(c);
		}
	}
	
	/**
	 * Special constructor for ease of use in testing palettes
	 * @param colors
	 */
	public GBAPalette(Color[] colors) {
		for(int i = 0; i < 16; ++i) {
			this.colors[i] = colors[i];
		}
	}
	
	/**
	 * Construct a Color object from a GBA-format short
	 * @param c The color value
	 * @return The Color object
	 */
	private static Color GBAtoColor(short c) {
		int r = (c & 0x1F) << 3;
		int g = ((c >> 5) & 0x1F) << 3;
		int b = ((c >> 10) & 0x1F) << 3;
		return new Color(r, g, b);
	}
}
