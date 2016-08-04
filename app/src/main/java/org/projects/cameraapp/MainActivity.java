package org.projects.cameraapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView imageView; //for displaying the image.
    String mCurrentPhotoPath; //for storing the path to the image taken.

    private File createImageFile() throws IOException {
        // Create an image file name - we want a unique filename
        //so we are using the current time as a timestamp to include in the filename.
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //This specifies that we store the images in the PRIVATE folder
        //that ONLY our app has access to (this is the effect of getExternalFilesDir).
        //and the Environment.DIRECTORY_PICTURES constant will make sure that
        //we store the file in the subdirectory called "Pictures" within that
        //directory.
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        //you can take a look at this path in the android monitor - it will give you
        //some idea of what the path looks like - this is a VERY good idea.
        System.out.println("storagedirectory:"+storageDir);

        //This creates an empty file.
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath =  image.getAbsolutePath(); //"file:" +
        System.out.println("photo path:"+mCurrentPhotoPath);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //NOTICE we do not get any data directly back from the intent
            //but the camera app will take the picture and save it in the url
            //we have specified earlier and we can use that path to read the file
            //and convert it into a bitmap.
            if (mCurrentPhotoPath!=null) {
                //decoding the file into a bitmap
                Bitmap imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
                //setting the bitmap on the image file.
                imageView.setImageBitmap(imageBitmap);
            }
            else {
                Toast toast = Toast.makeText(this,"no image filed saved",Toast.LENGTH_SHORT);
                toast.show();
            }

        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Make sure we have an app that can hanlde the IMAGE_CAPTURE intent.
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast toast = Toast.makeText(this,"Could not create file",Toast.LENGTH_SHORT);
                toast.show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                //This converts our file to a URI, using the provider we have
                //made in the manifest file.
                Uri photoURI = FileProvider.getUriForFile(this,
                        "org.projects.cameraapp.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        } else
        {
            //Make a toast if there is no camera app installed.
            Toast toast = Toast.makeText(this,"No program to take pictures",Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);
        Button pictureButton = (Button) findViewById(R.id.pictureButton);
        pictureButton.setOnClickListener(new View.OnClickListener() {
            //What should happen when we click the take image button.
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }
}
