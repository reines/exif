package com.furnaghan.exif;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Set;

import com.furnaghan.exif.io.StreamReader;
import com.furnaghan.exif.io.StreamWriter;
import com.google.common.collect.Sets;

public class JpegParser {

	public interface SegmentProcessor {
		byte[] process( final int marker, final byte[] data ) throws IOException;
	}

	// Start of Image
	private static final int SOI_MARKER = 0xFFD8;
	// End of Image
	private static final int EOI_MARKER = 0xFFD9;
	// Start of Scan
	private static final int SOS_MARKER = 0xFFDA;

	private final SegmentProcessor processor;
	private final Set<Integer> requiredSegments;

	public JpegParser( final SegmentProcessor processor ) {
		this( processor, Collections.emptySet() );
	}

	public JpegParser( final SegmentProcessor processor, final Set<Integer> requiredSegments ) {
		this.processor = processor;
		this.requiredSegments = requiredSegments;
	}

	public void process( final InputStream input, final OutputStream output ) throws IOException {
		final StreamReader in = new StreamReader( input, ByteOrder.BIG_ENDIAN );
		final StreamWriter out = new StreamWriter( output, ByteOrder.BIG_ENDIAN );

		final Set<Integer> markers = Sets.newHashSet();

		checkState( in.readMarker() == SOI_MARKER );
		out.writeMarker( SOI_MARKER );

		while ( in.available() ) {
			final int marker = in.readMarker();
			markers.add( marker );

			if ( marker == EOI_MARKER || marker == SOS_MARKER ) {
				for ( final int newMarker : Sets.difference( requiredSegments, markers ) ) {
					processSegment( out, newMarker, new byte[0] );
				}

				processImage( out, marker, in.readBytes() );
				break;
			}

			final int length = in.readShort();
			processSegment( out, marker, in.readBytes( length - 2 ) );
		}
	}

	private void processSegment( final StreamWriter out, final int marker, final byte[] input )
			throws IOException {
		final byte[] bytes = processor.process( marker, input );
		if ( bytes.length > 0 ) {
			out.writeMarker( marker );
			out.writeShort( bytes.length + 2 );
			out.writeBytes( bytes );
		}
	}

	private void processImage( final StreamWriter out, final int marker, final byte[] input )
			throws IOException {
		final byte[] bytes = processor.process( marker, input );
		out.writeMarker( marker );
		out.writeBytes( bytes );
	}
}
