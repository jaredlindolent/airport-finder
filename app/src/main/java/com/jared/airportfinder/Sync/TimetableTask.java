package com.jared.airportfinder.Sync;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.jared.airportfinder.Interfaces.TimetableListener;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TimetableTask extends AsyncTask<String, Void, String> {

    final private ProgressDialog progressDialog;
    private TimetableListener timetableListener;
    String iataCode;

    public TimetableTask(Activity context, TimetableListener activityContext, String iataCode){
        progressDialog = new ProgressDialog(context);
        this.timetableListener = activityContext;
        this.iataCode = iataCode;
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

            while (data != -1) {
                char current = (char) data;
                result += current;

                data = inputStreamReader.read();
            }
        } catch (Exception e) {
            return String.valueOf(responseCode);
        }
        return result;
    }

    @Override
    protected void onPostExecute(String s){
        if (progressDialog.isShowing()){
            progressDialog.dismiss();
        }

        try {
            if (s.isEmpty()){
                Log.i("TimetableTask", "no data");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        timetableListener.onTimeTableListener(s);
    }
}
