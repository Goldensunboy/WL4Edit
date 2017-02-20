package wl4;

import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
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
	public double alphaBlendingLevel = 1; // percentage to scale layer 0 when blending colors
	
	/** Graphical data */
	public GBAPalette[] palette = new GBAPalette[16];
	public Tile8x8[] tileGFX = new Tile8x8[0x600];
	public Map16Tile[] map16 = new Map16Tile[0x300];
	public Tile8x8[][] directMap = new Tile8x8[4][];
	private BufferedImage[] renderedLayer = new BufferedImage[4];
	
	public String name;
	
	/**
	 * Construct a new Area object from ROM data and a starting address
	 * @param data The ROM data
	 * @param ahptr Pointer to the start of the area header data
	 */
	public WL4Area(String name, byte[] data, int ahptr) {
		
		// Get header information
		this.name = name;
		
//		System.out.printf("0x%06X (%s): ", ahptr, name);
//		WL4Utils.PrintData(data, ahptr, 24 + 16, 24 + 16, 0);
//		WL4Utils.PrintData(data, WL4Constants.LEVEL_TILESET_TABLE + (data[ahptr] & 0xFF) * 36, 0x18, 0x18, 0);
		
		// Load static graphical information
		int toffset = (data[ahptr] & 0xFF); // offset from several tables for various graphics
		int tptr = WL4Constants.LEVEL_TILESET_TABLE + toffset * 36; // tileset header pointer
		// Palettes
		int palPtr = WL4Utils.GetPointer(data, tptr + 8); // pointer to this area's palette data
		for(int i = 0; i < 16; ++i) {
			palette[i] = new GBAPalette(data, palPtr + (i << 5));
		}
		int fgGFXptr = WL4Utils.GetPointer(data, tptr); // layers 0, 1 and 2
		int fgGFXlen = WL4Utils.GetPointer(data, tptr + 4);
		int bgGFXptr = WL4Utils.GetPointer(data, tptr + 12); // layer 3
		int bgGFXlen = WL4Utils.GetPointer(data, tptr + 16);
		// FG/near-bg tiles
		// Stored after the animated tiles and one blank tile
		for(int i = 0; i < (fgGFXlen >> 5); ++i) {
			tileGFX[i + 0x41] = Tile8x8.create(data, fgGFXptr + (i << 5));
		}
		// Far BG tiles
		// These are stored at the end of the 8x8 tile entries, behind a "blank" tile in the last slot
		for(int i = 0; i < (bgGFXlen >> 5); ++i) {
			tileGFX[tileGFX.length - 1 - (bgGFXlen >> 5) + i] = Tile8x8.create(data, bgGFXptr + (i << 5));
		}
		// Empty tiles
		tileGFX[0x40] = tileGFX[tileGFX.length - 1] = Tile8x8.DEFAULT_TILE;
		// This fills all the tiles between fg and bg 8x8 tile entries
		for(int i = 0x41 + (fgGFXlen >> 5); i < tileGFX.length - 1 - (bgGFXlen >> 5); ++i) {
			tileGFX[i] = Tile8x8.DEFAULT_TILE;
		}
		
		// Load animated graphical information
		int fptr = WL4Constants.ANIMATION_FRAME_TABLE + (toffset << 5); // pointer to animation frame data
		for(int i = 0; i < 16; ++i) {
			int fidx = WL4Utils.GetShort(data, fptr + (i << 1)); // frame index
			int gidx = WL4Utils.GetPointer(data, WL4Constants.ANIMATION_GFX_TABLE + (fidx << 3) + 4); // GFX index
			for(int j = 0; j < 4; ++j) {
				tileGFX[(i << 2) + j] = Tile8x8.create(data, gidx + (j << 5));
			}
		}
		
		// Load map16 information
		int map16ptr = WL4Utils.GetPointer(data, tptr + 0x14); // Pointer to start of map16 for this area
		for(int i = 0; i < map16.length; ++i) {
			map16[i] = Map16Tile.create(data, map16ptr + (i << 3), tileGFX, palette);
		}
		
		// Set dimensions
		int dim = WL4Utils.GetShort(data, WL4Utils.GetPointer(data, ahptr + 0xC));
		width = dim & 0xFF; // Effective width and height of the stage, equal to layer 1 (the interactive fg)
		height = (dim >> 8) & 0xFF;
		layerProperties = Arrays.copyOfRange(data, ahptr + 1, ahptr + 5);
		
		// Decompress graphical maps by layer
		for(int i = 0; i < 4; ++i) {
			int layerptr = WL4Utils.GetPointer(data, ahptr + 0x8 + (i << 2));
			// Two separate, consecutive RLE data segments exist per layer; the first is for the data's
			// lower bytes, the second is for the layer's upper bytes. The resulting halfwords, once composed
			// will reference map16 entries if layer properties bit 4 is set, or 8x8 tiles if bit 5 is set.
			RLEData layerLower, layerUpper;
			// Determine layer type
			if((data[ahptr + i + 1] & 0x10) != 0) {
				// Use map16
				layerWidth[i] = data[layerptr] & 0xFF;
				layerHeight[i] = data[layerptr + 1] & 0xFF;
				// map16 RLE data has 2-byte prefix for width and height, in map16 tiles
				layerLower = WL4Utils.uncompressRLE(data, layerptr + 2);
				layerUpper = WL4Utils.uncompressRLE(data, layerptr + 2 + layerLower.compressedSize);
			} else if((data[ahptr + i + 1] & 0x20) != 0) {
				// Direct tiles
				switch(data[layerptr] & 0xFF) {
				case 0:
					layerWidth[i] = layerHeight[i] = 32;
					break;
				case 1:
					layerWidth[i] = 64;
					layerHeight[i] = 32;
					break;
				case 2:
					layerWidth[i] = 32;
					layerHeight[i] = 64;
					break;
				default:
					System.out.printf("Invalid dimension byte encountered for 0x%06X layer %d: %d\n",
							ahptr, i, data[layerptr] & 0xFF);
					layerWidth[i] = layerHeight[i] = 0;
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
			// Do decompressed upper and lower bytes match in size?
			if(layerLower.data.length != layerUpper.data.length) {
				System.out.printf("Warning: Upper/Lower layer %d size mismatch for area 0x%06X!" +
						" Lower: %d, upper: %d\n", i, ahptr, layerLower.data.length, layerUpper.data.length);
			}
			// Does the resulting data match the layer dimensions?
			if(layerLower.data.length != layerWidth[i] * layerHeight[i]) {
				System.out.printf("Warning: RLE data layer %d size mismatch for area 0x%06X!" +
						" Size: %d, Dim: %d\n", i, ahptr, layerLower.data.length, layerWidth[i] * layerHeight[i]);
			}
			// Compose the upper and lower bytes into halfwords
			for(int j = 0; j < layerWidth[i] * layerHeight[i]; ++j) {
				layerMap[i][j] = (short) (((0xFF & layerUpper.data[j]) << 8) | (0xFF & layerLower.data[j]));
			}
			
			// If direct tile mapping, then create tile objects
			if((data[ahptr + i + 1] & 0x20) != 0) {
				// Reorder tiles for BG dimension 1 (2x1 screenblocks)
				if(data[layerptr] == 1) {
					short[] rearranged = new short[layerMap[i].length];
					for(int j = 0; j < 32; ++j) {
						for(int k = 0; k < 32; ++k) {
							rearranged[(j << 6) + k] = layerMap[i][(j << 5) + k];
							rearranged[(j << 6) + k + 32] = layerMap[i][(j << 5) + k + 1024];
						}
					}
					layerMap[i] = rearranged;
				}
				directMap[i] = new Tile8x8[layerWidth[i] * layerHeight[i]];
				for(int j = 0; j < directMap[i].length; ++j) {
					int idx = layerMap[i][j] & 0x3FF;
					directMap[i][j] = Tile8x8.create(tileGFX[0x200 + idx], layerMap[i][j], palette);
				}
			}
		}
		
		// Set layer priority
		layerPriority[3] = 3;
		layerPriority[0] = layerPriority[1] = layerPriority[2] = 0;
		int pri_idx = data[ahptr + 0x1A] & 0xFF;
		// Logically equivalent flow control to switch construct at ROM 0x6AFE2
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
			// Logically equivalent to result from switch construct at ROM 0x6B152
			int EVA = 0;
			switch((pri_idx - 8) >> 2) {
			case 0:
				EVA = 7; break;
			case 1:
				EVA = 10; break;
			case 2:
			case 9:
				EVA = 13; break;
			case 3:
			case 10:
				EVA = 16; break;
			case 4:
				EVA = 0; break;
			case 5:
				EVA = 3; break;
			case 6:
				EVA = 6; break;
			case 7:
				EVA = 9; break;
			case 8:
				EVA = 11; break;
			}
			if(((pri_idx - 8) >> 2) > 3) {
				// What does this signify? 0x6B24E, runs subroutine 0x6ADD4
			}
			alphaBlendingLevel = (16 - EVA) / 16.0;
		}
	}
	
	/**
	 * Return an array with the layer indexes in their appropriate draw order
	 * @return {2, 1, 0, 3} -> Draw layers 2, 1, 0, then 3
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
	 * Get a rendered layer as a buffered image
	 * If the layer has been previously rendered, get the cached version
	 * @param idx The index of the layer to render
	 * @return A buffered image (with alpha) of the layer graphics
	 */
	private BufferedImage getRenderedLayer(int idx) {
		if(renderedLayer[idx] != null) {
			return renderedLayer[idx];
		} else {
			BufferedImage img;
			// map16 or direct mapped?
			if((layerProperties[idx] & 0x10) != 0) {
				img = new BufferedImage(width << 4, height << 4, BufferedImage.TYPE_INT_ARGB);
				Graphics g = img.getGraphics();
				for(int i = 0; i < layerHeight[idx]; ++i) {
					for(int j = 0; j < layerWidth[idx]; ++j) {
						g.drawImage(map16[layerMap[idx][i * layerWidth[idx] + j]].getImage(),
								j << 4, i << 4, null);
					}
				}
			} else if((layerProperties[idx] & 0x20) != 0) {
				img = new BufferedImage(width << 4, height << 4, BufferedImage.TYPE_INT_ARGB);
				Graphics g = img.getGraphics();
				for(int i = 0; i < (height << 1); ++i) {
					for(int j = 0; j < (width << 1); ++j) {
						g.drawImage(directMap[idx][(i % layerHeight[idx]) * layerWidth[idx] +
								(j % layerWidth[idx])].getImage(), j << 3, i << 3, null);
					}
				}
			} else {
				System.out.println("Warning: Tried to render a layer without valid data!");
				img = null;
			}
			return renderedLayer[idx] = img;
		}
	}
	
	/**
	 * Draw layers to a Graphics context
	 * @param g The context to which the layers will be drawn
	 * @param scale The amount by which the image should be scaled up
	 * @param mask Which layers to draw
	 * @param enableAB Use GBA-style alhpa blending
	 */
	public void draw(Graphics g, int scale, int mask, boolean enableAB) {
		BufferedImage canvas = new BufferedImage((width << 4) * scale, (height << 4) * scale,
				BufferedImage.TYPE_INT_ARGB);
		
		// Draw the layers in the appropriate order
		for(int i : drawOrder()) {
			
			// Only draw layers enabled by caller's mask
			if(((mask >> i) & 1) == 1 && layerProperties[i] != 0) {
				
				// Render the layer
				BufferedImage layerimg = getRenderedLayer(i);
				
				// Scale the layer graphics
				BufferedImage scaledimg = new BufferedImage(
						layerimg.getWidth() * scale, layerimg.getHeight() * scale, BufferedImage.TYPE_INT_ARGB);
				AffineTransform at = new AffineTransform();
				at.scale(scale, scale);
				AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
				op.filter(layerimg, scaledimg);
				
				// Apply alpha to layer 0 if enabled
				if(i == 0 && enableAB && alphaBlendingLevel < 1) {
					for(int j = 0; j < scaledimg.getHeight(); ++j) {
						for(int k = 0; k < scaledimg.getWidth(); ++k) {
							int composite = scaledimg.getRGB(k, j);
							// Only modify alpha of opaque values
							if((composite & 0xFF000000) != 0) {
								int target = canvas.getRGB(k, j);
								int dr = Math.min(248, (target & 0xFF) +
										(int) ((composite & 0xFF) * alphaBlendingLevel));
								int dg = Math.min(248, ((target >> 8) & 0xFF) +
										(int) (((composite >> 8) & 0xFF) * alphaBlendingLevel));
								int db = Math.min(248, ((target >> 16) & 0xFF) +
										(int) (((composite >> 16) & 0xFF) * alphaBlendingLevel));
								canvas.setRGB(k, j, dr | (dg << 8) | (db << 16) | 0xFF000000);
							}
						}
					}
				} else {
					Graphics cg = canvas.getGraphics();
					cg.drawImage(scaledimg, 0, 0, null);
					cg.dispose();
				}
			}
		}
		g.drawImage(canvas, 0, 0, null);
	}
	
	/**
	 * Draw all layers
	 * @param g The context to which the layers will be drawn
	 * @param scale The amount by which the image should be scaled up
	 * @param gbaAB Use GBA-style alpha blending
	 */
	public void draw(Graphics g, int scale, boolean gbaAB) {
		draw(g, scale, 0xF, gbaAB);
	}
}
