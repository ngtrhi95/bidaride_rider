package Modules;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by trhie on 5/31/2017.
 */

public class Trip implements Parcelable {
    private String tripID;
    private String userID;
    private String username;
    private String userPhone;
    private String driverID;
    private String tripFrom;
    private String tripTo;
    private double fromLong;
    private double fromLat;
    private double toLong;
    private double toLat;
    private String createdDate;
    private double price;
    private String driverFullname;
    private  String driverPhone;

    public Trip(Parcel in) {
        tripID = in.readString();
        userID = in.readString();
        username = in.readString();
        userPhone = in.readString();
        driverID = in.readString();
        tripFrom = in.readString();
        tripTo = in.readString();
        fromLong = in.readDouble();
        fromLat = in.readDouble();
        toLong = in.readDouble();
        toLat = in.readDouble();
        createdDate = in.readString();
        price = in.readDouble();
        driverFullname = in.readString();
        driverPhone = in.readString();
    }

    public Trip() {

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tripID);
        dest.writeString(userID);
        dest.writeString(username);
        dest.writeString(userPhone);
        dest.writeString(driverID);
        dest.writeString(tripFrom);
        dest.writeString(tripTo);
        dest.writeDouble(fromLong);
        dest.writeDouble(fromLat);
        dest.writeDouble(toLong);
        dest.writeDouble(toLat);
        dest.writeString(createdDate);
        dest.writeDouble(price);
        dest.writeString(driverFullname);
        dest.writeString(driverPhone);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Trip> CREATOR = new Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };

    public String getTripID() {
        return tripID;
    }

    public void setTripID(String tripID) {
        this.tripID = tripID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDriverID() {
        return driverID;
    }

    public void setDriverID(String driverID) {
        this.driverID = driverID;
    }

    public String getTripFrom() {
        return tripFrom;
    }

    public void setTripFrom(String tripFrom) {
        this.tripFrom = tripFrom;
    }

    public String getTripTo() {
        return tripTo;
    }

    public void setTripTo(String tripTo) {
        this.tripTo = tripTo;
    }

    public double getFromLong() {
        return fromLong;
    }

    public void setFromLong(double fromLong) {
        this.fromLong = fromLong;
    }

    public double getFromLat() {
        return fromLat;
    }

    public void setFromLat(double fromLat) {
        this.fromLat = fromLat;
    }

    public double getToLong() {
        return toLong;
    }

    public void setToLong(double toLong) {
        this.toLong = toLong;
    }

    public double getToLat() {
        return toLat;
    }

    public void setToLat(double toLat) {
        this.toLat = toLat;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDriverFullname() {
        return driverFullname;
    }

    public void setDriverFullname(String driverFullname) {
        this.driverFullname = driverFullname;
    }

    public String getDriverPhone() {
        return driverPhone;
    }

    public void setDriverPhone(String driverPhone) {
        this.driverPhone = driverPhone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserphone() {
        return userPhone;
    }

    public void setUserphone(String userphone) {
        this.userPhone = userphone;
    }
}