package com.furnaghan.exif.io;

import java.io.OutputStream;

public class NoopOutputStream extends OutputStream {
	@Override
	public void write( final int b ) {
		/* no-op */
	}
}
