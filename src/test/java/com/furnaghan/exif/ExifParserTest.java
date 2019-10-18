package com.furnaghan.exif;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
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

		assertThat( tags.keys(),
				containsInAnyOrder( ExifTag.Image_XResolution.get(), ExifTag.Image_Model.get(),
						ExifTag.Image_YResolution.get(), ExifTag.Image_GPSTag.get(),
						ExifTag.Image_DateTime.get(),
						ExifTag.Image_JPEGInterchangeFormatLength.get(), ExifTag.Image_Make.get(),
						ExifTag.Image_JPEGInterchangeFormat.get(), ExifTag.Image_Compression.get(),
						ExifTag.Image_YCbCrPositioning.get(), ExifTag.Image_Software.get(),
						ExifTag.Image_ResolutionUnit.get(), ExifTag.Image_Orientation.get(),
						ExifTag.Image_ExifTag.get() ) );

		// Read directly
		final Collection<String> imageSoftware = tags.get( ExifTag.Image_Software );
		assertThat( imageSoftware, Matchers.contains( "Picasa" ) );

		// Read using some helper methods
		assertThat( tags.getDate().orNull(),
				is( new Date( 116, Calendar.MARCH, 13, 11, 49, 14 ) ) );
		assertThat( tags.getOrientation().orNull(), is( ExifTags.Orientation.NORMAL ) );
		assertThat( tags.getXResolution().orNull(), is( 72 ) );
		assertThat( tags.getYResolution().orNull(), is( 72 ) );
		assertThat( tags.getResolutionUnit().orNull(), is( ExifTags.ResolutionUnit.INCHES ) );
		assertThat( tags.getMake().orNull(), is( "LG Electronics" ) );
		assertThat( tags.getModel().orNull(), is( "LG-H815" ) );
	}

	@Test
	public void testReadExistingSampleImageHasExpectedTags() throws IOException {
		// Read exif data, don't modify anything
		final ExifTags tags = ExifParser.read( sampleImage );

		assertThat( tags.keys(),
				containsInAnyOrder( ExifTag.Image_ExifTag.get(), ExifTag.Image_DateTime.get(),
						ExifTag.Image_Orientation.get(), ExifTag.Image_JPEGInterchangeFormat.get(),
						ExifTag.Image_JPEGInterchangeFormatLength.get(),
						ExifTag.Image_Compression.get(), ExifTag.Image_YCbCrPositioning.get(),
						ExifTag.Image_Make.get(), ExifTag.Image_Model.get(),
						ExifTag.Image_XResolution.get(), ExifTag.Image_YResolution.get(),
						ExifTag.Image_ResolutionUnit.get() ) );

		// Read directly
		assertThat( tags.get( ExifTag.Image_Software ), emptyIterable() );

		// Read using some helper methods
		assertThat( tags.getDate().orNull(),
				is( new Date( 103, Calendar.DECEMBER, 14, 12, 1, 44 ) ) );
		assertThat( tags.getOrientation().orNull(), is( ExifTags.Orientation.NORMAL ) );
		assertThat( tags.getXResolution().orNull(), is( 180 ) );
		assertThat( tags.getYResolution().orNull(), is( 180 ) );
		assertThat( tags.getResolutionUnit().orNull(), is( ExifTags.ResolutionUnit.INCHES ) );
		assertThat( tags.getMake().orNull(), is( "Canon" ) );
		assertThat( tags.getModel().orNull(), is( "Canon PowerShot S40" ) );
	}

	@Test
	public void testUpdateWritesToOriginalFile() throws IOException {
		final String test = String.format( "test2_%s", new Date() );

		final long originalFileSize = Files.size( sampleImage.toPath() );

		// Update the exif data, overwriting the existing file
		ExifParser.update( sampleImage, new ExifParser.Updater() {
			@Override
			public void update( final ExifTags tags ) {
				tags.set( ExifTag.Image_Make, test );
			}
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
		ExifParser.update( diggerImage, new ExifParser.Updater() {
			@Override
			public void update( final ExifTags tags ) {
				tags.set( ExifTag.Image_Make, test );
			}
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

		// When we set the tags we also set the ExifTag tag
		assertThat( actualTags.keys(), Matchers.containsInAnyOrder( ExifTag.Image_Make.get(),
				ExifTag.Image_ExifTag.get() ) );
		final Collection<String> imageMake = actualTags.get( ExifTag.Image_Make );
		assertThat( imageMake, containsInAnyOrder( test ) );
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
