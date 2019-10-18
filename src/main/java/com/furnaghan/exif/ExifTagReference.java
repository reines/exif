package com.furnaghan.exif;

import java.util.Objects;

import com.furnaghan.exif.io.FieldType;

public class ExifTagReference {

	private final int id;
	private final FieldType type;

	public ExifTagReference( final int id, final FieldType type ) {
		this.id = id;
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public FieldType getType() {
		return type;
	}

	@Override
	public boolean equals( final Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( !( o instanceof ExifTagReference ) ) {
			return false;
		}
		final ExifTagReference that = (ExifTagReference) o;
		return id == that.id && type == that.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash( id, type );
	}
}
