package me.chayut.emotoappapi10;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by chayut on 6/01/15.
 */
public class eMotoLoginResponse implements Parcelable {


    //TODO Change to SetterGetter
    public boolean success;
    public String token;
    public String idle;
    public String username;
    public String credential; //temporary, unsafe

    public eMotoLoginResponse(){
        success = false;
        this.token = null;
        this.idle = null;
        this.username = null;
        this.credential = null;
    }

    //Parcelable
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeByte((byte) (success ? 1 : 0));
        out.writeString(token);
        out.writeString(idle);
        out.writeString(username);
        out.writeString(credential);
    }

    public static final Creator<eMotoLoginResponse> CREATOR
            = new Creator<eMotoLoginResponse>() {
        public eMotoLoginResponse createFromParcel(Parcel in) {
            return new eMotoLoginResponse(in);
        }

        public eMotoLoginResponse[] newArray(int size) {
            return new eMotoLoginResponse[size];
        }
    };

    private eMotoLoginResponse(Parcel in) {

        success = in.readByte() != 0;
        token = in.readString();
        idle = in.readString();
        username = in.readString();
        credential = in.readString();

    }

}
