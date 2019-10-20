# Exif
[![Build Status](https://api.travis-ci.org/reines/exif.png?branch=master)](https://travis-ci.org/reines/exif?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.furnaghan/exif/badge.png)](https://maven-badges.herokuapp.com/maven-central/com.furnaghan/exif)

A simple Exif parser for Java.

```xml
<dependency>
    <groupId>com.furnaghan</groupId>
    <artifactId>exif</artifactId>
    <version>...</version>
</dependency>
```

Release versions are deployed to Maven Central, development versions are available via OSS Sonatype.

```xml
<repositories>
    <repository>
        <id>oss-sonatype</id>
        <name>oss-sonatype</name>
        <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

## Usage

### Read the tags in an image.

```java
final ExifTags tags = ExifParser.read( targetFile );

// Using some helper methods for common tags
System.out.println( tags.getDate() );
System.out.println( tags.getOrientation() );

// Less common tags via ExifTag enum.
System.out.println( tags.get( Image.ISOSpeedRatings ) );
```

#### Update the tags in an image.

```java
ExifParser.update( targetFile, tags -> {
    tags.setDate( new Date() );
    tags.setOrientation( ExifTags.Orientation.NORMAL );
    return tags;
} );
```

#### Write new tags to an image.

```java
final ExifTags tags = ExifTags.empty();

// Using some helper methods for common tags
tags.setDate( new Date() );
tags.setOrientation( ExifTags.Orientation.NORMAL );

// Less common tags via ExifTag enum.
tags.add( Image.ISOSpeedRatings, 17 );

ExifParser.write( targetFile, tags );
```

## License

Released under the [Apache 2.0 License](LICENSE).
