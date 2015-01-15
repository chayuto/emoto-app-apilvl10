package me.chayut.emotoappapi10;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by chayut on 6/01/15.
 */
public class eMotoCell {
    public String deviceID;
    public String deviceLatitude;
    public String deviceLongitude;

    //region network connection
    public void putDeviceOnServer (String token) {
        Log.d("eMotoCell:", "putDeviceOnServer");

        BufferedReader rd  = null;

        //TODO: remove SSL bypass
        eMotoUtility.bypassSSLAllCertificate();

        try {
            URL u = new URL(String.format("https://emotovate.com/api/devicetracking/add/%s",token));
            String jsonString = String.format("{\"DeviceId\":\"%s\",\"LightSensor\":\"%s\",\"Longitude\":\"%s\",\"Latitude\":\"%s\",\"Temperature\":\"%s\"}",deviceID,"true",deviceLongitude,deviceLatitude,"24.3");
            //String jsonString = String.format("{DeviceId:\"%s\",LightSensor:\"%s\",Longitude:\"%s\",Latitude:\"%s\",Temperature:\"%s\"}","00000000","true","-33.8238395","151.1996951","24.3");
            Log.d("eMotoCell:", String.format("JSON:%s", jsonString));
            HttpsURLConnection c = (HttpsURLConnection) u.openConnection();


            c.setRequestMethod("POST");
            c.setUseCaches(false);
            c.setDoInput(true);
            c.setDoOutput(true);
            c.setRequestProperty("Content-length", String.format("%d",jsonString.getBytes("UTF-8").length));
            c.setRequestProperty("Content-Type","application/json");
            //c.setRequestProperty("Accept", "application/json");
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(3000);
            c.setReadTimeout(3000);

            c.connect();

            OutputStream os = c.getOutputStream();
            os.write(jsonString.getBytes("UTF-8"));
            os.flush();
            os.close();

            int status = c.getResponseCode();

            Log.d("eMotoCell:", String.format("http-response:%3d", status));
            switch (status) {

                case 200:
                case 201:
                    rd  = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    Log.d("eMotoCell:",rd.readLine());
                    break;
                case 400:
                case 500:
                    rd  = new BufferedReader(new InputStreamReader(c.getErrorStream()));
                    Log.d("eMotoCell:",rd.readLine());
                    break;
                case 401:
                    rd  = new BufferedReader(new InputStreamReader(c.getInputStream()));
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
}
