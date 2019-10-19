package com.furnaghan.exif;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.furnaghan.exif.io.FieldType;
import com.google.common.base.Optional;

public class ExifTagReference {

	private static final Map<ExifTagReference, String> tagNames = new HashMap<>();

	public static void register( final ExifTagReference reference, final String name ) {
		tagNames.put( reference, name );
	}

	private final int id;
	private final ImageFileDirectory ifd;
	private final FieldType type;

	public ExifTagReference( final int id, final ImageFileDirectory ifd, final FieldType type ) {
		this.id = id;
		this.ifd = ifd;
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public ImageFileDirectory getIfd() {
		return ifd;
	}

	public FieldType getType() {
		return type;
	}

	public Optional<String> getName() {
		return Optional.fromNullable( tagNames.get( this ) );
	}

	@Override
	public String toString() {
		final String name = getName().or( "unknown" );
		return "ExifTagReference{" + "id=" + id + ", idf=" + ifd + ", type=" + type + ", name=" + name + "}";
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
		return id == that.id && ifd == that.ifd;
	}

	@Override
	public int hashCode() {
		return Objects.hash( id, ifd );
	}
}
