package com.furnaghan.exif.parser;

import static com.furnaghan.exif.parser.ExifParser.EXIF_NAME;
import static com.furnaghan.exif.parser.ExifParser.IFD_TAGS;
import static com.furnaghan.exif.parser.ExifParser.TIFF_MARKER;
import static com.furnaghan.exif.parser.ExifParser.VERBOSE;
import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.furnaghan.exif.ExifTagData;
import com.furnaghan.exif.ExifTagReference;
import com.furnaghan.exif.ExifTags;
import com.furnaghan.exif.ImageFileDirectory;
import com.furnaghan.exif.io.StreamReader;
import com.google.common.collect.Lists;

public class ExifReader {

	private static final Logger LOG = LoggerFactory.getLogger( ExifReader.class );

	public static ExifTags readExifData( final InputStream in ) throws IOException {
		final StreamReader data = new StreamReader( in, ByteOrder.BIG_ENDIAN, VERBOSE );

		if ( !data.available() ) {
			return ExifTags.empty();
		}

		// Validate the start of the exif data
		checkState( EXIF_NAME.equalsIgnoreCase( data.readString( 4 ) ), "Invalid Exif header" );
		checkState( data.readShort() == 0 );

		// Mark the start of the TIFF data
		data.mark();

		// Check and write the byte order for the remaining data
		data.setByteOrder( data.readByteOrder() );

		// Validate TIFF marker
		checkState( data.readShort() == TIFF_MARKER, "Invalid TIFF marker" );

		final ExifTags exif = ExifTags.empty();

		final Queue<ImageFileDirectoryReference> ifds = new LinkedList<>();
		ifds.add( new ImageFileDirectoryReference( ImageFileDirectory.Image, data.readInt() ) );

		// While we still know about IFDs...
		while ( !ifds.isEmpty() ) {
			final Collection<ExifTagData> tags = Lists.newLinkedList();

			final ImageFileDirectoryReference ifd = ifds.poll();
			data.seek( ifd.offset );

			final int tagCount = data.readShort();
			LOG.info( "Found {} entries at offset={} in IFD={}", tagCount, ifd.offset, ifd.ifd );

			for ( int tagIndex = 0; tagIndex < tagCount; tagIndex++ ) {
				final ExifTagData tagData = ExifTagData.read( ifd.ifd, data );
				tags.add( tagData );
			}

			// Look for the next IFD
			final int nextOffset = data.readInt();
			if ( nextOffset != 0 ) {
				ifds.add( new ImageFileDirectoryReference( ifd.ifd, nextOffset ) );
			}

			for ( final ExifTagData tag : tags ) {
				final ExifTagReference reference = tag.get();
				try {
					final Collection<Object> values = tag.get( data );
					LOG.info( "Loading entry: {} = {}", reference, values );
					exif.addAll( reference, values );
				} catch ( final Exception e ) {
					LOG.warn( "Skipping invalid tag: {}", reference, e );
				}
			}

			// Queue up any IFD references we found
			for ( final Map.Entry<ExifTagReference, ImageFileDirectory> entry : IFD_TAGS.entrySet() ) {
				for ( final int offset : exif.<Integer>remove( entry.getKey() ) ) {
					ifds.add( new ImageFileDirectoryReference( entry.getValue(), offset ) );
				}
			}
		}

		return exif;
	}

	private static class ImageFileDirectoryReference {
		private final ImageFileDirectory ifd;
		private final int offset;

		private ImageFileDirectoryReference( final ImageFileDirectory ifd, final int offset ) {
			this.ifd = ifd;
			this.offset = offset;
		}
	}
}
