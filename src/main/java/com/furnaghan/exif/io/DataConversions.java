package com.furnaghan.exif.io;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import com.furnaghan.exif.math.Rational;

public class DataConversions {

	public static String toString( final byte[] bytes ) {
		return new String( bytes, StandardCharsets.US_ASCII );
	}

	public static byte[] fromString( final String value ) {
		return value.getBytes( StandardCharsets.US_ASCII );
	}

	public static int toByte( final byte[] bytes ) {
		return 0xff & bytes[0];
	}

	public static byte[] fromByte( final int value ) {
		return new byte[] { (byte) value };
	}

	public static int toShort( final byte[] bytes, int offset, final ByteOrder byteOrder ) {
		final int byte0 = 0xff & bytes[offset++];
		final int byte1 = 0xff & bytes[offset++];
		if ( byteOrder == ByteOrder.BIG_ENDIAN ) {
			return ( byte0 << 8 ) | byte1;
		} else {
			return ( byte1 << 8 ) | byte0;
		}
	}

	public static byte[] fromShort( final int value, final ByteOrder byteOrder ) {
		if ( byteOrder == ByteOrder.BIG_ENDIAN ) {
			return new byte[] { (byte) ( value >> 8 ), (byte) value };
		} else {
			return new byte[] { (byte) value, (byte) ( value >> 8 ) };
		}
	}

	public static int toInt( final byte[] bytes, int offset, final ByteOrder byteOrder ) {
		final int byte0 = 0xff & bytes[offset++];
		final int byte1 = 0xff & bytes[offset++];
		final int byte2 = 0xff & bytes[offset++];
		final int byte3 = 0xff & bytes[offset++];
		if ( byteOrder == ByteOrder.BIG_ENDIAN ) {
			return ( byte0 << 24 ) | ( byte1 << 16 ) | ( byte2 << 8 ) | byte3;
		} else {
			return ( byte3 << 24 ) | ( byte2 << 16 ) | ( byte1 << 8 ) | byte0;
		}
	}

	public static byte[] fromInt( final int value, final ByteOrder byteOrder ) {
		if ( byteOrder == ByteOrder.BIG_ENDIAN ) {
			return new byte[] { (byte) ( value >> 24 ),
								(byte) ( value >> 16 ),
								(byte) ( value >> 8 ),
								(byte) value };
		} else {
			return new byte[] { (byte) value,
								(byte) ( value >> 8 ),
								(byte) ( value >> 16 ),
								(byte) ( value >> 24 ) };
		}
	}

	public static float toFloat( final byte[] bytes, int offset, final ByteOrder byteOrder ) {
		final int byte0 = 0xff & bytes[offset++];
		final int byte1 = 0xff & bytes[offset++];
		final int byte2 = 0xff & bytes[offset++];
		final int byte3 = 0xff & bytes[offset++];
		if ( byteOrder == ByteOrder.BIG_ENDIAN ) {
			return Float.intBitsToFloat( ( byte0 << 24 ) | ( byte1 << 16 ) | ( byte2 << 8 ) | byte3 );
		} else {
			return Float.intBitsToFloat( ( byte3 << 24 ) | ( byte2 << 16 ) | ( byte1 << 8 ) | byte0 );
		}
	}

	public static byte[] fromFloat( final float value, final ByteOrder byteOrder ) {
		final int bits = Float.floatToRawIntBits( value );
		return fromInt( bits, byteOrder );
	}

	public static double toDouble( final byte[] bytes, int offset, final ByteOrder byteOrder ) {
		final long byte0 = 0xffL & bytes[offset++];
		final long byte1 = 0xffL & bytes[offset++];
		final long byte2 = 0xffL & bytes[offset++];
		final long byte3 = 0xffL & bytes[offset++];
		final long byte4 = 0xffL & bytes[offset++];
		final long byte5 = 0xffL & bytes[offset++];
		final long byte6 = 0xffL & bytes[offset++];
		final long byte7 = 0xffL & bytes[offset++];
		if ( byteOrder == ByteOrder.BIG_ENDIAN ) {
			return Double.longBitsToDouble( ( byte0 << 56 ) | ( byte1 << 48 ) | ( byte2 << 40 ) | ( byte3 << 32 ) | ( byte4 << 24 ) | ( byte5 << 16 ) | ( byte6 << 8 ) | byte7 );
		} else {
			return Double.longBitsToDouble( ( byte7 << 56 ) | ( byte6 << 48 ) | ( byte5 << 40 ) | ( byte4 << 32 ) | ( byte3 << 24 ) | ( byte2 << 16 ) | ( byte1 << 8 ) | byte0 );
		}
	}

	public static byte[] fromDouble( final double value, final ByteOrder byteOrder ) {
		final long bits = Double.doubleToRawLongBits( value );
		if ( byteOrder == ByteOrder.LITTLE_ENDIAN ) {
			return new byte[] { (byte) ( 0xff & ( bits >> 56 ) ),
								(byte) ( 0xff & ( bits >> 48 ) ),
								(byte) ( 0xff & ( bits >> 40 ) ),
								(byte) ( 0xff & ( bits >> 32 ) ),
								(byte) ( 0xff & ( bits >> 24 ) ),
								(byte) ( 0xff & ( bits >> 16 ) ),
								(byte) ( 0xff & ( bits >> 8 ) ),
								(byte) ( 0xff & bits ) };
		} else {
			return new byte[] { (byte) ( 0xff & bits ),
								(byte) ( 0xff & ( bits >> 8 ) ),
								(byte) ( 0xff & ( bits >> 16 ) ),
								(byte) ( 0xff & ( bits >> 24 ) ),
								(byte) ( 0xff & ( bits >> 32 ) ),
								(byte) ( 0xff & ( bits >> 40 ) ),
								(byte) ( 0xff & ( bits >> 48 ) ),
								(byte) ( 0xff & ( bits >> 56 ) ) };
		}
	}

	public static Rational toRational( final byte[] bytes, int offset, final ByteOrder byteOrder ) {
		final int byte0 = 0xff & bytes[offset++];
		final int byte1 = 0xff & bytes[offset++];
		final int byte2 = 0xff & bytes[offset++];
		final int byte3 = 0xff & bytes[offset++];
		final int byte4 = 0xff & bytes[offset++];
		final int byte5 = 0xff & bytes[offset++];
		final int byte6 = 0xff & bytes[offset++];
		final int byte7 = 0xff & bytes[offset++];
		if ( byteOrder == ByteOrder.BIG_ENDIAN ) {
			final int numerator = ( byte0 << 24 ) | ( byte1 << 16 ) | ( byte2 << 8 ) | byte3;
			final int divisor = ( byte4 << 24 ) | ( byte5 << 16 ) | ( byte6 << 8 ) | byte7;
			return Rational.rational( numerator, divisor );
		} else {
			final int numerator = ( byte3 << 24 ) | ( byte2 << 16 ) | ( byte1 << 8 ) | byte0;
			final int divisor = ( byte7 << 24 ) | ( byte6 << 16 ) | ( byte5 << 8 ) | byte4;
			return Rational.rational( numerator, divisor );
		}
	}

	public static byte[] fromRational( final Rational value, final ByteOrder byteOrder ) {
		if ( byteOrder == ByteOrder.BIG_ENDIAN ) {
			return new byte[] { (byte) ( value.getNumerator() >> 24 ),
								(byte) ( value.getNumerator() >> 16 ),
								(byte) ( value.getNumerator() >> 8 ),
								(byte) value.getNumerator(),
								(byte) ( value.getDenominator() >> 24 ),
								(byte) ( value.getDenominator() >> 16 ),
								(byte) ( value.getDenominator() >> 8 ),
								(byte) value.getDenominator() };
		} else {
			return new byte[] { (byte) value.getNumerator(),
								(byte) ( value.getNumerator() >> 8 ),
								(byte) ( value.getNumerator() >> 16 ),
								(byte) ( value.getNumerator() >> 24 ),
								(byte) value.getDenominator(),
								(byte) ( value.getDenominator() >> 8 ),
								(byte) ( value.getDenominator() >> 16 ),
								(byte) ( value.getDenominator() >> 24 ) };
		}
	}
}
