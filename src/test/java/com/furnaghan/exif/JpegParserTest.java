package com.furnaghan.exif;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.furnaghan.exif.io.NoopOutputStream;
import com.furnaghan.exif.jpeg.Marker;

public class JpegParserTest {

	@Test
	public void test() throws IOException {
		final JpegParser parser = new JpegParser( new JpegParser.SegmentProcessor() {
			@Override
			public byte[] process( final Marker marker, final byte[] data ) {
				System.out.println( String.format( "Marker %s(%d)", marker, data.length ) );
				return data;
			}
		} );
		try ( final InputStream in = JpegParserTest.class.getResourceAsStream(
				"/images/sample.jpg" ) ) {
			parser.process( in, new NoopOutputStream() );
		}
	}
}
