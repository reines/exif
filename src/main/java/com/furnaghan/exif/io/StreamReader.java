package com.furnaghan.exif.io;

import static com.google.common.base.Preconditions.checkState;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.furnaghan.exif.jpeg.Marker;
import com.furnaghan.exif.math.Rational;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;

public class StreamReader implements Closeable {

	private static final Logger LOG = LoggerFactory.getLogger( StreamReader.class );
	private static final Map<Integer, ByteOrder> BYTE_ORDERS = ImmutableMap.of( 0x4949,
			ByteOrder.LITTLE_ENDIAN, 0x4D4D, ByteOrder.BIG_ENDIAN );

	private final BufferedInputStream in;
	private final boolean verbose;
	private ByteOrder byteOrder;

	public StreamReader( final InputStream in, final ByteOrder byteOrder ) {
		this( in, byteOrder, false );
	}

	public StreamReader( final InputStream in, final ByteOrder byteOrder, final boolean verbose ) {
		this.in = new BufferedInputStream( in );
		this.verbose = verbose;
		this.byteOrder = byteOrder;
	}

	public ByteOrder getByteOrder() {
		return byteOrder;
	}

	public void setByteOrder( final ByteOrder byteOrder ) {
		this.byteOrder = byteOrder;
	}

	public boolean available() throws IOException {
		return in.available() > 0;
	}

	public InputStream stream() {
		return in;
	}

	public InputStream limit( final int length ) {
		return ByteStreams.limit( in, length );
	}

	public byte[] readBytes() throws IOException {
		try ( final ByteArrayOutputStream out = new ByteArrayOutputStream() ) {
			ByteStreams.copy( in, out );
			final byte[] bytes = out.toByteArray();
			if ( verbose ) {
				LOG.error( "read: {}", bytes );
			}
			return bytes;
		}
	}

	public byte[] readBytes( final int length ) throws IOException {
		final byte[] bytes = new byte[length];
		ByteStreams.readFully( in, bytes, 0, length );
		if ( verbose ) {
			LOG.error( "read: {}", bytes );
		}
		return bytes;
	}

	public int readShort() throws IOException {
		return DataConversions.toShort( readBytes( 2 ), 0, byteOrder );
	}

	public int readInt() throws IOException {
		return DataConversions.toInt( readBytes( 4 ), 0, byteOrder );
	}

	public Rational readRational() throws IOException {
		return DataConversions.toRational( readBytes( 8 ), 0, byteOrder );
	}

	public float readFloat() throws IOException {
		return DataConversions.toFloat( readBytes( 4 ), 0, byteOrder );
	}

	public double readDouble() throws IOException {
		return DataConversions.toDouble( readBytes( 8 ), 0, byteOrder );
	}

	public String readString( final int length ) throws IOException {
		return DataConversions.toString( readBytes( length ) );
	}

	public Marker readMarker() throws IOException {
		final byte[] markerBytes = new byte[2];
		do {
			markerBytes[0] = markerBytes[1];
			markerBytes[1] = (byte) ( 0xff & in.read() );
		} while ( ( 0xff & markerBytes[0] ) != 0xff || ( 0xff & markerBytes[1] ) == 0xff );
		return Marker.fromId( DataConversions.toShort( markerBytes, 0, byteOrder ) );
	}

	public ByteOrder readByteOrder() throws IOException {
		final int value = readShort();
		checkState( BYTE_ORDERS.containsKey( value ) );
		return BYTE_ORDERS.get( value );
	}

	public void mark() {
		in.mark( Integer.MAX_VALUE );
	}

	public void seek( final long offset ) throws IOException {
		in.reset();
		ByteStreams.skipFully( in, offset );
	}

	@Override
	public void close() throws IOException {
		in.close();
	}
}
