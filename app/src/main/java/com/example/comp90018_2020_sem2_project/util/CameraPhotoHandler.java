/*
Borrows code from the official android developer documentation available at https://developer.android.com/training/camera/photobasics
 */

package com.example.comp90018_2020_sem2_project.util;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;


public class CameraPhotoHandler extends AppCompatActivity {

    Context context;


    public CameraPhotoHandler(Context context) {
        this.context = context;
    }


    /*
    Creates an image file for the handlers context and returns a URI for it.
     */
    public Uri createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "JPEG_" + "Full_Image" + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        Uri photoURI = FileProvider.getUriForFile(context,"com.example.android.fileprovider", image);
        return photoURI;
    }

}
