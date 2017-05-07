package Modules;

/**
 * Created by trhie on 5/2/2017.
 */

public class TripInfo {
    private String userID;
    private String driverID;
    private String tripFrom;
    private String tripTo;
    private String fromLong;
    private String fromLat;
    private String toLong;
    private String toLat;

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

    public String getFromLong() {
        return fromLong;
    }

    public void setFromLong(String fromLong) {
        this.fromLong = fromLong;
    }

    public String getFromLat() {
        return fromLat;
    }

    public void setFromLat(String fromLat) {
        this.fromLat = fromLat;
    }

    public String getToLong() {
        return toLong;
    }

    public void setToLong(String toLong) {
        this.toLong = toLong;
    }

    public String getToLat() {
        return toLat;
    }

    public void setToLat(String toLat) {
        this.toLat = toLat;
    }
}
