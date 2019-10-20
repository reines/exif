package com.furnaghan.exif.parser;

import static com.furnaghan.exif.parser.ExifParser.EXIF_NAME;
import static com.furnaghan.exif.parser.ExifParser.IFD_TAGS;
import static com.furnaghan.exif.parser.ExifParser.TIFF_MARKER;
import static com.furnaghan.exif.parser.ExifParser.VERBOSE;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
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
import com.furnaghan.exif.tag.Thumbnail;

public class ExifWriter implements Closeable {

	private static final Logger LOG = LoggerFactory.getLogger( ExifWriter.class );

	private final StreamWriter data;

	public ExifWriter( final OutputStream out ) {
		this.data = new StreamWriter( out, ByteOrder.BIG_ENDIAN, VERBOSE );
	}

	private void writeHeader() throws IOException {
		LOG.info( "Writing header at {}", data.offset() );

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

		// Write the IFD0 offset in the TIFF header
		final int exifOffset = data.offset() + 4;
		data.writeInt( exifOffset );
	}

	private void writeSubDirectory( final ImageFileDirectory ifd,
			final Map<ExifTagReference, Collection<Object>> tags ) throws IOException {
		LOG.info( "Writing {} IFD at {}", ifd, data.offset() );

		final int numTags = tags.size();
		data.writeShort( numTags );

		int endOffset = data.offset() + ( numTags * 12 ) + 4;
		final List<byte[]> blobs = new LinkedList<>();

		LOG.info( "Writing {} entries at offset={} in IFD={}", numTags, data.offset(), ifd );

		for ( final Map.Entry<ExifTagReference, Collection<Object>> entry : tags.entrySet() ) {
			final byte[] blob = writeTag( entry.getKey(), entry.getValue(), endOffset );
			if ( blob != null ) {
				endOffset += blob.length;
				blobs.add( blob );
			}
		}

		data.writeInt( 0 );

		for ( final byte[] bytes : blobs ) {
			data.writeBytes( bytes );
		}
	}

	private void writeThumbnailIFD( final ExifTags exif ) throws IOException {
		LOG.info( "Writing thumbnail IFD at {}", data.offset() );

		final Collection<byte[]> thumbnails = exif.getThumbnails();
		final Map<ExifTagReference, Collection<Object>> tags = new HashMap<>(
				exif.getDirectory( ImageFileDirectory.Thumbnail ) );

		// Remove any thumbnail tags that made it in here...
		tags.remove( Thumbnail.JPEGInterchangeFormat.get() );
		tags.remove( Thumbnail.JPEGInterchangeFormatLength.get() );

		LOG.info( "Writing {} IFD at {}", ImageFileDirectory.Thumbnail, data.offset() );

		final int numTags = tags.size() + 2;
		data.writeShort( numTags );

		int endOffset = data.offset() + ( numTags * 12 ) + 4;
		final List<byte[]> blobs = new LinkedList<>();

		LOG.info( "Writing {} entries at offset={} in IFD={}", numTags, data.offset(),
				ImageFileDirectory.Thumbnail );

		for ( final Map.Entry<ExifTagReference, Collection<Object>> entry : tags.entrySet() ) {
			final byte[] blob = writeTag( entry.getKey(), entry.getValue(), endOffset );
			if ( blob != null ) {
				endOffset += blob.length;
				blobs.add( blob );
			}
		}

		// Create the thumbnail address and length tags and append the blobs.
		final Collection<Object> jpegInterchangeFormat = new ArrayList<>( thumbnails.size() );
		final Collection<Object> jpegInterchangeFormatLength = new ArrayList<>( thumbnails.size() );

		for ( final byte[] thumbnail : thumbnails ) {
			jpegInterchangeFormat.add( endOffset );
			jpegInterchangeFormatLength.add( thumbnail.length );

			endOffset += thumbnail.length;
			blobs.add( thumbnail );
		}

		writeTag( Thumbnail.JPEGInterchangeFormat.get(), jpegInterchangeFormat, endOffset );
		writeTag( Thumbnail.JPEGInterchangeFormatLength.get(), jpegInterchangeFormatLength,
				endOffset );

		data.writeInt( 0 );

		for ( final byte[] bytes : blobs ) {
			LOG.info( "Writing {} byte blob at {}", bytes.length, data.offset() );
			data.writeBytes( bytes );
		}
	}

	private byte[] writeTag( final ExifTagReference tag, final Collection<?> values,
			final int blobOffset ) throws IOException {
		final byte[] bytes = tag.getType().encode( values, data.getByteOrder() );

		LOG.info( "Writing entry: {} = {}", tag, values );
		data.writeShort( tag.getId() );
		data.writeShort( tag.getType().getId() );

		// Write the count. For ASCII this is the number of characters, for others this is
		// the number of "things" - almost always 1.
		data.writeInt( bytes.length / tag.getType().getSize() );

		if ( bytes.length > 4 ) {
			data.writeInt( blobOffset );
			LOG.info( "Writing sub-IFD for {} at {}", tag, blobOffset );
			return bytes;
		} else {
			final byte[] temp = new byte[4];
			if ( data.getByteOrder() == ByteOrder.BIG_ENDIAN ) {
				System.arraycopy( bytes, 0, temp, temp.length - bytes.length, bytes.length );
			} else {
				System.arraycopy( bytes, 0, temp, 0, bytes.length );
			}
			data.writeBytes( temp );
			return null;
		}
	}

	public void write( final ExifTags exif ) throws IOException {
		// Write segment header
		writeHeader();

		// Write image IFD0
		LOG.info( "Writing main image IFD at {}", data.offset() );
		final Map<ExifTagReference, Collection<Object>> image = exif.getDirectory(
				ImageFileDirectory.Image );
		final int numTags = image.size() + IFD_TAGS.size();
		data.writeShort( numTags );

		int endOffset = data.offset() + ( numTags * 12 ) + 4;
		final List<byte[]> blobs = new LinkedList<>();

		LOG.info( "Writing {} entries at offset={} in IFD={}", numTags, data.offset(),
				ImageFileDirectory.Image );
		for ( final Map.Entry<ExifTagReference, Collection<Object>> entry : image.entrySet() ) {
			final byte[] blob = writeTag( entry.getKey(), entry.getValue(), endOffset );
			if ( blob != null ) {
				endOffset += blob.length;
				blobs.add( blob );
			}
		}

		for ( final Map.Entry<ExifTagReference, ImageFileDirectory> ifd : IFD_TAGS.entrySet() ) {
			// Write a future reference to the IFD we will write later
			writeTag( ifd.getKey(), Collections.<Object>singleton( endOffset ), endOffset );

			// Figure out the size this IFD will take up so we know when the next will start
			endOffset += ( 2 + 4 );
			for ( final Map.Entry<ExifTagReference, Collection<Object>> entry : exif.getDirectory(
					ifd.getValue() ).entrySet() ) {
				final int length = entry.getKey().getType().getSize( entry.getValue() );
				endOffset += ( 12 + ( length > 4 ? length : 0 ) );
			}
		}

		data.writeInt( exif.hasThumbnails() ? endOffset : 0 );

		for ( final byte[] bytes : blobs ) {
			LOG.info( "Writing {} byte blob at {}", bytes.length, data.offset() );
			data.writeBytes( bytes );
		}

		// Write additional IFDs (GPS etc)
		for ( final Map.Entry<ExifTagReference, ImageFileDirectory> ifd : IFD_TAGS.entrySet() ) {
			writeSubDirectory( ifd.getValue(), exif.getDirectory( ifd.getValue() ) );
		}

		// Write thumbnail IFD1
		if ( exif.hasThumbnails() ) {
			writeThumbnailIFD( exif );
		}
	}

	@Override
	public void close() throws IOException {
		data.close();
	}
}
