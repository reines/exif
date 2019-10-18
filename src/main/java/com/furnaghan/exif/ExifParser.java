package com.furnaghan.exif;

import static com.google.common.base.Preconditions.checkState;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.furnaghan.exif.io.NoopOutputStream;
import com.furnaghan.exif.io.StreamReader;
import com.furnaghan.exif.io.StreamWriter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

public class ExifParser {

	private static final int EXIF_MARKER = 0xFFE1;
	private static final Set<Integer> REQUIRED_MARKERS = ImmutableSet.of( EXIF_MARKER );
	private static final String EXIF_NAME = "Exif";

	private static final Logger LOG = LoggerFactory.getLogger( ExifParser.class );

	public static ExifTags read( final File file ) throws IOException {
		try ( final InputStream in = new FileInputStream( file ) ) {
			return read( in );
		}
	}

	public static ExifTags read( final InputStream in ) throws IOException {
		final AtomicReference<ExifTags> exif = new AtomicReference<>( ExifTags.empty() );

		// Process the image, discarding the output
		new JpegParser( new JpegParser.SegmentProcessor() {
			@Override
			public byte[] process( final int marker, final byte[] data ) {
				if ( marker == EXIF_MARKER ) {
					try ( final InputStream exifIn = new ByteArrayInputStream( data ) ) {
						exif.set( readExifData( exifIn ) );
					} catch ( final Exception e ) {
						LOG.warn( "Failed to read exif segment", e );
					}
				}
				return data;
			}
		}, REQUIRED_MARKERS ).process( in, new NoopOutputStream() );

		return exif.get();
	}

	private static ExifTags readExifData( final InputStream in ) throws IOException {
		final StreamReader data = new StreamReader( in, ByteOrder.BIG_ENDIAN );

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
		checkState( data.readShort() == 0x002A, "Invalid TIFF marker" );

		final Collection<ExifTagData> tags = Lists.newLinkedList();

		while ( data.available() ) {
			// Offset to IFD, seek to it
			final int offset = data.readInt();
			if ( offset == 0 ) {
				break;
			}
			data.seek( offset );

			final int tagCount = data.readShort();
			for ( int tagIndex = 0; tagIndex < tagCount; tagIndex++ ) {
				tags.add( ExifTagData.read( data ) );
			}
		}

		final ExifTags exif = ExifTags.empty();

		for ( final ExifTagData tag : tags ) {
			try {
				exif.addAll( tag, tag.get( data ) );
			} catch ( final Exception e ) {
				LOG.warn( "Skipping invalid tag: " + tag, e );
			}
		}

		return exif;
	}

	public static void update( final File file, final Updater updater ) throws IOException {
		final Path tempFile = Files.createTempFile( "exif", "jpg" );
		try ( final InputStream in = new FileInputStream( file ) ) {
			try ( final OutputStream out = new FileOutputStream( tempFile.toFile() ) ) {
				update( in, out, updater );
			}
		}
		Files.move( tempFile, file.toPath(), StandardCopyOption.REPLACE_EXISTING );
	}

	public static void update( final InputStream in, final OutputStream out, final Updater updater )
			throws IOException {
		// Process the image, discarding the output
		new JpegParser( new JpegParser.SegmentProcessor() {
			@Override
			public byte[] process( final int marker, final byte[] data ) {
				if ( marker == EXIF_MARKER ) {
					try ( final InputStream exifIn = new ByteArrayInputStream( data ) ) {
						final ExifTags exif = readExifData( exifIn );
						updater.update( exif );
						try ( final ByteArrayOutputStream exifOut = new ByteArrayOutputStream() ) {
							writeExifData( exif, exifOut );
							return exifOut.toByteArray();
						}
					} catch ( final Exception e ) {
						LOG.warn( "Failed to read exif segment", e );
					}
				}
				return data;
			}
		}, REQUIRED_MARKERS ).process( in, out );
	}

	public static void write( final InputStream in, final OutputStream out, final ExifTags newTags )
			throws IOException {
		update( in, out, new Updater() {
			@Override
			public void update( final ExifTags existingTags ) {
				existingTags.clear();
				for ( final Map.Entry<ExifTagReference, Collection<Object>> entry : newTags.entries() ) {
					existingTags.addAll( entry.getKey(), entry.getValue() );
				}
			}
		} );
	}

	public static void write( final File file, final ExifTags newTags ) throws IOException {
		update( file, new Updater() {
			@Override
			public void update( final ExifTags existingTags ) {
				existingTags.clear();
				for ( final Map.Entry<ExifTagReference, Collection<Object>> entry : newTags.entries() ) {
					existingTags.addAll( entry.getKey(), entry.getValue() );
				}
			}
		} );
	}

	private static void writeExifData( final ExifTags exif, final OutputStream out )
			throws IOException {
		final StreamWriter data = new StreamWriter( out, ByteOrder.BIG_ENDIAN );

		// Write the start of the exif data
		data.writeString( EXIF_NAME );
		data.writeShort( 0 );

		// Mark the start of the TIFF data
		data.mark();

		// Write the byte order for the remaining data
		data.writeByteOrder( data.getByteOrder() );

		// Write TIFF marker
		data.writeShort( 0x002A );

		// Write the offset, both as an exif tag and in the TIFF header
		final int exifOffset = data.offset() + 4;
		exif.set( ExifTag.Image_ExifTag, exifOffset );

		data.writeInt( exifOffset );
		data.writeShort( exif.count() );

		// TODO: Group the tags by directory...

		int offset = data.offset() + 4 + ( exif.count() * 12 );
		final List<byte[]> blobs = Lists.newLinkedList();
		for ( final Map.Entry<ExifTagReference, Collection<Object>> entry : exif.entries() ) {
			final ExifTagReference tag = entry.getKey();
			final byte[] bytes = tag.getType().encode( entry.getValue(), data.getByteOrder() );

			data.writeShort( tag.getId() );
			data.writeShort( tag.getType().getId() );

			// Write the count. For ASCII this is the number of characters, for others this is
			// the number of "things" - almost always 1.
			data.writeInt( bytes.length / tag.getType().getSize() );

			if ( bytes.length > 4 ) {
				data.writeInt( offset );
				offset += bytes.length;
				blobs.add( bytes );
			} else {
				if ( data.getByteOrder() == ByteOrder.BIG_ENDIAN ) {
					data.writeBytes( bytes );
					data.writeBytes( new byte[4 - bytes.length] );
				} else {
					data.writeBytes( new byte[4 - bytes.length] );
					data.writeBytes( bytes );
				}
			}
		}

		data.writeInt( 0 );

		for ( final byte[] bytes : blobs ) {
			data.writeBytes( bytes );
		}
	}

	public interface Updater {
		void update( final ExifTags tags );
	}
}
