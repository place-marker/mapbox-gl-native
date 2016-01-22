package com.mapbox.mapboxsdk.geometry;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * An immutable class representing a latitude/longitude aligned rectangle.
 */
public class LatLngBounds implements Parcelable {

    private final double mLatNorth;
    private final double mLatSouth;
    private final double mLonEast;
    private final double mLonWest;

    private final boolean mIsValid;

    /**
     * Construct a new LatLngBounds based on its corners, given in NESW
     * order.
     *
     * @param northLatitude Northern Latitude
     * @param eastLongitude   Eastern Longitude
     * @param southLatitude Southern Latitude
     * @param westLongitude   Western Longitude
     */
    public LatLngBounds(final double northLatitude, final double eastLongitude, final double southLatitude, final double westLongitude) {
        this.mLatNorth = northLatitude;
        this.mLonEast = eastLongitude;
        this.mLatSouth = southLatitude;
        this.mLonWest = westLongitude;
        this.mIsValid = ((this.mLonWest < this.mLonEast) && (this.mLatNorth > this.mLatSouth));
    }

    /**
     * Construct a new LatLngBounds based on its corners, given in NESW order.
     *
     * @param northEast Coordinate
     * @param southWest Coordinate
     */
    public LatLngBounds(final LatLng northEast, final LatLng southWest) {
        this(northEast.getLatitude(), northEast.getLongitude(), southWest.getLatitude(), southWest.getLongitude());
    }

    /**
     * Create a LatLngBounds box from another LatLngBounds
     *
     * @param other the other LatLngBounds
     */
    public LatLngBounds(final LatLngBounds other) {
        this.mLatNorth = other.getLatNorth();
        this.mLonEast = other.getLonEast();
        this.mLatSouth = other.getLatSouth();
        this.mLonWest = other.getLonWest();
        this.mIsValid = other.isValid();
    }

    /**
     * Calculates the centerpoint of this LatLngBounds by simple interpolation and returns
     * it as a point. This is a non-geodesic calculation which is not the geographic center.
     *
     * @return LatLng center of this LatLngBounds
     */
    public LatLng getCenter() {
        return new LatLng((this.mLatNorth + this.mLatSouth) / 2,
                (this.mLonEast + this.mLonWest) / 2);
    }

    public double getLatNorth() {
        return this.mLatNorth;
    }

    public double getLatSouth() {
        return this.mLatSouth;
    }

    public double getLonEast() {
        return this.mLonEast;
    }

    public double getLonWest() {
        return this.mLonWest;
    }

    public boolean isValid() {
        return this.mIsValid;
    }

    /**
     * Get the area spanned by this LatLngBounds
     *
     * @return CoordinateSpan area
     */
    public CoordinateSpan getSpan() {
        return new CoordinateSpan(getLatitudeSpan(), getLongitudeSpan());
    }

    /**
     * Get the absolute distance, in degrees, between the north and
     * south boundaries of this LatLngBounds
     *
     * @return Span distance
     */
    public double getLatitudeSpan() {
        return Math.abs(this.mLatNorth - this.mLatSouth);
    }

    /**
     * Get the absolute distance, in degrees, between the west and
     * east boundaries of this LatLngBounds
     *
     * @return Span distance
     */
    public double getLongitudeSpan() {
        return Math.abs(this.mLonEast - this.mLonWest);
    }


    /**
     * Validate if LatLngBounds is empty, determined if absolute distance is
     *
     * @return boolean indicating if span is empty
     */
    public boolean isEmpty() {
        return getLongitudeSpan() == 0.0 || getLatitudeSpan() == 0.0;
    }

    @Override
    public String toString() {
        return "N:" + this.mLatNorth + "; E:" + this.mLonEast + "; S:" + this.mLatSouth + "; W:" + this.mLonWest;
    }

    /**
     * Constructs a LatLngBounds that contains all of a list of LatLng
     * objects. Empty lists will yield invalid LatLngBounds.
     *
     * @param latLngs List of LatLng objects
     * @return LatLngBounds
     */
    public static LatLngBounds fromLatLngs(final List<? extends ILatLng> latLngs) {
        double minLat = 90,
                minLon = 180,
                maxLat = -90,
                maxLon = -180;

        for (final ILatLng gp : latLngs) {
            final double latitude = gp.getLatitude();
            final double longitude = gp.getLongitude();

            minLat = Math.min(minLat, latitude);
            minLon = Math.min(minLon, longitude);
            maxLat = Math.max(maxLat, latitude);
            maxLon = Math.max(maxLon, longitude);
        }

        return new LatLngBounds(maxLat, maxLon, minLat, minLon);
    }

    /**
     * Determines whether this LatLngBounds matches another one via LatLng.
     *
     * @param o another object
     * @return a boolean indicating whether the LatLngBounds are equal
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o instanceof BoundingBox) {
            BoundingBox other = (BoundingBox) o;
            return mLatNorth == other.getLatNorth()
                    && mLatSouth == other.getLatSouth()
                    && mLonEast == other.getLonEast()
                    && mLonWest == other.getLonWest();
        }
        return false;
    }

    /**
     * Determines whether this LatLngBounds contains a point and the point
     * does not touch its boundary.
     *
     * @param latLng the point which may be contained
     * @return true, if the point is contained within the box.
     */
    public boolean contains(final ILatLng latLng) {
        final double latitude = latLng.getLatitude();
        final double longitude = latLng.getLongitude();
        return ((latitude < this.mLatNorth)
                && (latitude > this.mLatSouth))
                && ((longitude < this.mLonEast)
                && (longitude > this.mLonWest));
    }

    /**
     * Returns a new LatLngBounds that stretches to contain both this and another LatLngBounds.
     *
     * @param bounds LatLngBounds to add
     * @return LatLngBounds
     */
    public LatLngBounds union(LatLngBounds bounds) {
        return union(bounds.getLatNorth(), bounds.getLonEast(), bounds.getLatSouth(), bounds.getLonWest());
    }

    /**
     * Returns a new LatLngBounds that stretches to include another LatLngBounds,
     * given by corner points.
     *
     * @param lonNorth Northern Longitude
     * @param latEast  Eastern Latitude
     * @param lonSouth Southern Longitude
     * @param latWest  Western Longitude
     * @return BoundingBox
     */
    public LatLngBounds union(final double lonNorth, final double latEast, final double lonSouth, final double latWest) {
        return new LatLngBounds((this.mLatNorth < lonNorth) ? lonNorth : this.mLatNorth,
                (this.mLonEast < latEast) ? latEast : this.mLonEast,
                (this.mLatSouth > lonSouth) ? lonSouth : this.mLatSouth,
                (this.mLonWest > latWest) ? latWest : this.mLonWest);
    }

    /**
     * Returns a new LatLngBounds that is the intersection of this with another box
     *
     * @param box LatLngBounds to intersect with
     * @return LatLngBounds
     */
    public LatLngBounds intersect(LatLngBounds box) {
        double minLatWest = Math.max(getLonWest(), box.getLonWest());
        double maxLatEast = Math.min(getLonEast(), box.getLonEast());
        if (maxLatEast > minLatWest) {
            double minLonSouth = Math.max(getLatSouth(), box.getLatSouth());
            double maxLonNorth = Math.min(getLatNorth(), box.getLatNorth());
            if (maxLonNorth > minLonSouth) {
                return new LatLngBounds(maxLonNorth, maxLatEast, minLonSouth, minLatWest);
            }
        }
        return null;
    }

    /**
     * Returns a new LatLngBounds that is the intersection of this with another LatLngBounds
     *
     * @param northLongitude Northern Longitude
     * @param eastLatitude   Eastern Latitude
     * @param southLongitude Southern Longitude
     * @param westLatitude   Western Latitude
     * @return LatLngBounds
     */
    public LatLngBounds intersect(double northLongitude, double eastLatitude, double southLongitude, double westLatitude) {
        return intersect(new LatLngBounds(northLongitude, eastLatitude, southLongitude, westLatitude));
    }

    public static final Parcelable.Creator<LatLngBounds> CREATOR =
            new Parcelable.Creator<LatLngBounds>() {
                @Override
                public LatLngBounds createFromParcel(final Parcel in) {
                    return readFromParcel(in);
                }

                @Override
                public LatLngBounds[] newArray(final int size) {
                    return new LatLngBounds[size];
                }
            };

    @Override
    public int hashCode() {
        return (int) ((mLatNorth + 90)
                + ((mLatSouth + 90) * 1000)
                + ((mLonEast + 180) * 1000000)
                + ((mLonEast + 180) * 1000000000));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel out, final int arg1) {
        out.writeDouble(this.mLatNorth);
        out.writeDouble(this.mLonEast);
        out.writeDouble(this.mLatSouth);
        out.writeDouble(this.mLonWest);
    }

    private static LatLngBounds readFromParcel(final Parcel in) {
        final double lonNorth = in.readDouble();
        final double latEast = in.readDouble();
        final double lonSouth = in.readDouble();
        final double latWest = in.readDouble();
        return new LatLngBounds(lonNorth, latEast, lonSouth, latWest);
    }
}
