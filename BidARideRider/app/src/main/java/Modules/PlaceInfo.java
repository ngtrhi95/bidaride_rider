package Modules;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by trhie on 5/1/2017.
 */

public class PlaceInfo implements Parcelable {
    private String address;
    private LatLng location;
    private int idMap;


    public PlaceInfo(Parcel in) {
        address = in.readString();
        location = in.readParcelable(LatLng.class.getClassLoader());
        idMap = in.readInt();
    }

    public static final Creator<PlaceInfo> CREATOR = new Creator<PlaceInfo>() {
        @Override
        public PlaceInfo createFromParcel(Parcel in) {
            return new PlaceInfo(in);
        }

        @Override
        public PlaceInfo[] newArray(int size) {
            return new PlaceInfo[size];
        }
    };

    public PlaceInfo() {

    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public int getIdMap() {
        return idMap;
    }

    public void setIdMap(int idMap) {
        this.idMap = idMap;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeParcelable(location, flags);
        dest.writeInt(idMap);
    }
}
