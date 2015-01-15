package me.chayut.emotoappapi10;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;


/**
 * Created by chayut on 6/01/15.
 */
public class eMotoAdsCollection {

    public eMotoCell eMotoCell;
    public Map<String,eMotoAds> adsHashMap =  new HashMap<String,eMotoAds>();


    //region ads mangement
    public eMotoAds getAdsWithId (String id)
    {
        return  adsHashMap.get(id);
    }

    public boolean removeAdsWithId (String id){

        if(adsHashMap.remove(id) == null)
        {
            return false;
        }
        else{
            return true;
        }
    }
    //endregion


    //region network connection
    public void getAdsCollection (String token) {

        BufferedReader rd  = null;

        //TODO: remove SSL bypass
        eMotoUtility.bypassSSLAllCertificate();

        try {
            URL u = new URL(String.format("https://emotovate.com/api/ads/all/%s?deviceId=%s&lat=%s&lng=%s",token, eMotoCell.deviceID, eMotoCell.deviceLatitude, eMotoCell.deviceLongitude));
            HttpsURLConnection c = (HttpsURLConnection) u.openConnection();

            c.setRequestMethod("GET");

            c.setRequestProperty("Content-length", "0");
            c.setRequestProperty("Content-Type","application/json");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(3000);
            c.setReadTimeout(3000);
            c.connect();
            int status = c.getResponseCode();

            Log.d("Application:", String.format("http-response:%3d", status));
            switch (status) {

                case 200:
                case 201:
                    rd  = new BufferedReader(new InputStreamReader(c.getInputStream()));

                    adsHashMap.clear();//clear all old entry in hashmap

                    String json = rd.readLine();
                    JSONArray jArray  = new JSONArray(json);
                    for(int n = 0; n < jArray.length(); n++) {
                        eMotoAds myAds = new eMotoAds(jArray.getJSONObject(n));
                        adsHashMap.put(myAds.id(), myAds);
                    }

                    for(Map.Entry<String, eMotoAds> entry: adsHashMap.entrySet())  {

                        Log.d("Application", entry.getValue().id());
                    }

                    break;
                case 401:
                    Log.d("Application:","Server unauthorized");
                    break;
                default:


            }
        } catch (MalformedURLException ex) {
            ex.printStackTrace();

        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

        catch (JSONException ex){
            ex.printStackTrace();
        }

    }

    public boolean approveAdsWithID(String adsID,String token){

        BufferedReader rd  = null;

        if(this.getAdsWithId(adsID) != null)
        {
            eMotoAds ads = adsHashMap.get(adsID);
            try {
                URL u = new URL(String.format("https://emotovate.com/api/ads/approve/%s?scheduleAssetId=%s&userIP=%s",token,ads.scheduleAssetId(),"192.168.1.1"));
                HttpsURLConnection c = (HttpsURLConnection) u.openConnection();

                c.setRequestMethod("POST");

                c.setRequestProperty("Content-length", "0");
                c.setRequestProperty("Content-Type","application/json");
                c.setUseCaches(false);
                c.setAllowUserInteraction(false);
                c.setConnectTimeout(3000);
                c.setReadTimeout(3000);
                c.connect();
                int status = c.getResponseCode();

                Log.d("Application:", String.format("http-response:%3d", status));
                switch (status) {

                    case 200:
                    case 201:
                        rd  = new BufferedReader(new InputStreamReader(c.getInputStream()));

                        String json = rd.readLine();
                        Log.d("Application:",json);
                        break;
                    case 401:
                        Log.d("Application:","Server unauthorized");
                        break;
                    default:


                }
            } catch (MalformedURLException ex) {
                ex.printStackTrace();

            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public boolean unapproveAdsWithID(String adsID,String token){

        BufferedReader rd  = null;

        if(this.getAdsWithId(adsID) != null)
        {
            eMotoAds ads = adsHashMap.get(adsID);
            try {
                URL u = new URL(String.format("https://emotovate.com/api/ads/unapprove/%s?scheduleAssetId=%s&userIP=%s",token,ads.scheduleAssetId(),"192.168.1.1"));
                HttpsURLConnection c = (HttpsURLConnection) u.openConnection();

                c.setRequestMethod("POST");

                c.setRequestProperty("Content-length", "0");
                c.setRequestProperty("Content-Type","application/json");
                c.setUseCaches(false);
                c.setAllowUserInteraction(false);
                c.setConnectTimeout(3000);
                c.setReadTimeout(3000);
                c.connect();
                int status = c.getResponseCode();

                Log.d("Application:", String.format("http-response:%3d", status));
                switch (status) {

                    case 200:
                    case 201:
                        rd  = new BufferedReader(new InputStreamReader(c.getInputStream()));

                        String json = rd.readLine();
                        Log.d("Application:",json);
                        break;
                    case 401:
                        Log.d("Application:","Server unauthorized");
                        break;
                    default:


                }
            } catch (MalformedURLException ex) {
                ex.printStackTrace();

            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }
    //endregion



}
