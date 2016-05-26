package com.furnaghan.exif.io;

import static java.nio.ByteOrder.BIG_ENDIAN;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.Map;

import com.furnaghan.exif.math.Rational;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CountingOutputStream;

public class StreamWriter {

	private static final Map<ByteOrder, Integer> BYTE_ORDERS = ImmutableMap.of(
			ByteOrder.LITTLE_ENDIAN, 0x4949, BIG_ENDIAN, 0x4D4D );

	private final CountingOutputStream out;
	private ByteOrder byteOrder;
	private long mark;

	public StreamWriter( final OutputStream out, final ByteOrder byteOrder ) {
		this.out = new CountingOutputStream( out );
		this.byteOrder = byteOrder;
		this.mark = 0;
	}

	public ByteOrder getByteOrder() {
		return byteOrder;
	}

	public void setByteOrder( final ByteOrder byteOrder ) {
		this.byteOrder = byteOrder;
	}

	public void mark() {
		this.mark = out.getCount();
	}

	public int offset() {
		return (int) ( out.getCount() - mark );
	}

	public void writeBytes( final byte[] bytes ) throws IOException {
		out.write( bytes );
	}

	public void writeString( final String value ) throws IOException {
		writeBytes( DataConversions.fromString( value ) );
	}

	public void writeShort( final int value ) throws IOException {
		writeBytes( DataConversions.fromShort( value, byteOrder ) );
	}

	public void writeInt( final int value ) throws IOException {
		writeBytes( DataConversions.fromInt( value, byteOrder ) );
	}

	public void writeFloat( final float value ) throws IOException {
		writeBytes( DataConversions.fromFloat( value, byteOrder ) );
	}

	public void writeDouble( final double value ) throws IOException {
		writeBytes( DataConversions.fromDouble( value, byteOrder ) );
	}

	public void writeRational( final Rational value ) throws IOException {
		writeBytes( DataConversions.fromRational( value, byteOrder ) );
	}

	public void writeByteOrder( final ByteOrder order ) throws IOException {
		checkArgument( BYTE_ORDERS.containsKey( order ) );
		final int value = BYTE_ORDERS.get( order );
		writeShort( value );
	}

	public void writeMarker( final int marker ) throws IOException {
		writeShort( marker );
	}
}
