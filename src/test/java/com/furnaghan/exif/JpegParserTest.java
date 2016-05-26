package com.furnaghan.exif;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.furnaghan.exif.io.NoopOutputStream;

public class JpegParserTest {

	@Test
	public void test() throws IOException {
		final JpegParser parser = new JpegParser( new JpegParser.SegmentProcessor() {
			@Override
			public byte[] process( final int marker, final byte[] data ) {
				System.out.println( String.format( "Marker %s(%d)", Integer.toHexString( marker ),
						data.length ) );
				return data;
			}
		} );
		try ( final InputStream in = JpegParserTest.class.getResourceAsStream( "/images/img_1771.jpg" ) ) {
			parser.process( in, new NoopOutputStream() );
		}
	}
}
