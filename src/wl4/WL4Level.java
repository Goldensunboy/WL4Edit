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
	 * @param lvidx The level index
	 */
	public WL4Level(String name, byte[] data, int lvidx) {

		// Get pointer information for this level
		int lhptr = WL4Constants.LEVEL_HEADER_TABLE + lvidx * 12;
		int ahptr = WL4Utils.GetPointer(data, WL4Constants.AREA_DATA_PTR_TABLE + (lvidx << 2));
		
		// Initialize area data
		int areaCount = data[lhptr + 1] & 0xFF;
		areas = new WL4Area[areaCount];
		for(int i = 0; i < areaCount; ++i) {
			areas[i] = new WL4Area(String.format("%s-%X", name, i), data, ahptr + i * 0x2C);
		}
	}
}
