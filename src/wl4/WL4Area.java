package wl4;

import java.awt.Graphics;
import java.util.Arrays;

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
	public short[][] layerMap = new short[4][];
	public byte[] layerProperties;
	public int[] layerWidth = new int[4];
	public int[] layerHeight = new int[4];
	public int[] layerPriority = new int[4];
	public double alphaBlendingOpacity = 0;
	
	/** Graphical data */
	public GBAPalette[] palette = new GBAPalette[16];
	public Tile8x8[] tileGFX = new Tile8x8[0x600];
	public Map16Tile[] map16 = new Map16Tile[0x300];
	public Tile8x8[][] dmaps = new Tile8x8[4][];
	
	
	public String name;
	
	/**
	 * Construct a new Area object from ROM data and a starting address
	 * @param data The ROM data
	 * @param ahptr Pointer to the start of the area header data
	 */
	public WL4Area(String name, byte[] data, int ahptr) {
		
		// Get header information
		this.name = name;
		
		System.out.printf("0x%06X (%s): ", ahptr, name);
		WL4Utils.PrintData(data, ahptr, 24 + 16, 24 + 16, 0);
//		WL4Utils.PrintData(data, WL4Constants.LEVEL_TILESET_TABLE + (data[ahptr] & 0xFF) * 36, 0x18, 0x18, 0);
		
		// Load static graphical information
		int toffset = (data[ahptr] & 0xFF);
		int tptr = WL4Constants.LEVEL_TILESET_TABLE + toffset * 36;
		// Palettes
		int palPtr = WL4Utils.GetPointer(data, tptr + 8);
		for(int i = 0; i < 16; ++i) {
			palette[i] = new GBAPalette(data, palPtr + (i << 5));
		}
		int l0l1GFXptr = WL4Utils.GetPointer(data, tptr);
		int l0l1GFXlen = WL4Utils.GetPointer(data, tptr + 4);
		int l3GFXptr = WL4Utils.GetPointer(data, tptr + 12);
		int l3GFXlen = WL4Utils.GetPointer(data, tptr + 16);
		// FG/near-bg tiles
		for(int i = 0; i < (l0l1GFXlen >> 5); ++i) {
			tileGFX[i + 0x41] = new Tile8x8(data, l0l1GFXptr + (i << 5));
		}
		// Far BG tiles
		for(int i = 0; i < (l3GFXlen >> 5); ++i) {
			tileGFX[tileGFX.length - 1 - (l3GFXlen >> 5) + i] = new Tile8x8(data, l3GFXptr + (i << 5));
		}
		// Empty tiles
		tileGFX[0x40] = tileGFX[tileGFX.length - 1] = Tile8x8.DEFAULT_TILE;
		for(int i = 0x41 + (l0l1GFXlen >> 5); i < tileGFX.length - 1 - (l3GFXlen >> 5); ++i) {
			tileGFX[i] = Tile8x8.DEFAULT_TILE;
		}
		
		// Load animated graphical information
		int fptr = WL4Constants.ANIMATION_FRAME_TABLE + (toffset << 5);
		for(int i = 0; i < 16; ++i) {
			int fidx = WL4Utils.GetShort(data, fptr + (i << 1));
			int gidx = WL4Utils.GetPointer(data, WL4Constants.ANIMATION_GFX_TABLE + (fidx << 3) + 4);
			for(int j = 0; j < 4; ++j) {
				tileGFX[(i << 2) + j] = new Tile8x8(data, gidx + (j << 5));
			}
		}
		
		// Load map16 information
		int map16layer1ptr = WL4Utils.GetPointer(data, tptr + 0x14);
		for(int i = 0; i < map16.length; ++i) {
			map16[i] = new Map16Tile(data, map16layer1ptr + (i << 3), tileGFX, palette);
		}
		
		// Set dimensions
		int dim = WL4Utils.GetShort(data, WL4Utils.GetPointer(data, ahptr + 0xC));
		width = dim & 0xFF;
		height = (dim >> 8) & 0xFF;
		layerProperties = Arrays.copyOfRange(data, ahptr + 1, ahptr + 5);
		
		// Decompress graphical maps by layer
		for(int i = 0; i < 4; ++i) {
			int layerptr = WL4Utils.GetPointer(data, ahptr + 0x8 + (i << 2));
			RLEData layerLower, layerUpper;
			// Determine layer type
			if((data[ahptr + i + 1] & 0x10) != 0) {
				// Use map16
				layerWidth[i] = data[layerptr] & 0xFF;
				layerHeight[i] = data[layerptr + 1] & 0xFF;
				layerLower = WL4Utils.uncompressRLE(data, layerptr + 2);
				layerUpper = WL4Utils.uncompressRLE(data, layerptr + 2 + layerLower.compressedSize);
			} else if((data[ahptr + i + 1] & 0x20) != 0) {
				// Direct tiles
				layerWidth[i] = 64;
				layerHeight[i] = 32; // TODO where do I find these values? dimensions of direct mapped BGs
				switch(name.substring(0, 2)) {
				case "02":
				case "04":
				case "06":
				case "0D":
				case "11":
				case "12":
					layerWidth[i] = 32;
					break;
				case "13":
					switch(name) {
					case "13-0":
					case "13-1":
					case "13-2":
					case "13-3":
					case "13-4":
					case "13-5":
					case "13-6":
					case "13-7":
						layerWidth[i] = 32;
					}
				}
				layerLower = WL4Utils.uncompressRLE(data, layerptr + 1);
				layerUpper = WL4Utils.uncompressRLE(data, layerptr + 1 + layerLower.compressedSize);
			} else {
				// Layer disabled
				continue;
			}
			layerMap[i] = new short[layerWidth[i] * layerHeight[i]];
			
			// Error checking
			if(layerLower.data == null || layerUpper.data == null) {
				return;
			}
			if(layerLower.data.length != layerUpper.data.length) {
				System.out.printf("Warning: Upper/Lower layer %d size mismatch for area 0x%06X!" +
						" Lower: %d, upper: %d\n", i, ahptr, layerLower.data.length, layerUpper.data.length);
			}
			if(layerLower.data.length != layerWidth[i] * layerHeight[i]) {
				System.out.printf("Warning: RLE data layer %d size mismatch for area 0x%06X!" +
						" Size: %d, Dim: %d\n", i, ahptr, layerLower.data.length, layerWidth[i] * layerHeight[i]);
			}
			for(int j = 0; j < layerWidth[i] * layerHeight[i]; ++j) {
				layerMap[i][j] = (short) (((0xFF & layerUpper.data[j]) << 8) | (0xFF & layerLower.data[j]));
			}
			
			// If direct tile mapping, then create tiles
			if((data[ahptr + i + 1] & 0x20) != 0) {
				dmaps[i] = new Tile8x8[layerWidth[i] * layerHeight[i]];
				for(int j = 0; j < dmaps[i].length; ++j) {
					int idx = layerMap[i][j] & 0x3FF;
					dmaps[i][j] = new Tile8x8(tileGFX[0x200 + idx], layerMap[i][j], palette);
				}
			}
		}
		
		// Set layer priority
		layerPriority[3] = 3;
		layerPriority[0] = layerPriority[1] = layerPriority[2] = 0;
		int pri_idx = data[ahptr + 0x1A] & 0xFF;
		if(pri_idx < 5 || (pri_idx & 3) == 0) {
			layerPriority[1] = 1;
			layerPriority[2] = 2;
		} else if((pri_idx & 3) == 1) {
			layerPriority[0] = 1;
			layerPriority[2] = 2;
		} else if((pri_idx & 3) == 2) {
			layerPriority[0] = 1;
			layerPriority[2] = 2;
		} else {
			layerPriority[0] = 2;
			layerPriority[2] = 1;
		}
		
		// Set alpha blending
		if(pri_idx > 7) {
			// Perform alpha blending
			int EVA = (((pri_idx - 8) >> 2) * 3 + 7);
			alphaBlendingOpacity = EVA / 16.0;
			// TODO restructure draw() so layers are rendered separately for alpha blending
		}
	}
	
	/**
	 * Return an array with the layer indexes in their appropriate draw order
	 * @return
	 */
	private int[] drawOrder() {
		int[] order = new int[4];
		int idx = 0;
		for(int i = 3; i >= 0; --i) {
			for(int j = 0; j < 4; ++j) {
				if(layerPriority[j] == i) {
					order[idx++] = j;
				}
			}
		}
		return order;
	}
	
	/**
	 * Unfortunately, g.drawImage doesn't appear to work
	 * @param g
	 * @param scale
	 * @param mask Which layers to draw
	 */
	public void draw(Graphics g, int scale, int mask) {
		for(int i : drawOrder()) {
			if(((mask >> i) & 1) == 1) {
				if((layerProperties[i] & 0x10) != 0) {
					// Use map16
					for(int j = 0; j < layerHeight[i]; ++j) {
						for(int k = 0; k < layerWidth[i]; ++k) {
							map16[layerMap[i][j * layerWidth[i] + k]].draw(
									g, (k << 4) * scale, (j << 4) * scale, scale);
						}
					}
				} else if((layerProperties[i] & 0x20) != 0) {
					// Use direct tile map
					for(int j = 0; j < (layerHeight[1] << 1); ++j) {
						for(int k = 0; k < (layerWidth[1] << 1); ++k) {
							dmaps[i][(j % layerHeight[i]) * layerWidth[i] + (k % layerWidth[i])].draw(
									g, (k << 3) * scale, (j << 3) * scale, scale);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Draw all layers
	 * @param g
	 * @param scale
	 */
	public void draw(Graphics g, int scale) {
		draw(g, scale, 0xF);
	}
}
