package com.furnaghan.exif.tag;

import com.furnaghan.exif.ExifTagReference;
import com.furnaghan.exif.ImageFileDirectory;
import com.furnaghan.exif.io.FieldType;
import com.google.common.base.Supplier;

public enum Iop implements Supplier<ExifTagReference> {
	InteroperabilityIndex( 0x0001, FieldType.Ascii ), // Indicates the identification of the Interoperability rule. Use &#34;R98&#34; for stating ExifR98 Rules. Four bytes used including the termination code (NULL). see the separate volume of Recommended Exif Interoperability Rules (ExifR98) for other tags used for ExifR98.
	InteroperabilityVersion( 0x0002, FieldType.Undefined ), // Interoperability version
	RelatedImageFileFormat( 0x1000, FieldType.Ascii ), // File format of image file
	RelatedImageWidth( 0x1001, FieldType.Short ), // Image width
	RelatedImageLength( 0x1002, FieldType.Short ), // Image height
	;

	private final ExifTagReference reference;

	Iop( final int id, final FieldType type ) {
		this.reference = new ExifTagReference( id, ImageFileDirectory.Iop, type );
	}

	@Override
	public ExifTagReference get() {
		return reference;
	}
}
