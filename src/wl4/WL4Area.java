package wl4;

import java.awt.Graphics;

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
	
	/** Graphical data */
	public GBAPalette[] palette = new GBAPalette[16];
	public Tile8x8[] layer0and1tiles;
	public Map16Tile[][] map16 = new Map16Tile[1][]; // layer, entry
	
	public String name;
	
	/**
	 * Construct a new Area object from ROM data and a starting address
	 * @param data The ROM data
	 * @param hptr Pointer to the start of the header data
	 */
	public WL4Area(String name, byte[] data, int ahptr) {
		
		// Get header information
		this.name = name;
		
		// Load graphical information
		int tptr = WL4Constants.LEVEL_TILESET_TABLE + (data[ahptr] & 0xFF) * 36;
		int palPtr = WL4Utils.GetPointer(data, tptr + 8);
		for(int i = 0; i < 16; ++i) {
			palette[i] = new GBAPalette(data, palPtr + (i << 5));
		}
		int tileGFXptr = WL4Utils.GetPointer(data, tptr);
		int tileGFXlen = WL4Utils.GetPointer(data, tptr + 4); // TODO fix use of this
		//layer0and1tiles = new Tile8x8[(tileGFXlen >> 5) + 0x41];
		layer0and1tiles = new Tile8x8[0x400];
		for(int i = 0; i < 0x41; ++i) {
			layer0and1tiles[i] = Tile8x8.DEFAULT_TILE;
		}
		for(int i = 0; i + 0x41 < layer0and1tiles.length; ++i) {
			layer0and1tiles[i + 0x41] = new Tile8x8(data, tileGFXptr + (i << 5));
		}
		
		// Load map16 information
		int map16layer1ptr = WL4Utils.GetPointer(data, tptr + 0x14);
		Map16Tile[] map16layer1 = new Map16Tile[0x300];
		for(int i = 0; i < map16layer1.length; ++i) {
			map16layer1[i] = new Map16Tile(data, map16layer1ptr + (i << 3), layer0and1tiles, palette);
		}
		map16[0] = map16layer1;
		
		// Set up layer 1
		int layer1ptr = WL4Utils.GetPointer(data, ahptr + 0xC);
		width = 0xFF & data[layer1ptr];
		height = 0xFF & data[layer1ptr + 1];
		
		// Decompress tile data (lower byte RLE, then upper byte RLE)
		layer1Tiles = new short[width * height];
		RLEData lower = WL4Utils.uncompressRLE(data, layer1ptr + 2);
		RLEData upper = WL4Utils.uncompressRLE(data, layer1ptr + 2 + lower.compressedSize);
		if(lower.data.length != upper.data.length) {
			System.out.printf("Warning: Upper/Lower size mismatch for area 0x%06X: %d %d\n", ahptr,
					lower.data.length, upper.data.length);
			return;
		}
		if(lower.data.length != layer1Tiles.length) {
			System.out.printf("Warning: RLE data size mismatch for area 0x%06X: %d %d\n", ahptr,
					lower.data.length, layer1Tiles.length);
			return;
		}
		for(int i = 0; i < layer1Tiles.length; ++i) {
			layer1Tiles[i] = (short) (((0xFF & upper.data[i]) << 8) | (0xFF & lower.data[i]));
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
				map16[0][layer1Tiles[i * width + j]].draw(g, j * 16 * scale, i * 16 * scale, scale);
			}
		}
	}
}
