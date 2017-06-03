package Modules;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by trhie on 5/1/2017.
 */

public class DirectionInfo implements Parcelable {
    private PlaceInfo originInfo;
    private String userID;
    private String DesAddress;
    private LatLng DesLocation;
    private int cost;



    public DirectionInfo() {

    }

    protected DirectionInfo(Parcel in) {
        originInfo = in.readParcelable(PlaceInfo.class.getClassLoader());
        userID = in.readString();
        DesAddress = in.readString();
        DesLocation = in.readParcelable(LatLng.class.getClassLoader());
        cost = in.readInt();
    }

    public static final Creator<DirectionInfo> CREATOR = new Creator<DirectionInfo>() {
        @Override
        public DirectionInfo createFromParcel(Parcel in) {
            return new DirectionInfo(in);
        }

        @Override
        public DirectionInfo[] newArray(int size) {
            return new DirectionInfo[size];
        }
    };

    public PlaceInfo getOriginInfo() {
        return originInfo;
    }

    public void setOriginInfo(PlaceInfo originInfo) {
        this.originInfo = originInfo;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDesAddress() {
        return DesAddress;
    }

    public void setDesAddress(String desAddress) {
        DesAddress = desAddress;
    }

    public LatLng getDesLocation() {
        return DesLocation;
    }

    public void setDesLocation(LatLng desLocation) {
        DesLocation = desLocation;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(originInfo, flags);
        dest.writeString(userID);
        dest.writeString(DesAddress);
        dest.writeParcelable(DesLocation, flags);
        dest.writeInt(cost);
    }
}
