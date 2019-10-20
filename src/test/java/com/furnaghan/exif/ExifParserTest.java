package com.furnaghan.exif;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.furnaghan.exif.parser.ExifParser;
import com.furnaghan.exif.tag.Exif;
import com.furnaghan.exif.tag.GPSInfo;
import com.furnaghan.exif.tag.Image;
import com.furnaghan.exif.tag.Iop;
import com.furnaghan.exif.tag.Thumbnail;
import com.google.common.base.Supplier;

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

	// TODO: remove
	@Test
	public void bla() throws IOException {
		try ( final InputStream in = new FileInputStream( sampleImage ) ) {
			try ( final OutputStream out = new FileOutputStream(
					"/Users/jamiefurnaghan/Desktop/bla.jpg" ) ) {
				ExifParser.copy( in, out );
			}
		}
	}

	@Test
	public void testReadExistingImagePicasaHasExpectedTags() throws IOException {
		// Read exif data, don't modify anything
		final ExifTags tags = ExifParser.read( diggerImage );

		// @formatter:off
		assertDirectoryContains( tags, ImageFileDirectory.Image, Image.XResolution, Image.Model, Image.YResolution, Image.DateTime, Image.Make, Image.YCbCrPositioning, Image.Software, Image.ResolutionUnit, Image.Orientation );
		assertDirectoryContains( tags, ImageFileDirectory.Thumbnail, Thumbnail.XResolution, Thumbnail.YResolution, Thumbnail.Compression, Thumbnail.ResolutionUnit );
		assertDirectoryContains( tags, ImageFileDirectory.Exif, Exif.ColorSpace, Exif.ExposureTime, Exif.SubSecTimeOriginal, Exif.FlashpixVersion, Exif.ExifVersion, Exif.ShutterSpeedValue, Exif.PixelXDimension, Exif.PixelYDimension, Exif.WhiteBalance, Exif.MeteringMode, Exif.DateTimeDigitized, Exif.SubSecTime, Exif.SceneType, Exif.ComponentsConfiguration, Exif.SensingMethod, Exif.DigitalZoomRatio, Exif.FNumber, Exif.ExposureMode, Exif.ISOSpeedRatings, Exif.SceneCaptureType, Exif.UserComment, Exif.BrightnessValue, Exif.ImageUniqueID, Exif.ExposureProgram, Exif.SubSecTimeDigitized, Exif.Flash, Exif.FocalLength, Exif.ExposureBiasValue, Exif.ApertureValue, Exif.DateTimeOriginal );
		assertDirectoryContains( tags, ImageFileDirectory.Iop, Iop.InteroperabilityVersion, Iop.InteroperabilityIndex );
		assertDirectoryContains( tags, ImageFileDirectory.GPSInfo, GPSInfo.GPSDateStamp, GPSInfo.GPSLongitude, GPSInfo.GPSLatitude, GPSInfo.GPSVersionID, GPSInfo.GPSTimeStamp, GPSInfo.GPSAltitude, GPSInfo.GPSLongitudeRef, GPSInfo.GPSLatitudeRef, GPSInfo.GPSAltitudeRef );
		assertThat( tags.hasThumbnails(), is( true ) );
		// @formatter:on

		// Read directly
		final Collection<String> imageSoftware = tags.get( Image.Software );
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

		// @formatter:off
		assertDirectoryContains( tags, ImageFileDirectory.Image, Image.DateTime, Image.Orientation, Image.Make, Image.Model, Image.XResolution, Image.YResolution, Image.ResolutionUnit, Image.YCbCrPositioning );
		assertDirectoryContains( tags, ImageFileDirectory.Thumbnail, Thumbnail.XResolution, Thumbnail.YResolution, Thumbnail.Compression, Thumbnail.ResolutionUnit );
		assertDirectoryContains( tags, ImageFileDirectory.Iop, Iop.RelatedImageWidth, Iop.RelatedImageLength, Iop.InteroperabilityIndex, Iop.InteroperabilityVersion );
		assertDirectoryContains( tags, ImageFileDirectory.Exif, Exif.Flash, Exif.MeteringMode, Exif.WhiteBalance, Exif.ShutterSpeedValue, Exif.DigitalZoomRatio, Exif.CompressedBitsPerPixel, Exif.ApertureValue, Exif.ComponentsConfiguration, Exif.ColorSpace, Exif.CustomRendered, Exif.ExposureMode, Exif.FocalPlaneResolutionUnit, Exif.FocalPlaneXResolution, Exif.FocalPlaneYResolution, Exif.MaxApertureValue, Exif.ExposureTime, Exif.SceneCaptureType, Exif.DateTimeOriginal, Exif.UserComment, Exif.FileSource, Exif.FlashpixVersion, Exif.ExifVersion, Exif.PixelXDimension, Exif.PixelYDimension, Exif.MakerNote, Exif.DateTimeDigitized, Exif.ExposureBiasValue, Exif.FocalLength, Exif.SensingMethod, Exif.FNumber );
		assertThat( tags.hasThumbnails(), is( true ) );
		// @formatter:on

		// Read directly
		assertThat( tags.get( Image.Software ), emptyIterable() );

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
			public ExifTags update( final ExifTags tags ) {
				tags.set( Image.Make, test );
				return tags;
			}
		} );

		// Check the file size changed, this is a rough indication that we actually did modify the file.
		final long newFileSize = Files.size( sampleImage.toPath() );
		assertThat( newFileSize, not( originalFileSize ) );

		// Check that the make tag is what we set it to be
		final Collection<String> make = ExifParser.read( sampleImage ).get( Image.Make );
		assertThat( make, Matchers.contains( test ) );
	}

	@Test
	public void testUpdateWithUnknownSegmentRetainsIt() throws IOException {
		final String test = String.format( "test2_%s", new Date() );

		final long originalFileSize = Files.size( diggerImage.toPath() );

		// Update the exif data, overwriting the existing file
		ExifParser.update( diggerImage, new ExifParser.Updater() {
			@Override
			public ExifTags update( final ExifTags tags ) {
				tags.set( Image.Make, test );
				return tags;
			}
		} );

		// Check that the make tag is what we set it to be
		final Collection<String> make = ExifParser.read( diggerImage ).get( Image.Make );
		assertThat( make, Matchers.contains( test ) );

		// Check the file size changed, this is a rough indication that we actually did modify the file.
		final long newFileSize = Files.size( diggerImage.toPath() );
		assertThat( newFileSize, not( originalFileSize ) );
	}

	@Test
	public void testSet() throws IOException {
		final String test = String.format( "test2_%s", new Date() );

		final long originalFileSize = Files.size( sampleImage.toPath() );

		// Create the exif data ourselves
		final ExifTags tags = ExifTags.empty();
		tags.set( Image.Make, test );

		// Set the exif data, throwing away any existing, and overwriting the existing file
		ExifParser.write( sampleImage, tags );

		// Check the file size changed, this is a rough indication that we actually did modify the file.
		final long newFileSize = Files.size( sampleImage.toPath() );
		assertThat( newFileSize, not( originalFileSize ) );

		// Check that the date tag is what we set it to be
		final ExifTags actualTags = ExifParser.read( sampleImage );

		final Collection<String> imageMake = actualTags.get( Image.Make );
		assertThat( imageMake, containsInAnyOrder( test ) );
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetWrongDataTypeForNumericField() throws IOException {
		final ExifTags tags = ExifTags.empty();
		tags.set( Image.Orientation, "test" );
		ExifParser.write( sampleImage, tags );
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetWrongDataTypeForStringField() throws IOException {
		final ExifTags tags = ExifTags.empty();
		tags.set( Image.DateTime, new byte[0] );
		ExifParser.write( sampleImage, tags );
	}

	@SuppressWarnings("unchecked")
	private static void assertDirectoryContains( final ExifTags exif, final ImageFileDirectory ifd,
			final Supplier<ExifTagReference>... tags ) {
		final Matcher<ExifTagReference>[] matchers = new Matcher[tags.length];
		for ( int i = 0; i < tags.length; i++ ) {
			matchers[i] = is( tags[i].get() );
		}
		assertThat( exif.getDirectory( ifd ).keySet(), containsInAnyOrder( matchers ) );
	}
}
