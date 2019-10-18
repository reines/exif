package com.furnaghan.exif;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import com.furnaghan.exif.io.DataConversions;
import com.furnaghan.exif.io.FieldType;
import com.furnaghan.exif.io.StreamReader;
import com.google.common.base.Supplier;

public class ExifTagData implements Supplier<ExifTagReference> {

	static ExifTagData read( final StreamReader data ) throws IOException {
		return new ExifTagData( data.readShort(), data.readShort(), data.readInt(),
				data.readBytes( 4 ) );
	}

	private final ExifTagReference reference;
	private final int count;
	private final byte[] offset;

	private ExifTagData( final int id, final int type, final int count, final byte[] offset ) {
		this.reference = new ExifTagReference( id, FieldType.fromId( type ) );
		this.count = count;
		this.offset = offset;
	}

	@Override
	public ExifTagReference get() {
		return reference;
	}

	Collection<?> get( final StreamReader data ) throws IOException {
		final int length = reference.getType().getSize() * count;

		final InputStream bytes;
		if ( length > 4 ) {
			data.seek( DataConversions.toInt( offset, 0, data.getByteOrder() ) );
			bytes = data.limit( length );
		} else {
			bytes = new ByteArrayInputStream( offset, 0, length );
		}

		return reference.getType().decode( new StreamReader( bytes, data.getByteOrder() ), length );
	}
}
