package com.furnaghan.exif.math;

import java.util.Objects;

public class Rational extends Number {

	public static Rational real( final int value ) {
		return new Rational( value, 1 );
	}

	public static Rational rational( final int numerator, final int denominator ) {
		return new Rational( numerator, denominator );
	}

	private final int numerator;
	private final int denominator;

	private Rational( final int numerator, final int denominator ) {
		this.numerator = numerator;
		this.denominator = denominator;
	}

	public int getNumerator() {
		return numerator;
	}

	public int getDenominator() {
		return denominator;
	}

	@Override
	public int intValue() {
		return numerator / denominator;
	}

	@Override
	public long longValue() {
		return numerator / denominator;
	}

	@Override
	public float floatValue() {
		return (float) numerator / (float) denominator;
	}

	@Override
	public double doubleValue() {
		return (double) numerator / (double) denominator;
	}

	@Override
	public boolean equals( final Object o ) {
		if ( this == o )
			return true;
		if ( !( o instanceof Rational ) )
			return false;
		final Rational rational = (Rational) o;
		return numerator == rational.numerator && denominator == rational.denominator;
	}

	@Override
	public int hashCode() {
		return Objects.hash( numerator, denominator );
	}

	@Override
	public String toString() {
		return String.format( "%d/%d", numerator, denominator );
	}
}
