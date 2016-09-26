package org.projects.cameraapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Context context;
    Communication communication;
    String token = "token";
    String imageName = "imageName";
    Bitmap imageBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        communication = new Communication(this);
        communication.setupSSLCertificate();
        //get token if it exists
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        if (prefs.contains("token"))
        {
            token = prefs.getString("token","");
        } else{
            //TODO we need to show a dialog to the user to enter a username
            //See the createUser method that is here.
        }
        //TODO setup your listeners for buttons etc....

    }


    public void createUser() {
        OkCancelInputDialog dialog = new OkCancelInputDialog(this,"Create user","Choose a username")
        {
            @Override
            public void clickCancel() {
                super.clickCancel();
            }

            @Override
            public void clickOk() {
                Toast toast = Toast.makeText(context,"Creating user...please wait",Toast.LENGTH_LONG);
                toast.show();
                //Communication coms = new Communication(context);
                communication.CreateUser(getUserInput(),token+=System.currentTimeMillis());
                super.clickOk();
            }
        };
        dialog.show();


    }

}
