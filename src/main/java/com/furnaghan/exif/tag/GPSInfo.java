package com.furnaghan.exif.tag;

import com.furnaghan.exif.ExifTagReference;
import com.furnaghan.exif.ImageFileDirectory;
import com.furnaghan.exif.io.FieldType;
import com.google.common.base.Supplier;

public enum GPSInfo implements Supplier<ExifTagReference> {
	GPSVersionID( 0x0000, FieldType.Byte ), // Indicates the version of &lt;GPSInfoIFD&gt;. The version is given as 2.0.0.0. This tag is mandatory when &lt;GPSInfo&gt; tag is present. (Note: The &lt;GPSVersionID&gt; tag is given in bytes, unlike the &lt;ExifVersion&gt; tag. When the version is 2.0.0.0, the tag value is 02000000.H).
	GPSLatitudeRef( 0x0001, FieldType.Ascii ), // Indicates whether the latitude is north or south latitude. The ASCII value &#39;N&#39; indicates north latitude, and &#39;S&#39; is south latitude.
	GPSLatitude( 0x0002, FieldType.Rational ), // Indicates the latitude. The latitude is expressed as three RATIONAL values giving the degrees, minutes, and seconds, respectively. When degrees, minutes and seconds are expressed, the format is dd/1,mm/1,ss/1. When degrees and minutes are used and, for example, fractions of minutes are given up to two decimal places, the format is dd/1,mmmm/100,0/1.
	GPSLongitudeRef( 0x0003, FieldType.Ascii ), // Indicates whether the longitude is east or west longitude. ASCII &#39;E&#39; indicates east longitude, and &#39;W&#39; is west longitude.
	GPSLongitude( 0x0004, FieldType.Rational ), // Indicates the longitude. The longitude is expressed as three RATIONAL values giving the degrees, minutes, and seconds, respectively. When degrees, minutes and seconds are expressed, the format is ddd/1,mm/1,ss/1. When degrees and minutes are used and, for example, fractions of minutes are given up to two decimal places, the format is ddd/1,mmmm/100,0/1.
	GPSAltitudeRef( 0x0005, FieldType.Byte ), // Indicates the altitude used as the reference altitude. If the reference is sea level and the altitude is above sea level, 0 is given. If the altitude is below sea level, a value of 1 is given and the altitude is indicated as an absolute value in the GSPAltitude tag. The reference unit is meters. Note that this tag is BYTE type, unlike other reference tags.
	GPSAltitude( 0x0006, FieldType.Rational ), // Indicates the altitude based on the reference in GPSAltitudeRef. Altitude is expressed as one RATIONAL value. The reference unit is meters.
	GPSTimeStamp( 0x0007, FieldType.Rational ), // Indicates the time as UTC (Coordinated Universal Time). &lt;TimeStamp&gt; is expressed as three RATIONAL values giving the hour, minute, and second (atomic clock).
	GPSSatellites( 0x0008, FieldType.Ascii ), // Indicates the GPS satellites used for measurements. This tag can be used to describe the number of satellites, their ID number, angle of elevation, azimuth, SNR and other information in ASCII notation. The format is not specified. If the GPS receiver is incapable of taking measurements, value of the tag is set to NULL.
	GPSStatus( 0x0009, FieldType.Ascii ), // Indicates the status of the GPS receiver when the image is recorded. &#34;A&#34; means measurement is in progress, and &#34;V&#34; means the measurement is Interoperability.
	GPSMeasureMode( 0x000a, FieldType.Ascii ), // Indicates the GPS measurement mode. &#34;2&#34; means two-dimensional measurement and &#34;3&#34; means three-dimensional measurement is in progress.
	GPSDOP( 0x000b, FieldType.Rational ), // Indicates the GPS DOP (data degree of precision). An HDOP value is written during two-dimensional measurement, and PDOP during three-dimensional measurement.
	GPSSpeedRef( 0x000c, FieldType.Ascii ), // Indicates the unit used to express the GPS receiver speed of movement. &#34;K&#34; &#34;M&#34; and &#34;N&#34; represents kilometers per hour, miles per hour, and knots.
	GPSSpeed( 0x000d, FieldType.Rational ), // Indicates the speed of GPS receiver movement.
	GPSTrackRef( 0x000e, FieldType.Ascii ), // Indicates the reference for giving the direction of GPS receiver movement. &#34;T&#34; denotes true direction and &#34;M&#34; is magnetic direction.
	GPSTrack( 0x000f, FieldType.Rational ), // Indicates the direction of GPS receiver movement. The range of values is from 0.00 to 359.99.
	GPSImgDirectionRef( 0x0010, FieldType.Ascii ), // Indicates the reference for giving the direction of the image when it is captured. &#34;T&#34; denotes true direction and &#34;M&#34; is magnetic direction.
	GPSImgDirection( 0x0011, FieldType.Rational ), // Indicates the direction of the image when it was captured. The range of values is from 0.00 to 359.99.
	GPSMapDatum( 0x0012, FieldType.Ascii ), // Indicates the geodetic survey data used by the GPS receiver. If the survey data is restricted to Japan, the value of this tag is &#34;TOKYO&#34; or &#34;WGS-84&#34;.
	GPSDestLatitudeRef( 0x0013, FieldType.Ascii ), // Indicates whether the latitude of the destination point is north or south latitude. The ASCII value &#34;N&#34; indicates north latitude, and &#34;S&#34; is south latitude.
	GPSDestLatitude( 0x0014, FieldType.Rational ), // Indicates the latitude of the destination point. The latitude is expressed as three RATIONAL values giving the degrees, minutes, and seconds, respectively. If latitude is expressed as degrees, minutes and seconds, a typical format would be dd/1,mm/1,ss/1. When degrees and minutes are used and, for example, fractions of minutes are given up to two decimal places, the format would be dd/1,mmmm/100,0/1.
	GPSDestLongitudeRef( 0x0015, FieldType.Ascii ), // Indicates whether the longitude of the destination point is east or west longitude. ASCII &#34;E&#34; indicates east longitude, and &#34;W&#34; is west longitude.
	GPSDestLongitude( 0x0016, FieldType.Rational ), // Indicates the longitude of the destination point. The longitude is expressed as three RATIONAL values giving the degrees, minutes, and seconds, respectively. If longitude is expressed as degrees, minutes and seconds, a typical format would be ddd/1,mm/1,ss/1. When degrees and minutes are used and, for example, fractions of minutes are given up to two decimal places, the format would be ddd/1,mmmm/100,0/1.
	GPSDestBearingRef( 0x0017, FieldType.Ascii ), // Indicates the reference used for giving the bearing to the destination point. &#34;T&#34; denotes true direction and &#34;M&#34; is magnetic direction.
	GPSDestBearing( 0x0018, FieldType.Rational ), // Indicates the bearing to the destination point. The range of values is from 0.00 to 359.99.
	GPSDestDistanceRef( 0x0019, FieldType.Ascii ), // Indicates the unit used to express the distance to the destination point. &#34;K&#34;, &#34;M&#34; and &#34;N&#34; represent kilometers, miles and knots.
	GPSDestDistance( 0x001a, FieldType.Rational ), // Indicates the distance to the destination point.
	GPSProcessingMethod( 0x001b, FieldType.Undefined ), // A character string recording the name of the method used for location finding. The first byte indicates the character code used, and this is followed by the name of the method.
	GPSAreaInformation( 0x001c, FieldType.Undefined ), // A character string recording the name of the GPS area. The first byte indicates the character code used, and this is followed by the name of the GPS area.
	GPSDateStamp( 0x001d, FieldType.Ascii ), // A character string recording date and time information relative to UTC (Coordinated Universal Time). The format is &#34;YYYY:MM:DD.&#34;.
	GPSDifferential( 0x001e, FieldType.Short ), // Indicates whether differential correction is applied to the GPS receiver.
	;

	private final ExifTagReference reference;

	GPSInfo( final int id, final FieldType type ) {
		this.reference = new ExifTagReference( id, ImageFileDirectory.GPSInfo, type );
	}

	@Override
	public ExifTagReference get() {
		return reference;
	}
}
