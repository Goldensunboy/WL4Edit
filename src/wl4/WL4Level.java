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
	 * @param hptr Pointer to the header data
	 * @param dptr Pointer to the area data
	 * @param tptr Pointer to tileset data base (to be offset by dptr[0])
	 */
	public WL4Level(String name, byte[] data, int lvidx) {

		// Get pointer information for this level
		int lhptr = WL4Constants.LEVEL_HEADER_TABLE + lvidx * 12;
		int ahptr = WL4Utils.GetPointer(data, WL4Constants.LEVEL_DATA_PTR_TABLE + (lvidx << 2));
		
		// Initialize area data
		int areaCount = data[lhptr + 1] & 0xFF;
		areas = new WL4Area[areaCount];
		for(int i = 0; i < areaCount; ++i) {
			areas[i] = new WL4Area(String.format("%s-%X", name, i), data, ahptr + i * 0x2C);
		}
	}
}
