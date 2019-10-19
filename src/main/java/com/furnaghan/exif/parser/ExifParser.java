package com.furnaghan.exif.parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.furnaghan.exif.ExifTagReference;
import com.furnaghan.exif.ExifTags;
import com.furnaghan.exif.ImageFileDirectory;
import com.furnaghan.exif.JpegParser;
import com.furnaghan.exif.io.NoopOutputStream;
import com.furnaghan.exif.tag.Exif;
import com.furnaghan.exif.tag.GPSInfo;
import com.furnaghan.exif.tag.Image;
import com.furnaghan.exif.tag.Iop;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class ExifParser {

	private static final Set<Integer> MARKERS = ImmutableSet.of(
			0xFFE1 ); // TODO: , 0xFFE2, 0xFFED );

	static final String EXIF_NAME = "Exif";
	static final int TIFF_MARKER = 0x002A;

	// @formatter:off
	static final Map<ExifTagReference, ImageFileDirectory> IFD_TAGS = ImmutableMap.of(
			Image.ExifTag.get(), ImageFileDirectory.Exif,
			Image.GPSTag.get(), ImageFileDirectory.GPSInfo,
			Exif.InteroperabilityTag.get(), ImageFileDirectory.Iop
	);
	// @formatter:on

	static final boolean VERBOSE = false;

	private static final Logger LOG = LoggerFactory.getLogger( ExifParser.class );

	static {
		for ( final Image tag : Image.values() ) {
			ExifTagReference.register( tag.get(), tag.name() );
		}
		for ( final Exif tag : Exif.values() ) {
			ExifTagReference.register( tag.get(), tag.name() );
		}
		for ( final GPSInfo tag : GPSInfo.values() ) {
			ExifTagReference.register( tag.get(), tag.name() );
		}
		for ( final Iop tag : Iop.values() ) {
			ExifTagReference.register( tag.get(), tag.name() );
		}
	}

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
				if ( MARKERS.contains( marker ) ) {
					try ( final InputStream exifIn = new ByteArrayInputStream( data ) ) {
						exif.set( ExifReader.readExifData( exifIn ) );
					} catch ( final Exception e ) {
						LOG.warn( "Failed to read exif segment: {}", marker, e );
					}
				}
				return data;
			}
		}, MARKERS ).process( in, new NoopOutputStream() );

		return exif.get();
	}

	public static void copy( final InputStream in, final OutputStream out ) throws IOException {
		update( in, out, new Updater() {
			@Override
			public ExifTags update( final ExifTags tags ) {
				return tags;
			}
		} );
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
				if ( MARKERS.contains( marker ) ) {
					try ( final InputStream exifIn = new ByteArrayInputStream( data ) ) {
						final ExifTags exif = updater.update( ExifReader.readExifData( exifIn ) );
						try ( final ByteArrayOutputStream exifOut = new ByteArrayOutputStream() ) {
							final ExifWriter writer = new ExifWriter( exifOut );
							writer.write( exif );
							return exifOut.toByteArray();
						}
					} catch ( final Exception e ) {
						LOG.warn( "Failed to process exif segment: {}", marker, e );
					}
				}
				return data;
			}
		}, MARKERS ).process( in, out );
	}

	public static void write( final InputStream in, final OutputStream out, final ExifTags newTags )
			throws IOException {
		update( in, out, new Updater() {
			@Override
			public ExifTags update( final ExifTags existingTags ) {
				return newTags;
			}
		} );
	}

	public static void write( final File file, final ExifTags newTags ) throws IOException {
		update( file, new Updater() {
			@Override
			public ExifTags update( final ExifTags existingTags ) {
				return newTags;
			}
		} );
	}

	public interface Updater {
		ExifTags update( final ExifTags tags );
	}
}
