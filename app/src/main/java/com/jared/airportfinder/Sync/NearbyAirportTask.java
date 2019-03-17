package com.jared.airportfinder.Sync;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.jared.airportfinder.Interfaces.NearbyAirportListener;
import com.jared.airportfinder.R;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NearbyAirportTask extends AsyncTask<String, Void, String> {

    final private ProgressDialog progressDialog;
    private NearbyAirportListener nearbyAirportListener;
    LatLng coordinates;

    public NearbyAirportTask(Activity context, NearbyAirportListener activityContext, LatLng coords){
        progressDialog = new ProgressDialog(context);
        this.nearbyAirportListener = activityContext;
        coordinates = coords;
    }

    @Override
    protected void onPreExecute(){
        progressDialog.setMessage("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    @Override
    protected String doInBackground(String... urls) {
        String result = "";
        URL url;
        HttpURLConnection http;
        InputStream inputStream;
        int responseCode = 0;

        try {
            url = new URL(urls[0]);
            http = (HttpURLConnection) url.openConnection();
            http.setRequestProperty("Content-Type", "application/json");
            http.setRequestMethod("GET");
            http.setReadTimeout(15000);
            http.setConnectTimeout(15000);
            http.connect();

            if (http.getResponseCode() < http.HTTP_BAD_REQUEST) {
                inputStream = http.getInputStream();
            } else {
                inputStream = http.getErrorStream();
                responseCode = http.getResponseCode();
            }

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            int data = inputStreamReader.read();

            while (data != -1){
                char current = (char) data;
                result += current;

                data = inputStreamReader.read();
            }
        } catch (Exception e){
            return String.valueOf(responseCode);
        }
        return result;
    }

    @Override
    protected  void onPostExecute(String s){
        if (progressDialog.isShowing()){
            progressDialog.dismiss();
        }

        try {
            if (s.isEmpty()) {
                Log.i("NearbyAirportTask", "no data");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        nearbyAirportListener.onNearbyAirportListener(s);
    }
}
