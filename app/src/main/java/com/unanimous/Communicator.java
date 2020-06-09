package com.unanimous;

import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.unanimous.MyUtils.str;

public class Communicator {
    private OkHttpClient client;
    private static String[] responsebodies;

    public Communicator() {
        client = new OkHttpClient();
    }

    public ArrayList<String> requestMap(final String filename) {
        ArrayList<String> arrstr = new ArrayList<>();

        Thread thread = new Thread() {
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url("http://220.70.2.54:8080/download/" + filename + ".pcd")
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);
                    ResponseBody responseBody = response.body();
                    String responseString = responseBody.string();
                    responsebodies = responseString.split("\n");
                } catch (
                        IOException ioe) {
                    Log.w("Run ", ioe.getMessage());
                }
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        for (int i = 0; i < responsebodies.length; i++) {
            arrstr.add(responsebodies[i]);
        }
        return arrstr;
    }

    public ArrayList<String> requestPath(final String filename, final float start_x, final float start_y,
                            final float goal_x, final float goal_y) {
        // reset the response...
        responsebodies = new String[]{""};

        Thread thread = new Thread() {
            @Override
            public void run() {
                RequestBody formBody = new FormEncodingBuilder()
                        .add("file_name", filename)
                        .add("start_x", str(start_x))
                        .add("start_y", str(start_y))
                        .add("goal_x", str(goal_x))
                        .add("goal_y", str(goal_y))
                        .build();
                Request request = new Request.Builder()
                        .url("http://220.70.2.54:8080/pathfind/")
                        .post(formBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) throw new IOException("Unexpected code "+response);
                    //Log.i("Response ", response.body().string());
                    // Response be readable only once...!!!
                    String responseString = response.body().string();
                    if (responseString.equals("Socket Fail")) {
                        Log.i("Response ", "failed...");
                        return;
                    }
                    // renew the response!
                    responsebodies = responseString.split("\n");
                } catch (IOException ioe) {
                    Log.w("Run ", ioe.getMessage());
                } catch (IllegalStateException ise) {
                    ise.printStackTrace();
                }
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        } finally {
            if (responsebodies.length == 0) {
                return null;
            }
            else {
                ArrayList<String> apoints = new ArrayList<>();
                for (int i = 0; i < responsebodies.length; i++)
                    apoints.add(responsebodies[i]+",0,0");
                return apoints;
            }
        }
    }
}
