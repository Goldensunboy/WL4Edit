package wl4;

/**
 * This class represents a level, which contains areas
 * 
 * @author Andrew Wilder
 */
public class WL4Level {
	
	/** Level data */
	public WL4Area[] areas;
	public GBAPalette[] palettes = new GBAPalette[0x10];
	public Tile8x8[] tiles = new Tile8x8[0x400];
	public Map16Tile[] m16layer1 = new Map16Tile[0x300];
	
	public String name;
	
	private static final String CMP_NAME = "01";
	
	/**
	 * Construct a new Level object
	 * @param data The ROM data
	 * @param hptr Pointer to the header data
	 * @param dptr Pointer to the area data
	 * @param tptr Pointer to tileset data base (to be offset by dptr[0])
	 */
	public WL4Level(String name, byte[] data, int hptr, int dptr, int tptr) {

		// Load graphical information
		tptr += (data[dptr] & 0xFF) * 36;
		int palPtr = WL4Utils.GetPointer(data, tptr + 8);
		for(int i = 0; i < 16; ++i) {
			palettes[i] = new GBAPalette(data, palPtr + i * 32);
		}
		for(int i = 0; i < tiles.length; ++i) {
			tiles[i] = Tile8x8.DEFAULT_TILE;
		}
		int tileGFXptr = WL4Utils.GetPointer(data, tptr);
		int tileGFXlen = WL4Utils.GetPointer(data, tptr + 4);
		for(int i = 0; i < tileGFXlen / 32 && i + 0x41 < tiles.length; ++i) {
			tiles[i + 0x41] = new Tile8x8(data, tileGFXptr + i * 32);
		}
		int map16l1ptr = WL4Utils.GetPointer(data, tptr + 0x14);
		
//		if(name.equals(CMP_NAME)) {
//			System.out.println("Debug information for level " + name + ":");
//			System.out.printf("\thptr: 0x%06X\n", hptr);
//			System.out.printf("\tdptr: 0x%06X\n", dptr);
//			System.out.printf("\ttptr: 0x%06X\n", tptr);
//			System.out.printf("\tPal ptr: 0x%06X\n", palPtr);
//			System.out.printf("\tTile ptr: 0x%06X\n", tileGFXptr);
//			System.out.printf("\tTile len: 0x%06X\n", tileGFXlen);
//			System.out.printf("\tMap16 ptr: 0x%06X\n", map16l1ptr);
//		}
		
		for(int i = 0; i < m16layer1.length; ++i) {
			m16layer1[i] = new Map16Tile(data, map16l1ptr + i * 8, tiles, palettes);
		}
		
		// Initialize area data
		int areaCount = data[hptr + 1] & 0xFF;
		areas = new WL4Area[areaCount];
		Map16Tile[][] map16layers = {
			m16layer1
		};
		for(int i = 0; i < areaCount; ++i) {
			areas[i] = new WL4Area(String.format("%s-%X", name, i), data, dptr + i * 0x2C, map16layers);
		}
	}
}
