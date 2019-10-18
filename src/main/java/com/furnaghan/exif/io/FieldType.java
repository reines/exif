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
	Byte( 0x01, 1, new Codec() {
		@Override
		public Collection<?> decode( final StreamReader in, final int length ) throws IOException {
			return Collections.singletonList( in.readBytes( length / Byte.size ) );
		}

		@Override
		public void encode( final Collection<?> values, final StreamWriter out )
				throws IOException {
			for ( final Object value : values ) {
				out.writeBytes( (byte[]) value );
			}
		}
	}, Byte[].class, byte[].class ),
	Ascii( 0x02, 1, new Codec() {
		@Override
		public Collection<Object> decode( final StreamReader in, final int length )
				throws IOException {
			return Collections.<Object>singletonList( in.readString( length / Ascii.size ).trim() );
		}

		@Override
		public void encode( final Collection<?> values, final StreamWriter out )
				throws IOException {
			for ( final Object value : values ) {
				out.writeString( (String) value + '\n' );
			}
		}
	}, String.class ),
	Short( 0x03, 2, new Codec() {
		@Override
		public Collection<?> decode( final StreamReader in, final int length ) throws IOException {
			final Collection<Integer> values = new LinkedList<>();
			while ( in.available() ) {
				values.add( in.readShort() );
			}
			return values;
		}

		@Override
		public void encode( final Collection<?> values, final StreamWriter out )
				throws IOException {
			for ( final Object value : values ) {
				out.writeShort( (int) value );
			}
		}
	}, Short.class, short.class, Integer.class, int.class ),
	Long( 0x04, 4, new Codec() {
		@Override
		public Collection<?> decode( final StreamReader in, final int length ) throws IOException {
			final Collection<Integer> values = new LinkedList<>();
			while ( in.available() ) {
				values.add( in.readInt() );
			}
			return values;
		}

		@Override
		public void encode( final Collection<?> values, final StreamWriter out )
				throws IOException {
			for ( final Object value : values ) {
				out.writeInt( (int) value );
			}
		}
	}, Integer.class, int.class ),
	Rational( 0x05, 8, new Codec() {
		@Override
		public Collection<?> decode( final StreamReader in, final int length ) throws IOException {
			final Collection<Rational> values = new LinkedList<>();
			while ( in.available() ) {
				values.add( in.readRational() );
			}
			return values;
		}

		@Override
		public void encode( final Collection<?> values, final StreamWriter out )
				throws IOException {
			for ( final Object value : values ) {
				out.writeRational( (Rational) value );
			}
		}
	}, Rational.class ),
	SByte( 0x06, 1, Byte.converter, Byte.types ),
	Undefined( 0x07, 1, Byte.converter, Byte.types ),
	SShort( 0x08, 2, Short.converter, Short.types ),
	SLong( 0x09, 4, Long.converter, Long.types ),
	SRational( 0x10, 8, Rational.converter, Rational.types ),
	Float( 0x11, 4, new Codec() {
		@Override
		public Collection<?> decode( final StreamReader in, final int length ) throws IOException {
			final Collection<Float> values = new LinkedList<>();
			while ( in.available() ) {
				values.add( in.readFloat() );
			}
			return values;
		}

		@Override
		public void encode( final Collection<?> values, final StreamWriter out )
				throws IOException {
			for ( final Object value : values ) {
				out.writeFloat( (float) value );
			}
		}
	}, Float.class, float.class ),
	Double( 0x12, 8, new Codec() {
		@Override
		public Collection<?> decode( final StreamReader in, final int length ) throws IOException {
			final Collection<Double> values = new LinkedList<>();
			while ( in.available() ) {
				values.add( in.readDouble() );
			}
			return values;
		}

		@Override
		public void encode( final Collection<?> values, final StreamWriter out )
				throws IOException {
			for ( final Object value : values ) {
				out.writeDouble( (double) value );
			}
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

	public Collection<?> decode( final StreamReader in, final int length ) throws IOException {
		return converter.decode( in, length );
	}

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

	private interface Codec {
		Collection<?> decode( final StreamReader in, final int length ) throws IOException;

		void encode( final Collection<?> values, final StreamWriter out ) throws IOException;
	}
}
