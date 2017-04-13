package wl4;

/**
 * This class represents a level, which contains areas
 * 
 * @author Andrew Wilder
 */
public class WL4Level {
	
	/** Level data */
	public WL4Area[] areas;
	
	public String name;
	
	/**
	 * Construct a new Level object
	 * @param data The ROM data
	 * @param passage The selected passage:
	 * 			0: Beginning
	 * 			1: Emerald
	 * 			2: Ruby
	 * 			3: Topaz
	 * 			4: Saphhire
	 * 			5: Golden
	 * @param level The selected level:
	 * 			0: Level 1
	 * 			1: Level 2
	 * 			2: Level 3
	 * 			3: Level 4
	 * 			4: Mini-game shop
	 * 			5: Boss door
	 */
	public WL4Level(String name, byte[] data, int passage, int level) {
		this.name = name;
		
		// Get level header index
		int lhidx = WL4Utils.GetInt(data, WL4Constants.LEVEL_HEADER_IDX_TABLE +
				passage * 24 + (level << 2));

		// Get pointer information for this level
		int lhptr = WL4Constants.LEVEL_HEADER_TABLE + lhidx * 12;
		int lvidx = data[lhptr] & 0xFF;
		int ahptr = WL4Utils.GetPointer(data, WL4Constants.AREA_DATA_PTR_TABLE + (lvidx << 2));
		
//		System.out.printf("Level %d-%d (%02X, %02X):\n", passage, level, lhidx, lvidx);
//		WL4Utils.PrintData(data, lhptr, 12, 12, 1);
		
		// Initialize area data
		int areaCount = data[lhptr + 1] & 0xFF;
		areas = new WL4Area[areaCount];
		for(int i = 0; i < areaCount; ++i) {
			areas[i] = new WL4Area(String.format("%s-%X", name, i), data, ahptr + i * 0x2C);
		}
	}
}
