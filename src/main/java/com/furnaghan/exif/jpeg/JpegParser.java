package com.furnaghan.exif.jpeg;

import static com.google.common.base.Preconditions.checkState;

import java.io.ByteArrayInputStream;
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
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;

public class JpegParser {

	public interface SegmentProcessor {
		InputStream process( final Marker marker, final InputStream in ) throws IOException;
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
					processSegment( out, newMarker, new ByteArrayInputStream( new byte[0] ) );
				}

				processImage( out, marker, in.stream() );
				break;
			}

			final int length = in.readShort();
			processSegment( out, marker, in.limit( length - 2 ) );
		}
	}

	private void processSegment( final StreamWriter out, final Marker marker, final InputStream in )
			throws IOException {
		final byte[] bytes = ByteStreams.toByteArray( processor.process( marker, in ) );
		if ( bytes.length > 0 ) {
			LOG.info( "Writing {} bytes at segment {}", bytes.length, marker );
			out.writeMarker( marker );
			out.writeShort( bytes.length + 2 );
			out.writeBytes( bytes );
		}
	}

	private void processImage( final StreamWriter out, final Marker marker, final InputStream in )
			throws IOException {
		final InputStream bytes = processor.process( marker, in );
		LOG.info( "Writing stream of bytes at segment {}", marker );
		out.writeMarker( marker );
		out.writeStream( bytes );
	}
}
