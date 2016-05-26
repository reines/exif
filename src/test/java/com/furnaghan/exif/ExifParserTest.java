package com.furnaghan.exif;

import static co.unruly.matchers.OptionalMatchers.contains;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ExifParserTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private File targetFile;

	@Before
	public void setUp() throws IOException {
		targetFile = temporaryFolder.newFile();

		// Just create a temp file for this test
		try ( final InputStream in = JpegParserTest.class.getResourceAsStream( "/images/img_1771.jpg" ) ) {
			Files.copy( in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING );
		}
	}

	@Test
	public void testReadExistingImageHasExpectedTags() throws IOException {
		// Read exif data, don't modify anything
		final ExifTags tags = ExifParser.read( targetFile );

		assertThat(
				tags.keys(),
				containsInAnyOrder( ExifTag.Image_ExifTag, ExifTag.Image_DateTime,
						ExifTag.Image_Orientation, ExifTag.Image_JPEGInterchangeFormat,
						ExifTag.Image_JPEGInterchangeFormatLength, ExifTag.Image_Compression,
						ExifTag.Image_YCbCrPositioning, ExifTag.Image_Make, ExifTag.Image_Model,
						ExifTag.Image_XResolution, ExifTag.Image_YResolution,
						ExifTag.Image_ResolutionUnit ) );

		// Read using some helper methods
		assertThat( tags.getDate(),
				contains( Date.from( Instant.parse( "2003-12-14T12:01:44.00Z" ) ) ) );
		assertThat( tags.getOrientation(), contains( ExifTags.Orientation.NORMAL ) );
		assertThat( tags.getXResolution(), contains( 180 ) );
		assertThat( tags.getYResolution(), contains( 180 ) );
		assertThat( tags.getResolutionUnit(), contains( ExifTags.ResolutionUnit.INCHES ) );
	}

	@Test
	public void testUpdateWritesToOriginalFile() throws IOException {
		final String test = String.format( "test2_%s", new Date() );

		final long originalFileSize = Files.size( targetFile.toPath() );

		// Update the exif data, overwriting the existing file
		ExifParser.update( targetFile, tags -> {
			tags.set( ExifTag.Image_Make, test );
		} );

		// Check the file size changed, this is a rough indication that we actually did modify the file.
		final long newFileSize = Files.size( targetFile.toPath() );
		assertThat( newFileSize, not( originalFileSize ) );

		// Check that the make tag is what we set it to be
		final Collection<String> make = ExifParser.read( targetFile ).get( ExifTag.Image_Make );
		assertThat( make, Matchers.contains( test ) );
	}

	@Test
	public void testSet() throws IOException {
		final String test = String.format( "test2_%s", new Date() );

		final long originalFileSize = Files.size( targetFile.toPath() );

		// Create the exif data ourselves
		final ExifTags tags = ExifTags.empty();
		tags.set( ExifTag.Image_Make, test );

		// Set the exif data, throwing away any existing, and overwriting the existing file
		ExifParser.write( targetFile, tags );

		// Check the file size changed, this is a rough indication that we actually did modify the file.
		final long newFileSize = Files.size( targetFile.toPath() );
		assertThat( newFileSize, not( originalFileSize ) );

		// Check that the date tag is what we set it to be
		final ExifTags actualTags = ExifParser.read( targetFile );
		assertThat( actualTags.keys(), Matchers.contains( ExifTag.Image_Make ) );
		assertThat( actualTags.get( ExifTag.Image_Make ), containsInAnyOrder( test ) );
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetWrongDataTypeForNumericField() throws IOException {
		final ExifTags tags = ExifTags.empty();
		tags.set( ExifTag.Image_Orientation, "test" );
		ExifParser.write( targetFile, tags );
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetWrongDataTypeForStringField() throws IOException {
		final ExifTags tags = ExifTags.empty();
		tags.set( ExifTag.Image_DateTime, new byte[0] );
		ExifParser.write( targetFile, tags );
	}
}
