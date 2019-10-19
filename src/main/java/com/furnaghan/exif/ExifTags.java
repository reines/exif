package com.furnaghan.exif;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.furnaghan.exif.math.Rational;
import com.furnaghan.exif.tag.Image;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;

public class ExifTags {

	private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat( "yyyy:MM:dd HH:mm:ss" );
		}
	};

	public static ExifTags empty() {
		return new ExifTags( HashMultimap.<ExifTagReference, Object>create() );
	}

	private static void validateType( final ExifTagReference tag, final Object value ) {
		final Set<Class<?>> expected = tag.getType().getTypes();
		final Class<?> actual = value.getClass();

		for ( final Class<?> type : expected ) {
			if ( type.isAssignableFrom( actual ) ) {
				return;
			}
		}

		throw new IllegalArgumentException(
				String.format( "%s must be of type %s, given %s", tag, expected,
						actual.getSimpleName() ) );
	}

	private final Multimap<ExifTagReference, Object> tags;

	private ExifTags( final Multimap<ExifTagReference, Object> tags ) {
		this.tags = tags;
	}

	public int count() {
		return tags.size();
	}

	public Table<ImageFileDirectory, ExifTagReference, Collection<Object>> asTable() {
		final Table<ImageFileDirectory, ExifTagReference, Collection<Object>> table = HashBasedTable
				.create();

		for ( final Map.Entry<ExifTagReference, Collection<Object>> entry : tags.asMap()
				.entrySet() ) {
			final ExifTagReference tag = entry.getKey();
			final Collection<Object> values = entry.getValue();

			table.put( tag.getIfd(), tag, values );
		}

		return Tables.unmodifiableTable( table );
	}

	public synchronized ExifTags add( final Supplier<ExifTagReference> supplier,
			final Object value ) {
		return add( supplier.get(), value );
	}

	public synchronized ExifTags add( final ExifTagReference tag, final Object value ) {
		validateType( tag, value );
		tags.put( tag, value );
		return this;
	}

	public synchronized ExifTags addAll( final Supplier<ExifTagReference> supplier,
			final Iterable<?> values ) {
		return addAll( supplier.get(), values );
	}

	public synchronized ExifTags addAll( final ExifTagReference tag, final Iterable<?> values ) {
		for ( final Object value : values ) {
			add( tag, value );
		}
		return this;
	}

	public synchronized ExifTags set( final Supplier<ExifTagReference> supplier,
			final Object value ) {
		return set( supplier.get(), value );
	}

	public synchronized ExifTags set( final ExifTagReference tag, final Object value ) {
		validateType( tag, value );

		tags.removeAll( tag );
		tags.put( tag, value );
		return this;
	}

	public boolean isEmpty() {
		return tags.isEmpty();
	}

	public Set<ExifTagReference> keys() {
		return Collections.unmodifiableSet( tags.keySet() );
	}

	public boolean contains( final Supplier<ExifTagReference> supplier ) {
		return contains( supplier.get() );
	}

	public boolean contains( final ExifTagReference tag ) {
		return tags.containsKey( tag );
	}

	public <T> Collection<T> remove( final Supplier<ExifTagReference> supplier ) {
		return remove( supplier.get() );
	}

	@SuppressWarnings("unchecked")
	public <T> Collection<T> remove( final ExifTagReference tag ) {
		return (Collection<T>) tags.removeAll( tag );
	}

	public <T> Collection<T> get( final Supplier<ExifTagReference> supplier ) {
		return get( supplier.get() );
	}

	@SuppressWarnings("unchecked")
	public <T> Collection<T> get( final ExifTagReference tag ) {
		return (Collection<T>) tags.get( tag );
	}

	public <T> Optional<T> getFirst( final Supplier<ExifTagReference> supplier ) {
		return getFirst( supplier.get() );
	}

	public <T> Optional<T> getFirst( final ExifTagReference tag ) {
		final Collection<T> values = get( tag );
		return Optional.fromNullable( Iterables.getFirst( values, null ) );
	}

	public synchronized ExifTags clear() {
		tags.clear();
		return this;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		for ( final Map.Entry<ExifTagReference, Collection<Object>> entry : tags.asMap()
				.entrySet() ) {
			builder.append( entry.getKey() );
			builder.append( ": " );
			builder.append( Joiner.on( ',' ).join( entry.getValue() ) );
			builder.append( '\n' );
		}
		return builder.toString().trim();
	}

	// Helpers for common tags

	public Optional<Orientation> getOrientation() {
		final Optional<Integer> value = getFirst( Image.Orientation );
		return value.transform( new Function<Integer, Orientation>() {
			@Override
			public Orientation apply( final Integer integer ) {
				return Orientation.fromValue( integer );
			}
		} );
	}

	public ExifTags setOrientation( final Orientation orientation ) {
		set( Image.Orientation, orientation.value );
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
		final Optional<Rational> xResolution = getFirst( Image.XResolution );
		return xResolution.transform( new Function<Rational, Integer>() {
			@Override
			public Integer apply( final Rational rational ) {
				return rational.intValue();
			}
		} );
	}

	public Optional<Integer> getYResolution() {
		final Optional<Rational> yResolution = getFirst( Image.YResolution );
		return yResolution.transform( new Function<Rational, Integer>() {
			@Override
			public Integer apply( final Rational rational ) {
				return rational.intValue();
			}
		} );
	}

	public Optional<ResolutionUnit> getResolutionUnit() {
		final Optional<Integer> value = getFirst( Image.ResolutionUnit );
		return value.transform( new Function<Integer, ResolutionUnit>() {
			@Override
			public ResolutionUnit apply( final Integer integer ) {
				return ResolutionUnit.fromValue( integer );
			}
		} );
	}

	public ExifTags setResolution( final int x, final int y, final ResolutionUnit units ) {
		set( Image.XResolution, Rational.real( x ) );
		set( Image.YResolution, Rational.real( y ) );
		set( Image.ResolutionUnit, units.value );
		return this;
	}

	public Optional<String> getMake() {
		return getFirst( Image.Make );
	}

	public ExifTags setMake( final String make ) {
		return set( Image.Make, make );
	}

	public Optional<String> getModel() {
		return getFirst( Image.Model );
	}

	public ExifTags setModel( final String model ) {
		return set( Image.Model, model );
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
		final Optional<String> value = getFirst( Image.DateTime );
		return value.transform( new Function<String, Date>() {
			@Override
			public Date apply( final String s ) {
				try {
					return DATE_FORMAT.get().parse( s );
				} catch ( ParseException e ) {
					throw Throwables.propagate( e );
				}
			}
		} );
	}

	public ExifTags setDate( final Date date ) {
		set( Image.DateTime, DATE_FORMAT.get().format( date ) );
		return this;
	}
}
