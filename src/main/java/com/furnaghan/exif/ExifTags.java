package com.furnaghan.exif;

import static com.google.common.base.Preconditions.checkArgument;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.furnaghan.exif.math.Rational;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class ExifTags {

	private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = ThreadLocal.withInitial( ( ) -> new SimpleDateFormat( "yyyy:MM:dd HH:mm:ss" ) );

	public static ExifTags empty() {
		return new ExifTags( HashMultimap.create() );
	}

	private static void validateType( final ExifTag tag, final Object value ) {
		final Set<Class<?>> expected = tag.getType().getTypes();
		final Class<?> actual = value.getClass();

		final boolean valid = expected.stream().anyMatch( e -> e.isAssignableFrom( actual ) );
		checkArgument( valid, "%s must be of type %s, given %s", tag, expected,
				actual.getSimpleName() );
	}

	private final Multimap<ExifTag, Object> tags;

	private ExifTags( final Multimap<ExifTag, Object> tags ) {
		this.tags = tags;
	}

	public int count() {
		return tags.size();
	}

	public Collection<Map.Entry<ExifTag, Collection<Object>>> entries() {
		return tags.asMap().entrySet();
	}

	public synchronized ExifTags add( final ExifTag tag, final Object value ) {
		validateType( tag, value );
		tags.put( tag, value );
		return this;
	}

	public synchronized ExifTags addAll( final ExifTag tag, final Iterable<?> values ) {
		for ( final Object value : values ) {
			add( tag, value );
		}
		return this;
	}

	public synchronized ExifTags set( final ExifTag tag, final Object value ) {
		validateType( tag, value );

		tags.removeAll( tag );
		tags.put( tag, value );
		return this;
	}

	public boolean isEmpty() {
		return tags.isEmpty();
	}

	public Set<ExifTag> keys() {
		return Collections.unmodifiableSet( tags.keySet() );
	}

	public boolean contains( final ExifTag tag ) {
		return tags.containsKey( tag );
	}

	@SuppressWarnings("unchecked")
	public <T> Collection<T> get( final ExifTag tag ) {
		return (Collection<T>) tags.get( tag );
	}

	public <T> Optional<T> getFirst( final ExifTag tag ) {
		return Optional.ofNullable( Iterables.getFirst( get( tag ), null ) );
	}

	public synchronized ExifTags clear() {
		tags.clear();
		return this;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		for ( final Map.Entry<ExifTag, Collection<Object>> entry : tags.asMap().entrySet() ) {
			builder.append( entry.getKey() );
			builder.append( ": " );
			builder.append( Joiner.on( ',' ).join( entry.getValue() ) );
			builder.append( '\n' );
		}
		return builder.toString().trim();
	}

	// Helpers for common tags

	public Optional<Orientation> getOrientation() {
		final Optional<Integer> value = getFirst( ExifTag.Image_Orientation );
		return value.map( Orientation::fromValue );
	}

	public ExifTags setOrientation( final Orientation orientation ) {
		set( ExifTag.Image_Orientation, orientation.value );
		return this;
	}

	public enum Orientation {
		NORMAL( 0x1 ),
		FLIP_HORIZONTAL( 0x2 ),
		ROTATE_180( 0x3 ),
		FLIP_VERTICAL( 0x4 ),
		TRANSPOSE( 0x5 ),
		ROTATE_270( 0x6 ),
		TRANSVERSE( 0x7 ),
		ROTATE_90( 0x8 );

		private final int value;

		Orientation( final int value ) {
			this.value = value;
		}

		private static final Map<Integer, Orientation> orientationByValue = Maps.newHashMap();

		static {
			for ( final Orientation orientation : Orientation.values() ) {
				orientationByValue.put( orientation.value, orientation );
			}
		}

		private static Orientation fromValue( final int value ) {
			return orientationByValue.get( value );
		}
	}

	public Optional<Integer> getXResolution() {
		final Optional<Rational> xResolution = getFirst( ExifTag.Image_XResolution );
		return xResolution.map( Rational::intValue );
	}

	public Optional<Integer> getYResolution() {
		final Optional<Rational> yResolution = getFirst( ExifTag.Image_YResolution );
		return yResolution.map( Rational::intValue );
	}

	public Optional<ResolutionUnit> getResolutionUnit() {
		final Optional<Integer> value = getFirst( ExifTag.Image_ResolutionUnit );
		return value.map( ResolutionUnit::fromValue );
	}

	public ExifTags setResolution( final int x, final int y, final ResolutionUnit units ) {
		set( ExifTag.Image_XResolution, Rational.real( x ) );
		set( ExifTag.Image_YResolution, Rational.real( y ) );
		set( ExifTag.Image_ResolutionUnit, units.value );
		return this;
	}

	public Optional<String> getMake() {
		return getFirst( ExifTag.Image_Make );
	}

	public ExifTags setMake( final String make ) {
		return set( ExifTag.Image_Make, make );
	}

	public Optional<String> getModel() {
		return getFirst( ExifTag.Image_Model );
	}

	public ExifTags setModel( final String model ) {
		return set( ExifTag.Image_Model, model );
	}

	public enum ResolutionUnit {
		NONE( 0x1 ),
		INCHES( 0x2 ),
		CENTIMETERS( 0x3 );

		private final int value;

		ResolutionUnit( final int value ) {
			this.value = value;
		}

		private static final Map<Integer, ResolutionUnit> unitsByValue = Maps.newHashMap();

		static {
			for ( final ResolutionUnit unit : ResolutionUnit.values() ) {
				unitsByValue.put( unit.value, unit );
			}
		}

		private static ResolutionUnit fromValue( final int value ) {
			return unitsByValue.get( value );
		}
	}

	public Optional<Date> getDate() {
		final Optional<String> value = getFirst( ExifTag.Image_DateTime );
		return value.map( v -> {
			try {
				return DATE_FORMAT.get().parse( v );
			} catch ( ParseException e ) {
				throw Throwables.propagate( e );
			}
		} );
	}

	public ExifTags setDate( final Date date ) {
		set( ExifTag.Image_DateTime, DATE_FORMAT.get().format( date ) );
		return this;
	}
}
