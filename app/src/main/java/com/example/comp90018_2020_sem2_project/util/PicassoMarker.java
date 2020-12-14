package com.example.comp90018_2020_sem2_project.util;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class PicassoMarker implements Target {
    Marker mMarker;

    public PicassoMarker(Marker marker) {
        mMarker = marker;
    }

    /*
    Returns the markers hash code
     */
    @Override
    public int hashCode() {
        return mMarker.hashCode();
    }

    /*
    Overridden equality checking for picasso markers
     */
    @Override
    public boolean equals(Object o) {
        if(o instanceof PicassoMarker) {
            Marker marker = ((PicassoMarker) o).mMarker;
            return mMarker.equals(marker);
        } else {
            return false;
        }
    }

    /*
    Sets marker icon
     */
    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
    }

    @Override
    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
        Log.e("picasso","er"+e);
    }


    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }
}
