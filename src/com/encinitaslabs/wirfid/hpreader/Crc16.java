package com.encinitaslabs.wirfid.hpreader;

import java.util.Arrays;

public class Crc16 {
	private static final int poly = 0x1021;
	private static final int[] crcTable = new int[256];

	static {
	    for(int i = 0; i < 256; i++) {
	        int fcs = 0;
	        int d = i << 8;
	        for (int k = 0; k < 8; k++) {
	            if (((fcs ^ d) & 0x8000) != 0)
	                fcs = (fcs << 1) ^ poly;
	            else
	                fcs = (fcs << 1);
	            d <<= 1;
	            fcs &= 0xffff;
	        }
	        crcTable[i] = fcs;
	    }
    }

	public static int calculate(byte[] bytes, int length) {
		int work = 0xffff;
		
		for(int i = 0; i < length; i++)
			work = (crcTable[(bytes[i] ^ (work >>> 8)) & 0xff ] ^ (work << 8)) & 0xffff;
		
		return work;
	}
	
	public static boolean check(byte[] bytes, int length) {
		byte[] baData;
		byte buffer;
		
		baData = Arrays.copyOfRange(bytes, 0, length);
		buffer = baData[length-2];
		baData[length-2] = baData[length-1];
		baData[length-1] = buffer;

		return (calculate(baData, length) == 0x1d0f);
	}
}
