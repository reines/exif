package com.furnaghan.exif;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.furnaghan.exif.io.StreamReader;
import com.furnaghan.exif.io.StreamWriter;
import com.furnaghan.exif.jpeg.Marker;
import com.google.common.collect.Sets;

public class JpegParser {

	public interface SegmentProcessor {
		byte[] process( final Marker marker, final byte[] data ) throws IOException;
	}

	private static final Logger LOG = LoggerFactory.getLogger( JpegParser.class );

	private final SegmentProcessor processor;
	private final Set<Marker> requiredSegments;

	public JpegParser( final SegmentProcessor processor ) {
		this( processor, Collections.<Marker>emptySet() );
	}

	public JpegParser( final SegmentProcessor processor, final Set<Marker> requiredSegments ) {
		this.processor = processor;
		this.requiredSegments = requiredSegments;
	}

	public void process( final InputStream input, final OutputStream output ) throws IOException {
		final StreamReader in = new StreamReader( input, ByteOrder.BIG_ENDIAN );
		final StreamWriter out = new StreamWriter( output, ByteOrder.BIG_ENDIAN );

		final Set<Marker> markers = new HashSet<>();

		checkState( in.readMarker() == Marker.SOI );
		out.writeMarker( Marker.SOI );

		while ( in.available() ) {
			final Marker marker = in.readMarker();
			markers.add( marker );

			if ( marker == Marker.EOI || marker == Marker.SOS ) {
				for ( final Marker newMarker : Sets.difference( requiredSegments, markers ) ) {
					processSegment( out, newMarker, new byte[0] );
				}

				processImage( out, marker, in.readBytes() );
				break;
			}

			final int length = in.readShort();
			processSegment( out, marker, in.readBytes( length - 2 ) );
		}
	}

	private void processSegment( final StreamWriter out, final Marker marker, final byte[] input )
			throws IOException {
		final byte[] bytes = processor.process( marker, input );
		if ( bytes.length > 0 ) {
			LOG.info( "Writing {} bytes at segment {}", bytes.length, marker );
			out.writeMarker( marker );
			out.writeShort( bytes.length + 2 );
			out.writeBytes( bytes );
		}
	}

	private void processImage( final StreamWriter out, final Marker marker, final byte[] input )
			throws IOException {
		final byte[] bytes = processor.process( marker, input );
		LOG.info( "Writing {} bytes at segment {}", bytes.length, marker );
		out.writeMarker( marker );
		out.writeBytes( bytes );
	}
}
