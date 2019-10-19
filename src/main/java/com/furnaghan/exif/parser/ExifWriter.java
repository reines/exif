package com.furnaghan.exif.parser;

import static com.furnaghan.exif.parser.ExifParser.EXIF_NAME;
import static com.furnaghan.exif.parser.ExifParser.IFD_TAGS;
import static com.furnaghan.exif.parser.ExifParser.TIFF_MARKER;
import static com.furnaghan.exif.parser.ExifParser.VERBOSE;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.furnaghan.exif.ExifTagReference;
import com.furnaghan.exif.ExifTags;
import com.furnaghan.exif.ImageFileDirectory;
import com.furnaghan.exif.io.StreamWriter;
import com.google.common.collect.Table;

public class ExifWriter implements Closeable {

	private static final Logger LOG = LoggerFactory.getLogger( ExifWriter.class );

	private final StreamWriter data;

	public ExifWriter( final OutputStream out ) {
		this.data = new StreamWriter( out, ByteOrder.BIG_ENDIAN, VERBOSE );
	}

	private void writeHeader() throws IOException {
		// Write the start of the exif data
		data.writeString( EXIF_NAME );
		data.writeShort( 0 );

		// Mark the start of the TIFF data
		data.mark();

		// Write the byte order for the remaining data
		data.setByteOrder( ByteOrder.LITTLE_ENDIAN );
		data.writeByteOrder( data.getByteOrder() );

		// Write TIFF marker
		data.writeShort( TIFF_MARKER );
	}

	private int writeDirectory( final ImageFileDirectory ifd,
			final Map<ExifTagReference, Collection<Object>> tags, final int nextOffset )
			throws IOException {
		final int offset = data.offset();
		data.writeShort( tags.size() );

		int blobOffset = data.offset() + ( tags.size() * 12 ) + 4;
		final List<byte[]> blobs = new LinkedList<>();

		LOG.info( "Writing {} entries at offset={} in IFD={}", tags.size(), data.offset(), ifd );
		for ( final Map.Entry<ExifTagReference, Collection<Object>> entry : tags.entrySet() ) {
			try {
				final ExifTagReference tag = entry.getKey();
				final byte[] bytes = tag.getType().encode( entry.getValue(), data.getByteOrder() );

				LOG.info( "Writing entry: {} = {}", tag, entry.getValue() );
				data.writeShort( tag.getId() );
				data.writeShort( tag.getType().getId() );

				// Write the count. For ASCII this is the number of characters, for others this is
				// the number of "things" - almost always 1.
				data.writeInt( bytes.length / tag.getType().getSize() );

				if ( bytes.length > 4 ) {
					data.writeInt( blobOffset );
					LOG.info( "Writing sub-IFD for {} at {}", tag, blobOffset );
					blobOffset += bytes.length;
					blobs.add( bytes );
				} else {
					final byte[] temp = new byte[4];
					if ( data.getByteOrder() == ByteOrder.BIG_ENDIAN ) {
						System.arraycopy( bytes, 0, temp, temp.length - bytes.length,
								bytes.length );
					} else {
						System.arraycopy( bytes, 0, temp, 0, bytes.length );
					}
					data.writeBytes( temp );
				}
			} catch ( final Exception e ) {
				LOG.error( "Failed to write exif tag", e );
			}
		}

		data.writeInt( nextOffset );

		for ( final byte[] bytes : blobs ) {
			data.writeBytes( bytes );
		}

		return offset;
	}

	public void write( final ExifTags exif ) throws IOException {
		// Group the tags by directory...
		final Table<ImageFileDirectory, ExifTagReference, Collection<Object>> entries = exif.asTable();

		writeHeader();

		// Write the offset, both as an exif tag and in the TIFF header
		final int exifOffset = data.offset() + 4;
		data.writeInt( exifOffset );

		// Write image IFD
		// TODO: add an offset pointing after the below
		writeDirectory( ImageFileDirectory.Image, entries.row( ImageFileDirectory.Image ), 0 );

		// Write additional IFDs
		final Map<ExifTagReference, Collection<Object>> ifdTags = new HashMap<>();
		for ( final Map.Entry<ExifTagReference, ImageFileDirectory> entry : IFD_TAGS.entrySet() ) {
			final ExifTagReference ifdTag = entry.getKey();
			final ImageFileDirectory ifd = entry.getValue();
			final Map<ExifTagReference, Collection<Object>> tags = entries.row( ifd );

			if ( !tags.isEmpty() ) {
				final int offset = writeDirectory( ifd, tags, 0 );
				ifdTags.put( ifdTag, Collections.<Object>singleton( offset ) );
			}
		}

		// Write an additional directory pointing to the other IFDs
		writeDirectory( ImageFileDirectory.Image, ifdTags, 0 );
	}

	@Override
	public void close() throws IOException {
		data.close();
	}
}
