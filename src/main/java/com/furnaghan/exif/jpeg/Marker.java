package com.furnaghan.exif.jpeg;

import java.util.HashMap;
import java.util.Map;

public enum Marker {
	SOI( 0xFFD8, "Start Of Image" ),
	SOF0( 0xFFC0, "Start Of Frame (Baseline DCT)" ),
	SOF2( 0xFFC2, "Start Of Frame (Progressive DCT)" ),
	DHT( 0xFFC4, "Define Huffman Table(s)" ),
	DQT( 0xFFDB, "Define Quantization Table(s)" ),
	DRI( 0xFFDD, "Define Restart Interval" ),
	SOS( 0xFFDA, "Start Of Scan" ),
	RST0( 0xFFD0, "Restart 0" ),
	RST1( 0xFFD1, "Restart 1" ),
	RST2( 0xFFD2, "Restart 2" ),
	RST3( 0xFFD3, "Restart 3" ),
	RST4( 0xFFD4, "Restart 4" ),
	RST5( 0xFFD5, "Restart 5" ),
	RST6( 0xFFD6, "Restart 6" ),
	RST7( 0xFFD7, "Restart 7" ),
	APP0( 0xFFE0, "JFIF" ),
	APP1( 0xFFE1, "Exif" ),
	APP2( 0xFFE2, "ICC Profile" ),
	APP3( 0xFFE3, "" ),
	APP4( 0xFFE4, "" ),
	APP5( 0xFFE5, "" ),
	APP6( 0xFFE6, "" ),
	APP7( 0xFFE7, "" ),
	APP8( 0xFFE8, "" ),
	APP9( 0xFFE9, "" ),
	APP10( 0xFFEA, "" ),
	APP11( 0xFFEB, "" ),
	APP12( 0xFFEC, "" ),
	APP13( 0xFFED, "Adobe Photoshop" ),
	APP14( 0xFFEE, "" ),
	APP15( 0xFFEF, "" ),
	COM( 0xFFFE, "Comment" ),
	EOI( 0xFFD9, "End Of Image" ),
	;

	private static final Map<Integer, Marker> byId = new HashMap<>();

	static {
		for ( final Marker marker : Marker.values() ) {
			byId.put( marker.id, marker );
		}
	}

	public static Marker fromId( final int id ) {
		return byId.get( id );
	}

	private final int id;
	private final String description;

	Marker( final int id, final String description ) {
		this.id = id;
		this.description = description;
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return String.format( "%s (%s)", name(), description );
	}
}
