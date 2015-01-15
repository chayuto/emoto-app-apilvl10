package me.chayut.emotoappapi10;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chayut on 7/01/15.
 */
public class eMotoAds {
    public JSONObject adsJSONObject;

    public eMotoAds(JSONObject ads)
    {
        adsJSONObject = ads;
    }
    public String description() {

        String des = null;
        try {
            des = adsJSONObject.getString("Description");
        }
        catch (JSONException ex){
            ex.printStackTrace();
        }
        return des;
    }

    public String id() {

        String Id = null;
        try {
            Id = adsJSONObject.getString("Id");
        }
        catch (JSONException ex){
            ex.printStackTrace();
        }
        return Id;
    }

    public String scheduleAssetId() {

        String Id = null;
        try {
            Id = adsJSONObject.getString("ScheduleAssetId");
        }
        catch (JSONException ex){
            ex.printStackTrace();
        }
        return Id;
    }

    public String isApprovedStr() {

        String Id = null;
        try {
            Id = adsJSONObject.getString("Approved");
        }
        catch (JSONException ex){
            ex.printStackTrace();
        }
        return Id;
    }

    public Boolean isApproved(){
        return Boolean.parseBoolean(isApprovedStr());
    }

    public String getAdsImageURL(){
        String URL = null;
        try {
            URL = adsJSONObject.getString("Url");
        }
        catch (JSONException ex){
            ex.printStackTrace();
        }
        return URL;
    }

    public String getAdsThumbnailURL(){
        String s = getAdsImageURL();
        String ThumbnailURL = s.substring(0,s.length()-4) + "_t.jpg";

        return ThumbnailURL;
    }
}
