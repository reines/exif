package com.furnaghan.exif.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.furnaghan.exif.math.Rational;

public enum FieldType {
	Byte( 0x01, 1, new Codec<byte[]>() {
		@Override
		public Collection<byte[]> decode( final StreamReader in, final int length )
				throws IOException {
			return Collections.singletonList( in.readBytes( length / Byte.size ) );
		}

		@Override
		public void encode( final Collection<byte[]> values, final StreamWriter out )
				throws IOException {
			for ( final byte[] value : values ) {
				out.writeBytes( value );
			}
		}

		@Override
		public int length( final Collection<byte[]> values ) {
			int length = 0;
			for ( final byte[] value : values ) {
				length += value.length;
			}
			return length;
		}
	}, Byte[].class, byte[].class ),
	Ascii( 0x02, 1, new Codec<String>() {
		@Override
		public Collection<String> decode( final StreamReader in, final int length )
				throws IOException {
			return Collections.singletonList( in.readString( length / Ascii.size ).trim() );
		}

		@Override
		public void encode( final Collection<String> values, final StreamWriter out )
				throws IOException {
			for ( final String value : values ) {
				out.writeString( value + "\0" );
			}
		}

		@Override
		public int length( final Collection<String> values ) {
			int length = 0;
			for ( final String value : values ) {
				length += value.length() + 1;
			}
			return length;
		}
	}, String.class ),
	Short( 0x03, 2, new Codec<Integer>() {
		@Override
		public Collection<Integer> decode( final StreamReader in, final int length )
				throws IOException {
			final Collection<Integer> values = new LinkedList<>();
			while ( in.available() ) {
				values.add( in.readShort() );
			}
			return values;
		}

		@Override
		public void encode( final Collection<Integer> values, final StreamWriter out )
				throws IOException {
			for ( final int value : values ) {
				out.writeShort( value );
			}
		}

		@Override
		public int length( final Collection<Integer> values ) {
			return 2 * values.size();
		}
	}, Short.class, short.class, Integer.class, int.class ),
	Long( 0x04, 4, new Codec<Integer>() {
		@Override
		public Collection<Integer> decode( final StreamReader in, final int length )
				throws IOException {
			final Collection<Integer> values = new LinkedList<>();
			while ( in.available() ) {
				values.add( in.readInt() );
			}
			return values;
		}

		@Override
		public void encode( final Collection<Integer> values, final StreamWriter out )
				throws IOException {
			for ( final int value : values ) {
				out.writeInt( value );
			}
		}

		@Override
		public int length( final Collection<Integer> values ) {
			return 4 * values.size();
		}
	}, Integer.class, int.class ),
	Rational( 0x05, 8, new Codec<Rational>() {
		@Override
		public Collection<Rational> decode( final StreamReader in, final int length )
				throws IOException {
			final Collection<Rational> values = new LinkedList<>();
			while ( in.available() ) {
				values.add( in.readRational() );
			}
			return values;
		}

		@Override
		public void encode( final Collection<Rational> values, final StreamWriter out )
				throws IOException {
			for ( final Rational value : values ) {
				out.writeRational( value );
			}
		}

		@Override
		public int length( final Collection<Rational> values ) {
			return 8 * values.size();
		}
	}, Rational.class ),
	SByte( 0x06, 1, Byte.converter, Byte.types ),
	Undefined( 0x07, 1, Byte.converter, Byte.types ),
	SShort( 0x08, 2, Short.converter, Short.types ),
	SLong( 0x09, 4, Long.converter, Long.types ),
	SRational( 0x0A, 8, Rational.converter, Rational.types ),
	Float( 0x0B, 4, new Codec<Float>() {
		@Override
		public Collection<Float> decode( final StreamReader in, final int length )
				throws IOException {
			final Collection<Float> values = new LinkedList<>();
			while ( in.available() ) {
				values.add( in.readFloat() );
			}
			return values;
		}

		@Override
		public void encode( final Collection<Float> values, final StreamWriter out )
				throws IOException {
			for ( final float value : values ) {
				out.writeFloat( value );
			}
		}

		@Override
		public int length( final Collection<Float> values ) {
			return 4 * values.size();
		}
	}, Float.class, float.class ),
	Double( 0x0C, 8, new Codec<Double>() {
		@Override
		public Collection<Double> decode( final StreamReader in, final int length )
				throws IOException {
			final Collection<Double> values = new LinkedList<>();
			while ( in.available() ) {
				values.add( in.readDouble() );
			}
			return values;
		}

		@Override
		public void encode( final Collection<Double> values, final StreamWriter out )
				throws IOException {
			for ( final double value : values ) {
				out.writeDouble( value );
			}
		}

		@Override
		public int length( final Collection<Double> values ) {
			return 8 * values.size();
		}
	}, Double.class, double.class );

	private final int id;
	private final int size;
	private final Codec converter;
	private final Set<Class<?>> types;

	FieldType( final int id, final int size, final Codec converter, final Class<?>... types ) {
		this( id, size, converter, new HashSet<>( Arrays.asList( types ) ) );
	}

	FieldType( final int id, final int size, final Codec converter, final Set<Class<?>> types ) {
		this.id = id;
		this.size = size;
		this.converter = converter;
		this.types = types;
	}

	public Set<Class<?>> getTypes() {
		return types;
	}

	public int getId() {
		return id;
	}

	public int getSize() {
		return size;
	}

	@SuppressWarnings( "unchecked" )
	public int getSize( final Collection<?> values ) {
		return converter.length( values );
	}

	public Collection<?> decode( final StreamReader in, final int length ) throws IOException {
		return converter.decode( in, length );
	}

	@SuppressWarnings("unchecked")
	public byte[] encode( final Collection<?> values, final ByteOrder byteOrder )
			throws IOException {
		try ( final ByteArrayOutputStream out = new ByteArrayOutputStream() ) {
			converter.encode( values, new StreamWriter( out, byteOrder ) );
			return out.toByteArray();
		}
	}

	private static final Map<Integer, FieldType> typesById = new HashMap<>();

	static {
		for ( final FieldType type : FieldType.values() ) {
			typesById.put( type.id, type );
		}
	}

	public static FieldType fromId( final int type ) {
		if ( !typesById.containsKey( type ) ) {
			throw new IllegalStateException( "Unknown field type: " + Integer.toHexString( type ) );
		}

		return typesById.get( type );
	}

	private interface Codec<T> {
		Collection<T> decode( final StreamReader in, final int length ) throws IOException;

		void encode( final Collection<T> values, final StreamWriter out ) throws IOException;

		int length( final Collection<T> values );
	}
}
