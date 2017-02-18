package wl4;

public final class WL4Constants {
	
	/** Constants */
	public static final int LEVEL_DATA_PTR_TABLE = 0x78F280; // 4-byte pointers to 42-byte area entries
		// 00   : (Offset / 36) from 0x3F2298 for tptr value for area tileset
		//      : (Offset / 16) from 0x3F8C18 for animation data dependent upon switches
		//      : (offset / 32) from 0x3F8098 which contains animation frame index data
		//      :               from RAM 0x3007DDC, indexes into 0x3F7828
		//      :               Copy 7 bytes to RAM 0x3003B08:
		//      :                 Src: AA BB CC xx DD DD DD DD
		//      :                 Dst: AA BB 00 CC 00 xx xx xx DD DD DD DD
		// 01   : Layer 0 property bitvector (10 = enable, 20 = ???, 01 = ???, 02 = ???)
		//      :   Bit 5 (0x20) unset: Priority 2 0 1 3
		// 02   : Layer 1 properties (always 10)
		// 03   : Layer 2 properties
		// 04   : Layer 3 properties (commonly 20... ?)
		// 05-07: Always 00
		// 08-0B: Pointer to first area layer 0 data, subsequent areas are consecutive
		// 0C-0F: Pointer to layer 1 data
		// 10-13: Pointer to layer 2 data
		// 14-17: Pointer to layer 3 data
		// 18-2B: Unknown
	public static final int LEVEL_HEADER_TABLE = 0x639068; // 12-byte entries (area count = offset +1)
		// 00   : Unknown
		// 01   : Number of areas
		// 02-0B: Unknown
	public static final int LEVEL_TILESET_TABLE = 0x3F2298; // 36-byte entries (contains tileset info)
		// 00-03: Pointer to layer 0 + 1 tile graphics
		// 04-07: Number of bytes the layer 0 + 1 tile graphics take up
		// 08-0B: Pointer to 256-color stage palette
    	// 0C-0F: Pointer to layer 3 graphics
    	// 10-13: Number of bytes the layer 3 tile graphics take up
    	// 14-17: Pointer to layer 1 map16 table
		// 18-1B: Unknown
		// 1C-1F: Unknown
		// 20-23: Unknown
	public static final int ANIMATION_GFX_TABLE = 0x3F7828; // 8-byte entries
		// 00-03: Unknown
		// 04-07: Pointer to graphics for animation tiles
	public static final int ANIMATION_FRAME_TABLE = 0x3F8098; // 32-byte entries
		// 00-01: (halfword / 8) indexing table 0x3F7828 for animation frame entries
	public static final int ANIMATION_SWITCH_TABLE = 0x3F8C18; // 16-byte entries
		// These entries are offsets into table in work RAM 0x300002E which contains RAM controlling animation
		// Which is dependent upon the active switches:
		//   2E: Nothing
		//   2F: Switch 1: 00 = off, 01 = on, 02 = switching on (one frame), 03 = switching off (one frame)
		//   30: Switch 2: See above
		//   31: Switch 3: See above
		//   32: Frog switch: See above (cannot switch off)
}
