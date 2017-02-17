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
	
	public static final int LEVEL_COUNT = 20;
	
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
			levels[i] = new WL4Level(String.format("%02X", i), ROM_DATA, i);
		}
		
//		Tile8x8[][] tilesArr = new Tile8x8[levels.length][];
//		GBAPalette[][] palArr = new GBAPalette[levels.length][];
//		for(int i = 0; i < levels.length; ++i) {
//			tilesArr[i] = levels[i].areas[0].tileGFX;
//			palArr[i] = levels[i].areas[0].palette;
//		}
//		new DataTester(tilesArr, palArr);
		
//		JFrame frame = new Tile8x8TestFrame(tilesArr, palArr);
//		Map16Tile[][] map16arr = new Map16Tile[levels.length][];
//		for(int i = 0; i < levels.length; ++i) {
//			map16arr[i] = levels[i].m16layer1;
//		}
//		new DataTester(map16arr);
		
		new DataTester(levels);
	}
}
