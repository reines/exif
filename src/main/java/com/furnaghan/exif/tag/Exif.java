package com.furnaghan.exif.tag;

import com.furnaghan.exif.ExifTagReference;
import com.furnaghan.exif.ImageFileDirectory;
import com.furnaghan.exif.io.FieldType;
import com.google.common.base.Supplier;

public enum Exif implements Supplier<ExifTagReference> {
	ExposureTime( 0x829a, FieldType.Rational ), // Exposure time, given in seconds (sec).
	FNumber( 0x829d, FieldType.Rational ), // The F number.
	ExposureProgram( 0x8822, FieldType.Short ), // The class of the program used by the camera to set exposure when the picture is taken.
	SpectralSensitivity( 0x8824, FieldType.Ascii ), // Indicates the spectral sensitivity of each channel of the camera used. The tag value is an ASCII string compatible with the standard developed by the ASTM Technical Committee.
	ISOSpeedRatings( 0x8827, FieldType.Short ), // Indicates the ISO Speed and ISO Latitude of the camera or input device as specified in ISO 12232.
	OECF( 0x8828, FieldType.Undefined ), // Indicates the Opto-Electoric Conversion Function (OECF) specified in ISO 14524. &lt;OECF&gt; is the relationship between the camera optical input and the image values.
	SensitivityType( 0x8830, FieldType.Short ), // The SensitivityType tag indicates which one of the parameters of ISO12232 is the PhotographicSensitivity tag. Although it is an optional tag, it should be recorded when a PhotographicSensitivity tag is recorded. Value = 4, 5, 6, or 7 may be used in case that the values of plural parameters are the same.
	StandardOutputSensitivity( 0x8831, FieldType.Long ), // This tag indicates the standard output sensitivity value of a camera or input device defined in ISO 12232. When recording this tag, the PhotographicSensitivity and SensitivityType tags shall also be recorded.
	RecommendedExposureIndex( 0x8832, FieldType.Long ), // This tag indicates the recommended exposure index value of a camera or input device defined in ISO 12232. When recording this tag, the PhotographicSensitivity and SensitivityType tags shall also be recorded.
	ISOSpeed( 0x8833, FieldType.Long ), // This tag indicates the ISO speed value of a camera or input device that is defined in ISO 12232. When recording this tag, the PhotographicSensitivity and SensitivityType tags shall also be recorded.
	ISOSpeedLatitudeyyy( 0x8834, FieldType.Long ), // This tag indicates the ISO speed latitude yyy value of a camera or input device that is defined in ISO 12232. However, this tag shall not be recorded without ISOSpeed and ISOSpeedLatitudezzz.
	ISOSpeedLatitudezzz( 0x8835, FieldType.Long ), // This tag indicates the ISO speed latitude zzz value of a camera or input device that is defined in ISO 12232. However, this tag shall not be recorded without ISOSpeed and ISOSpeedLatitudeyyy.
	ExifVersion( 0x9000, FieldType.Undefined ), // The version of this standard supported. Nonexistence of this field is taken to mean nonconformance to the standard.
	DateTimeOriginal( 0x9003, FieldType.Ascii ), // The date and time when the original image data was generated. For a digital still camera the date and time the picture was taken are recorded.
	DateTimeDigitized( 0x9004, FieldType.Ascii ), // The date and time when the image was stored as digital data.
	ComponentsConfiguration( 0x9101, FieldType.Undefined ), // Information specific to compressed data. The channels of each component are arranged in order from the 1st component to the 4th. For uncompressed data the data arrangement is given in the &lt;PhotometricInterpretation&gt; tag. However, since &lt;PhotometricInterpretation&gt; can only express the order of Y, Cb and Cr, this tag is provided for cases when compressed data uses components other than Y, Cb, and Cr and to enable support of other sequences.
	CompressedBitsPerPixel( 0x9102, FieldType.Rational ), // Information specific to compressed data. The compression mode used for a compressed image is indicated in unit bits per pixel.
	ShutterSpeedValue( 0x9201, FieldType.SRational ), // Shutter speed. The unit is the APEX (Additive System of Photographic Exposure) setting.
	ApertureValue( 0x9202, FieldType.Rational ), // The lens aperture. The unit is the APEX value.
	BrightnessValue( 0x9203, FieldType.SRational ), // The value of brightness. The unit is the APEX value. Ordinarily it is given in the range of -99.99 to 99.99.
	ExposureBiasValue( 0x9204, FieldType.SRational ), // The exposure bias. The units is the APEX value. Ordinarily it is given in the range of -99.99 to 99.99.
	MaxApertureValue( 0x9205, FieldType.Rational ), // The smallest F number of the lens. The unit is the APEX value. Ordinarily it is given in the range of 00.00 to 99.99, but it is not limited to this range.
	SubjectDistance( 0x9206, FieldType.Rational ), // The distance to the subject, given in meters.
	MeteringMode( 0x9207, FieldType.Short ), // The metering mode.
	LightSource( 0x9208, FieldType.Short ), // The kind of light source.
	Flash( 0x9209, FieldType.Short ), // This tag is recorded when an image is taken using a strobe light (flash).
	FocalLength( 0x920a, FieldType.Rational ), // The actual focal length of the lens, in mm. Conversion is not made to the focal length of a 35 mm film camera.
	SubjectArea( 0x9214, FieldType.Short ), // This tag indicates the location and area of the main subject in the overall scene.
	MakerNote( 0x927c, FieldType.Undefined ), // A tag for manufacturers of Exif writers to record any desired information. The contents are up to the manufacturer.
	UserComment( 0x9286, FieldType.Undefined ), // A tag for Exif users to write keywords or comments on the image besides those in &lt;ImageDescription&gt;, and without the character code limitations of the &lt;ImageDescription&gt; tag.
	SubSecTime( 0x9290, FieldType.Ascii ), // A tag used to record fractions of seconds for the &lt;DateTime&gt; tag.
	SubSecTimeOriginal( 0x9291, FieldType.Ascii ), // A tag used to record fractions of seconds for the &lt;DateTimeOriginal&gt; tag.
	SubSecTimeDigitized( 0x9292, FieldType.Ascii ), // A tag used to record fractions of seconds for the &lt;DateTimeDigitized&gt; tag.
	FlashpixVersion( 0xa000, FieldType.Undefined ), // The FlashPix format version supported by a FPXR file.
	ColorSpace( 0xa001, FieldType.Short ), // The color space information tag is always recorded as the color space specifier. Normally sRGB is used to define the color space based on the PC monitor conditions and environment. If a color space other than sRGB is used, Uncalibrated is set. Image data recorded as Uncalibrated can be treated as sRGB when it is converted to FlashPix.
	PixelXDimension( 0xa002, FieldType.Short ), // Information specific to compressed data. When a compressed file is recorded, the valid width of the meaningful image must be recorded in this tag, whether or not there is padding data or a restart marker. This tag should not exist in an uncompressed file.
	PixelYDimension( 0xa003, FieldType.Short ), // Information specific to compressed data. When a compressed file is recorded, the valid height of the meaningful image must be recorded in this tag, whether or not there is padding data or a restart marker. This tag should not exist in an uncompressed file. Since data padding is unnecessary in the vertical direction, the number of lines recorded in this valid image height tag will in fact be the same as that recorded in the SOF.
	RelatedSoundFile( 0xa004, FieldType.Ascii ), // This tag is used to record the name of an audio file related to the image data. The only relational information recorded here is the Exif audio file name and extension (an ASCII string consisting of 8 characters + &#39;.&#39; + 3 characters). The path is not recorded.
	InteroperabilityTag( 0xa005, FieldType.Long ), // Interoperability IFD is composed of tags which stores the information to ensure the Interoperability and pointed by the following tag located in Exif IFD. The Interoperability structure of Interoperability IFD is the same as TIFF defined IFD structure but does not contain the image data characteristically compared with normal TIFF IFD.
	FlashEnergy( 0xa20b, FieldType.Rational ), // Indicates the strobe energy at the time the image is captured, as measured in Beam Candle Power Seconds (BCPS).
	SpatialFrequencyResponse( 0xa20c, FieldType.Undefined ), // This tag records the camera or input device spatial frequency table and SFR values in the direction of image width, image height, and diagonal direction, as specified in ISO 12233.
	FocalPlaneXResolution( 0xa20e, FieldType.Rational ), // Indicates the number of pixels in the image width (X) direction per &lt;FocalPlaneResolutionUnit&gt; on the camera focal plane.
	FocalPlaneYResolution( 0xa20f, FieldType.Rational ), // Indicates the number of pixels in the image height (V) direction per &lt;FocalPlaneResolutionUnit&gt; on the camera focal plane.
	FocalPlaneResolutionUnit( 0xa210, FieldType.Short ), // Indicates the unit for measuring &lt;FocalPlaneXResolution&gt; and &lt;FocalPlaneYResolution&gt;. This value is the same as the &lt;ResolutionUnit&gt;.
	SubjectLocation( 0xa214, FieldType.Short ), // Indicates the location of the main subject in the scene. The value of this tag represents the pixel at the center of the main subject relative to the left edge, prior to rotation processing as per the &lt;Rotation&gt; tag. The first value indicates the X column number and second indicates the Y row number.
	ExposureIndex( 0xa215, FieldType.Rational ), // Indicates the exposure index selected on the camera or input device at the time the image is captured.
	SensingMethod( 0xa217, FieldType.Short ), // Indicates the image sensor type on the camera or input device.
	FileSource( 0xa300, FieldType.Undefined ), // Indicates the image source. If a DSC recorded the image, this tag value of this tag always be set to 3, indicating that the image was recorded on a DSC.
	SceneType( 0xa301, FieldType.Undefined ), // Indicates the type of scene. If a DSC recorded the image, this tag value must always be set to 1, indicating that the image was directly photographed.
	CFAPattern( 0xa302, FieldType.Undefined ), // Indicates the color filter array (CFA) geometric pattern of the image sensor when a one-chip color area sensor is used. It does not apply to all sensing methods.
	CustomRendered( 0xa401, FieldType.Short ), // This tag indicates the use of special processing on image data, such as rendering geared to output. When special processing is performed, the reader is expected to disable or minimize any further processing.
	ExposureMode( 0xa402, FieldType.Short ), // This tag indicates the exposure mode set when the image was shot. In auto-bracketing mode, the camera shoots a series of frames of the same scene at different exposure settings.
	WhiteBalance( 0xa403, FieldType.Short ), // This tag indicates the white balance mode set when the image was shot.
	DigitalZoomRatio( 0xa404, FieldType.Rational ), // This tag indicates the digital zoom ratio when the image was shot. If the numerator of the recorded value is 0, this indicates that digital zoom was not used.
	FocalLengthIn35mmFilm( 0xa405, FieldType.Short ), // This tag indicates the equivalent focal length assuming a 35mm film camera, in mm. A value of 0 means the focal length is unknown. Note that this tag differs from the &lt;FocalLength&gt; tag.
	SceneCaptureType( 0xa406, FieldType.Short ), // This tag indicates the type of scene that was shot. It can also be used to record the mode in which the image was shot. Note that this differs from the &lt;SceneType&gt; tag.
	GainControl( 0xa407, FieldType.Short ), // This tag indicates the degree of overall image gain adjustment.
	Contrast( 0xa408, FieldType.Short ), // This tag indicates the direction of contrast processing applied by the camera when the image was shot.
	Saturation( 0xa409, FieldType.Short ), // This tag indicates the direction of saturation processing applied by the camera when the image was shot.
	Sharpness( 0xa40a, FieldType.Short ), // This tag indicates the direction of sharpness processing applied by the camera when the image was shot.
	DeviceSettingDescription( 0xa40b, FieldType.Undefined ), // This tag indicates information on the picture-taking conditions of a particular camera model. The tag is used only to indicate the picture-taking conditions in the reader.
	SubjectDistanceRange( 0xa40c, FieldType.Short ), // This tag indicates the distance to the subject.
	ImageUniqueID( 0xa420, FieldType.Ascii ), // This tag indicates an identifier assigned uniquely to each image. It is recorded as an ASCII string equivalent to hexadecimal notation and 128-bit fixed length.
	CameraOwnerName( 0xa430, FieldType.Ascii ), // This tag records the owner of a camera used in photography as an ASCII string.
	BodySerialNumber( 0xa431, FieldType.Ascii ), // This tag records the serial number of the body of the camera that was used in photography as an ASCII string.
	LensSpecification( 0xa432, FieldType.Rational ), // This tag notes minimum focal length, maximum focal length, minimum F number in the minimum focal length, and minimum F number in the maximum focal length, which are specification information for the lens that was used in photography. When the minimum F number is unknown, the notation is 0/0
	LensMake( 0xa433, FieldType.Ascii ), // This tag records the lens manufactor as an ASCII string.
	LensModel( 0xa434, FieldType.Ascii ), // This tag records the lens&#39;s model name and model number as an ASCII string.
	LensSerialNumber( 0xa435, FieldType.Ascii ), // This tag records the serial number of the interchangeable lens that was used in photography as an ASCII string.
	;

	private final ExifTagReference reference;

	Exif( final int id, final FieldType type ) {
		this.reference = new ExifTagReference( id, ImageFileDirectory.Exif, type );
	}

	@Override
	public ExifTagReference get() {
		return reference;
	}
}
