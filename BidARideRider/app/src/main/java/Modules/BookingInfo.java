package Modules;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by trhie on 5/13/2017.
 */

public class BookingInfo implements Parcelable {
    private String driverName;
    private String driverPhone;
    private String fromAddress;
    private String toAddress;
    private int cost;

    public BookingInfo() {

    }

    protected BookingInfo(Parcel in) {
        driverName = in.readString();
        driverPhone = in.readString();
        fromAddress = in.readString();
        toAddress = in.readString();
        cost = in.readInt();
    }

    public static final Creator<BookingInfo> CREATOR = new Creator<BookingInfo>() {
        @Override
        public BookingInfo createFromParcel(Parcel in) {
            return new BookingInfo(in);
        }

        @Override
        public BookingInfo[] newArray(int size) {
            return new BookingInfo[size];
        }
    };

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverPhone() {
        return driverPhone;
    }

    public void setDriverPhone(String driverPhone) {
        this.driverPhone = driverPhone;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
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
        dest.writeString(driverName);
        dest.writeString(driverPhone);
        dest.writeString(fromAddress);
        dest.writeString(toAddress);
        dest.writeInt(cost);
    }
}
