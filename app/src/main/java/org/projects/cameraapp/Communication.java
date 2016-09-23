package org.projects.cameraapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by makn on 19-09-2016.
 */
public class Communication {

    Context context;
    RequestQueue queue;
    public final static String URL_BASE = "https://185.93.195.194/";
    public final static String URL_CREATEUSER = "https://185.93.195.194/createuser.php";
    public final static String URL_UPLOADIMAGE = "https://185.93.195.194/uploadpic.php";

    public Communication(Context context)
    {
        this.context = context;
    }



    public class CustomHostnameVerifier implements HostnameVerifier
    {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
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

    public void setupSSLCertificate()
    {
       // queue = Volley.newRequestQueue(context);
        String ur = URL_BASE;
        System.out.println("setting up ssl ca");
        URL url = null;
        Certificate ca = null;
        HttpsURLConnection.setDefaultHostnameVerifier(new CustomHostnameVerifier());

        HttpsURLConnection urlConnection= null;

        try {
            url = new URL(ur);
        }
        catch (MalformedURLException e)
        {
            Log.d("MalformedURL",e.toString());
        }

        // Load CAs from an InputStream
// (could be from a resource or ByteArrayInputStream or ...)
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
           // new FileInputStream(R.raw.cacert);
            InputStream caInput = context.getResources().openRawResource(
                    context.getResources().getIdentifier("cacert",
                            "raw", context.getPackageName()));

           // InputStream caInput = new BufferedInputStream(new FileInputStream(R.raw.cacert));
            try {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }
        } catch (Exception e)
        {
            Log.d("CertificationError",e.toString());
        }
        System.out.println("factory done!");


// Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

        }
        catch (Exception e)
        {
            Log.d("KeyStoreException",e.toString());
        }
        System.out.println("KeyStore done!");

// Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

// Create an SSLContext that uses our TrustManager

            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, tmf.getTrustManagers(), null);

// Tell the URLConnection to use a SocketFactory from our SSLContext
         //   URL url = new URL("https://certs.cac.washington.edu/CAtest/");
            urlConnection =
                    (HttpsURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(sslcontext.getSocketFactory());
        }
        catch (Exception e)
        {
            Log.d("TrustManagerException",e.toString());
        }
        System.out.println("Trustmanager done!");
        if ("https".equals(url.getProtocol())) {
           // ((HttpsURLConnection)urlConnection).setSSLSocketFactory(mSslSocketFactory);
            urlConnection.setHostnameVerifier(new CustomHostnameVerifier());
        }

        queue = Volley.newRequestQueue(context, new HurlStack(null, urlConnection.getSSLSocketFactory()));

    }

    public String trimMessage(String json, String key){
        String trimmedString = null;

        try{
            JSONObject obj = new JSONObject(json);
            trimmedString = obj.getString(key);
        } catch(JSONException e){
            e.printStackTrace();
            return null;
        }

        return trimmedString;
    }

    public String uploadPicture(final Bitmap bitmap,final String token,
                                final String caseId, final String description,
                                final String imageName)
    {
        System.out.println("uploading image...");
        String url = URL_UPLOADIMAGE;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        final String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);


        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("ResponseUploadImage", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ErrorServerUpload",error.toString());
                System.out.println("error from server");
                String json2 = null;
                if (error.networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        // Show timeout error message
                        Log.d("ErrorTimeOut","TimeOut errror");

                    }
                }

                NetworkResponse response = error.networkResponse;
                if(response != null && response.data != null){
                    switch(response.statusCode){
                        case 400:
                            json2 = new String(response.data);
                           // json2 = trimMessage(json2, "message");
                            if(json2 != null)
                                Log.d("ErrorServerUpload", json2);
                            break;
                        case 200:
                            json2 = new String(response.data);
                            // json2 = trimMessage(json2, "message");
                            if(json2 != null)
                                Log.d("SucccesUploading", json2);
                            break;
                    }
                    //Additional cases
                }
                if (error.getMessage()!=null)
                    Log.d("ErrorServerUpload", error.getMessage().toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                JSONObject json = new JSONObject();

                try {
                    json.put("token", token);
                    json.put("case_id",Integer.valueOf(caseId).intValue());
                    json.put("description",description);
                    json.put("name",imageName);
                    System.out.println("JSON:"+json.toString());

                    json.put("base64",encoded);
                }
                catch (JSONException e)
                {
                    Log.d("JsonExceptionUpload",e.getMessage().toString());
                }
                map.put("json", json.toString());
                //System.out.println("JSON:"+json.toString());
                return map;
            }
        };

// Add the request to the RequestQueue.
        int socketTimeout = 40000;//20 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        queue.add(stringRequest);

        return "";
    }

    public String CreateUser(final String userName, final String token) {
        String url = URL_CREATEUSER;
        //setupSSLCertificate(url);
// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("Response", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ErrorResponseServer",error.toString());
                if (error.getMessage()!=null)
                    Log.d("ErrorResponseServer", error.getMessage().toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                JSONObject json = new JSONObject();

                try {
                    json.put("token", token);
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
        int socketTimeout = 20000;//20 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        queue.add(stringRequest);

        return "";
    }


}
