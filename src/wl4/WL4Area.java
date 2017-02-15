package wl4;

import java.awt.Graphics;
import java.util.HashSet;
import java.util.Set;

import wl4.WL4Utils.RLEData;

/**
 * This class represents an area, which is a part of a level
 * 
 * @author Andrew Wilder
 */
public class WL4Area {
	
	/** Area data */
	public int width;
	public int height;
	public short[] layer1Tiles;
	public Map16Tile[][] map16layers;
	
	public String name;
	public int hptr;
	
	public static Set<Short> shorts = new HashSet<Short>();
	private void do_thing(short s) {
		shorts.add(s);
	}
	
	/**
	 * Construct a new Area object from ROM data and a starting address
	 * @param data The ROM data
	 * @param hptr Pointer to the start of the header data
	 */
	public WL4Area(String name, byte[] data, int hptr, Map16Tile[][] map16layers) {
		
		// Get header information
		this.name = name;
		int dptr = WL4Utils.GetPointer(data, hptr + 0xC);
		width = 0xFF & data[dptr];
		height = 0xFF & data[dptr + 1];
		
		this.map16layers = map16layers;
		this.hptr = hptr;
		
		// Decompress tile data (lower byte RLE, then upper byte RLE)
		layer1Tiles = new short[width * height];
		RLEData lower = WL4Utils.uncompressRLE(data, dptr + 2);
		RLEData upper = WL4Utils.uncompressRLE(data, dptr + 2 + lower.compressedSize);
		if(lower.data.length != upper.data.length) {
			System.out.printf("Warning: Upper/Lower size mismatch for area 0x%06X: %d %d\n", hptr,
					lower.data.length, upper.data.length);
			return;
		}
		if(lower.data.length != layer1Tiles.length) {
			System.out.printf("Warning: RLE data size mismatch for area 0x%06X: %d %d\n", hptr,
					lower.data.length, layer1Tiles.length);
			return;
		}
		for(int i = 0; i < layer1Tiles.length; ++i) {
			layer1Tiles[i] = (short) (((0xFF & upper.data[i]) << 8) | (0xFF & lower.data[i]));
			do_thing(layer1Tiles[i]);
		}
	}
	
	/**
	 * Unfortunately, g.drawImage doesn't appear to work
	 * @param g
	 * @param scale
	 */
	public void draw(Graphics g, int scale) {
		for(int i = 0; i < height; ++i) {
			for(int j = 0; j < width; ++j) {
				map16layers[0][layer1Tiles[i * width + j]].draw(g, j * 16 * scale, i * 16 * scale, scale);
			}
		}
	}
}
