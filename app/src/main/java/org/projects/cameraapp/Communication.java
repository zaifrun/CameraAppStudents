package org.projects.cameraapp;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by makn on 19-09-2016.
 */
public class Communication {

    Context context;

    public Communication(Context context)
    {
        this.context = context;
    }

    private String convertStreamToString(InputStream is) {
        String line = "";
        StringBuilder total = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        try {
            while ((line = rd.readLine()) != null) {
                total.append(line);
            }
        } catch (Exception e) {
            Log.d("Error","Stream Exception");
           // Toast.makeText(context, "Stream Exception", Toast.LENGTH_SHORT).show();
        }
        return total.toString();
    }

    public String CreateUser(String url, final String userName) {

        RequestQueue queue = Volley.newRequestQueue(context);

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("Response", response.substring(0, 500));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ErrorResponseServer", error.getMessage().toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                JSONObject json = new JSONObject();
                try {
                    json.put("token", "testtoken");
                    json.put("username",userName);
                }
                catch (JSONException e)
                {
                    Log.d("JsonException",e.getMessage().toString());
                }
                map.put("json", json.toString());
                System.out.println("JSON:"+json.toString());
                return map;
            }
        };

// Add the request to the RequestQueue.
        queue.add(stringRequest);

        return "";
    }


}
