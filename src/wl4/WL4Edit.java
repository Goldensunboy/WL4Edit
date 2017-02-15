package wl4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import wl4.ui.DataTester;

/**
 * Main class for WL4Edit
 * 
 * @author Andrew Wilder
 */
public class WL4Edit {
	
	/** Constants */
	public static final int LEVEL_COUNT = 20;
	public static final int LEVEL_DATA_PTR_TABLE = 0x78F280; // 4-byte pointers to 42-byte entries
		// 00   : (Offset / 36) from 0x3F2298 for tptr value for tilesets
		// 01-0B: Unknown
		// 0C-0F: Pointer to first area data, subsequent areas are consecutive
		// 10-2C: Unknown
	public static final int LEVEL_HEADER_TABLE = 0x639068; // 12-byte entries (area count = offset +1)
		// 00   : Unknown
		// 01   : Number of areas
		// 02-0B: Unknown
	public static final int LEVEL_TILESET_TABLE = 0x3F2298; // 36-byte entries (contains tileset info)
		// 00-03: Pointer to layer 0 + 1 tile graphics
		// 04-07: Number of bytes the layer 0 + 1 tile graphics take up
		// 08-0B: Pointer to 256-color stage palette
    	// 0C-0F: Layer 3 graphics
    	// 10-13: Number of bytes the layer 3 tile graphics take up
    	// 14-17: Pointer to layer 1 map16 table
		// 18-1B: Unknown
		// 1C-1F: Unknown
		// 20-23: Unknown
	
	/** ROM data instance */
	public static final String ROM_FILE = "WL4.gba";
	public static byte[] ROM_DATA;
	static {
		try {
			ROM_DATA = Files.readAllBytes(Paths.get(ROM_FILE));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void main(String[] args) {
		WL4Level[] levels = new WL4Level[LEVEL_COUNT];
		for(int i = 0; i < LEVEL_COUNT; ++i) {
			int hptr = LEVEL_HEADER_TABLE + i * 12;
			int dptr = WL4Utils.GetPointer(ROM_DATA, LEVEL_DATA_PTR_TABLE + i * 4);
			int tptr = LEVEL_TILESET_TABLE;
			levels[i] = new WL4Level(String.format("%02X", i), ROM_DATA, hptr, dptr, tptr);
		}
		
//		Tile8x8[][] tilesArr = new Tile8x8[levels.length][];
//		GBAPalette[][] palArr = new GBAPalette[levels.length][];
//		for(int i = 0; i < levels.length; ++i) {
//			tilesArr[i] = levels[i].tiles;
//			palArr[i] = levels[i].palettes;
//		}
//		new DataTester(tiles, palettes);
		
//		JFrame frame = new Tile8x8TestFrame(tilesArr, palArr);
//		Map16Tile[][] map16arr = new Map16Tile[levels.length][];
//		for(int i = 0; i < levels.length; ++i) {
//			map16arr[i] = levels[i].m16layer1;
//		}
//		new DataTester(map16arr);
		
		new DataTester(levels);
	}
}
