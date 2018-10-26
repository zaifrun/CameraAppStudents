package org.projects.cameraapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
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
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Context context;
    Bitmap imageBitmap;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView imageView; //for displaying the image.
    String mCurrentPhotoPath; //for storing the path to the image taken.


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //NOTICE we do not get any data directly back from the intent
            //but the camera app will take the picture and save it in the uri
            //we have specified earlier and we can use that path to read the file
            //and convert it into a bitmap.

            if (mCurrentPhotoPath!=null) {
                //decoding the file into a bitmap
                imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
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
                //This creates a URI for our file, using the provider we have
                //made in the manifest file.
                Uri photoURI = FileProvider.getUriForFile(this,
                        "org.projects.cameraapp.fileprovider",
                        photoFile);
                //putting the URI in the Intent for the camera app to use
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // A hack to get it to work on older Android versions
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    List<ResolveInfo> resolvedIntentActivities = context.getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
                        String packageName = resolvedIntentInfo.activityInfo.packageName;
                        context.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                }//end of hack

                //start the intent and wait for the result.
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        } else
        {
            //Make a toast if there is no camera app installed.
            Toast toast = Toast.makeText(this,"No program to take pictures",Toast.LENGTH_SHORT);
            toast.show();
        }
    }

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

        // Save a file: path for use with ACTION_VIEW intents used later
        mCurrentPhotoPath =  image.getAbsolutePath();
        System.out.println("photo path:"+mCurrentPhotoPath);
        return image;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        context = this;

        Button pictureButton = findViewById(R.id.pictureButton);
        //putting a clicklistener on the button.
        pictureButton.setOnClickListener(new View.OnClickListener() {
            //What should happen when we click the take image button.
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        Button clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageResource(0);
            }
        });





    }


}
