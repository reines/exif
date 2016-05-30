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

	private File copyFile( final String path ) throws IOException {
		final File file = temporaryFolder.newFile();

		// Just create a temp file for this test
		try ( final InputStream in = ExifParserTest.class.getResourceAsStream( path ) ) {
			Files.copy( in, file.toPath(), StandardCopyOption.REPLACE_EXISTING );
		}

		return file;
	}

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private File sampleImage, diggerImage;

	@Before
	public void setUp() throws IOException {
		sampleImage = copyFile( "/images/sample.jpg" );
		diggerImage = copyFile( "/images/digger.jpg" );
	}

	@Test
	public void testReadExistingImagePicasaHasExpectedTags() throws IOException {
		// Read exif data, don't modify anything
		final ExifTags tags = ExifParser.read( diggerImage );

		assertThat(
				tags.keys(),
				containsInAnyOrder( ExifTag.Image_XResolution, ExifTag.Image_Model,
						ExifTag.Image_YResolution, ExifTag.Image_GPSTag, ExifTag.Image_DateTime,
						ExifTag.Image_JPEGInterchangeFormatLength, ExifTag.Image_Make,
						ExifTag.Image_JPEGInterchangeFormat, ExifTag.Image_Compression,
						ExifTag.Image_YCbCrPositioning, ExifTag.Image_Software,
						ExifTag.Image_ResolutionUnit, ExifTag.Image_Orientation,
						ExifTag.Image_ExifTag) );

		// Read directly
		assertThat( tags.get( ExifTag.Image_Software ), Matchers.contains( "Picasa" ) );
		assertThat( tags.get( ExifTag.Image_Make ), Matchers.contains( "LG Electronics" ) );
		assertThat( tags.get( ExifTag.Image_Model ), Matchers.contains( "LG-H815" ) );

		// Read using some helper methods
		assertThat( tags.getDate(),
				contains( Date.from( Instant.parse( "2016-03-13T11:49:14.00Z" ) ) ) );
		assertThat( tags.getOrientation(), contains( ExifTags.Orientation.NORMAL ) );
		assertThat( tags.getXResolution(), contains( 72 ) );
		assertThat( tags.getYResolution(), contains( 72 ) );
		assertThat( tags.getResolutionUnit(), contains( ExifTags.ResolutionUnit.INCHES ) );
	}

	@Test
	public void testReadExistingSampleImageHasExpectedTags() throws IOException {
		// Read exif data, don't modify anything
		final ExifTags tags = ExifParser.read( sampleImage );

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

		final long originalFileSize = Files.size( sampleImage.toPath() );

		// Update the exif data, overwriting the existing file
		ExifParser.update( sampleImage, tags -> {
			tags.set( ExifTag.Image_Make, test );
		} );

		// Check the file size changed, this is a rough indication that we actually did modify the file.
		final long newFileSize = Files.size( sampleImage.toPath() );
		assertThat( newFileSize, not( originalFileSize ) );

		// Check that the make tag is what we set it to be
		final Collection<String> make = ExifParser.read( sampleImage ).get( ExifTag.Image_Make );
		assertThat( make, Matchers.contains( test ) );
	}

	@Test
	public void testUpdateWithUnknownSegmentRetainsIt() throws IOException {
		final String test = String.format( "test2_%s", new Date() );

		final long originalFileSize = Files.size( diggerImage.toPath() );

		// Update the exif data, overwriting the existing file
		ExifParser.update( diggerImage, tags -> {
			tags.set( ExifTag.Image_Make, test );
		} );

		// Check the file size changed, this is a rough indication that we actually did modify the file.
		final long newFileSize = Files.size( diggerImage.toPath() );
		assertThat( newFileSize, not( originalFileSize ) );

		// Check that the make tag is what we set it to be
		final Collection<String> make = ExifParser.read( diggerImage ).get( ExifTag.Image_Make );
		assertThat( make, Matchers.contains( test ) );
	}

	@Test
	public void testSet() throws IOException {
		final String test = String.format( "test2_%s", new Date() );

		final long originalFileSize = Files.size( sampleImage.toPath() );

		// Create the exif data ourselves
		final ExifTags tags = ExifTags.empty();
		tags.set( ExifTag.Image_Make, test );

		// Set the exif data, throwing away any existing, and overwriting the existing file
		ExifParser.write( sampleImage, tags );

		// Check the file size changed, this is a rough indication that we actually did modify the file.
		final long newFileSize = Files.size( sampleImage.toPath() );
		assertThat( newFileSize, not( originalFileSize ) );

		// Check that the date tag is what we set it to be
		final ExifTags actualTags = ExifParser.read( sampleImage );
		assertThat( actualTags.keys(), Matchers.contains( ExifTag.Image_Make ) );
		assertThat( actualTags.get( ExifTag.Image_Make ), containsInAnyOrder( test ) );
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetWrongDataTypeForNumericField() throws IOException {
		final ExifTags tags = ExifTags.empty();
		tags.set( ExifTag.Image_Orientation, "test" );
		ExifParser.write( sampleImage, tags );
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetWrongDataTypeForStringField() throws IOException {
		final ExifTags tags = ExifTags.empty();
		tags.set( ExifTag.Image_DateTime, new byte[0] );
		ExifParser.write( sampleImage, tags );
	}
}
