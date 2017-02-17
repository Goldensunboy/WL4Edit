package wl4;

import java.util.ArrayList;
import java.util.List;

/**
 * Uncategorized utility functions
 * 
 * @author Andrew Wilder
 */
public class WL4Utils {
	
	/**
	 * Retrieves a little-endian pointer value from a ROM address
	 * @param data The ROM data
	 * @param addr The address of the pointer
	 * @return The pointer, minus the 0x08 uppermost 8 bits for the GBA memory map
	 */
	public static int GetPointer(byte[] data, int addr) {
		int a = 0xFF & data[addr];
		int b = 0xFF & data[addr + 1];
		int c = 0xFF & data[addr + 2];
		return a | (b << 8) | (c << 16);
	}
	
	/**
	 * Load a litt-endian short value from ROM data
	 * @param data The ROM data
	 * @param addr The address of the short value
	 * @return The short value
	 */
	public static int GetShort(byte[] data, int addr) {
		return (data[addr] & 0xFF) | ((data[addr + 1] & 0xFF) << 8);
	}
	
	/**
	 * Print data for ease of use in viewing the bytes
	 * @param data ROM data
	 * @param ptr Pointer to first byte of data to print
	 * @param count Number of bytes to print
	 * @param width Number of bytes per line
	 * @param tabs Number of tabs to prefix each line
	 */
	public static void PrintData(byte[] data, int ptr, int count, int width, int tabs) {
		for(int i = 0; i < count; ++i) {
			if(i % width == 0) {
				if(i != 0) {
					System.out.println();
				}
				for(int j = 0; j < tabs; ++j) {
					System.out.print("\t");
				}
			}
			System.out.printf("%02X ", data[ptr + i]);
		}
		System.out.println();
	}
	
	/**
	 * Uncompress RLE data within the ROM
	 * @param data The ROM data
	 * @param ptr Pointer to the start of the compressed data
	 * @return An RLEData structure containing the decompressed data and size of the compressed data
	 */
	public static RLEData uncompressRLE(byte[] data, int ptr) {
		return new RLEData(data, ptr);
	}
	
	/**
	 * This inner class represents a pairing of uncompressed RLE data and the compressed size
	 */
	public static class RLEData {
		
		/** Instance data */
		public byte[] data;
		public int compressedSize;
		
		/**
		 * Create a new RLEData object
		 * @param romData The ROM data
		 * @param ptr Pointer to the beginning of the RLE data
		 */
		public RLEData(byte[] romData, int ptr) {
			
			// Perform the RLE decompression
			List<Byte> ldata = new ArrayList<>();
			int len, dptr = ptr;
			switch(romData[dptr++]) {
			case 0x01: // Single-byte length RLE
				while((len = romData[dptr++]) != 0) {
					if((len & 0x80) == 0) {
						// Copy a run of values once each
						while(len-- > 0) {
							ldata.add(romData[dptr++]);
						}
					} else {
						// Copy a single value several times
						len &= 0x7F;
						while(len-- > 0) {
							ldata.add(romData[dptr]);
						}
						++dptr;
					}
				}
				break;
			case 0x02: // Double-byte length RLE
				len = ((0xFF & romData[dptr]) << 8) | (0xFF & romData[dptr + 1]);
				dptr += 2;
				while(len != 0) {
					if((len & 0x8000) == 0) {
						// Copy a run of values once each
						while(len-- > 0) {
							ldata.add(romData[dptr++]);
						}
					} else {
						// Copy a single value several times
						len &= 0x7FFF;
						while(len-- > 0) {
							ldata.add(romData[dptr]);
						}
						++dptr;
					}
					len = ((0xFF & romData[dptr]) << 8) | (0xFF & romData[dptr + 1]);
					dptr += 2;
				}
				break;
			default:
				System.out.println("Unknown RLE type: " + romData[ptr] + String.format(" (0x%06X)", ptr));
				return;
			}
			
			// Initialize the RLEData object
			data = new byte[ldata.size()];
			for(int i = 0; i < data.length; ++i) {
				data[i] = ldata.get(i);
			}
			compressedSize = dptr - ptr;
		}
	}
}
