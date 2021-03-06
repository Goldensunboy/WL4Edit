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
	
	/** Available passages and levels */
	public static final int[] LEVELS = {
		0, 0,
		0, 4,
		1, 0,
		1, 1,
		1, 2,
		1, 3,
		1, 4,
		2, 0,
		2, 1,
		2, 2,
		2, 3,
		2, 4,
		3, 0,
		3, 1,
		3, 2,
		3, 3,
		3, 4,
		4, 0,
		4, 1,
		4, 2,
		4, 3,
		5, 4,
		5, 0,
		5, 4,
	};
	public static final int LEVEL_COUNT = LEVELS.length / 2;
	
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
	
	/**
	 * For the time being, just test data representation
	 * @param args Unused
	 */
	public static void main(String[] args) {
		WL4Level[] levels = new WL4Level[LEVEL_COUNT];
		for(int i = 0; i < LEVELS.length; i += 2) {
			levels[i >> 1] = new WL4Level(String.format("%d-%d", LEVELS[i], LEVELS[i + 1]),
					ROM_DATA, LEVELS[i], LEVELS[i + 1]);
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
