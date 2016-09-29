package com.matejdro.pebblecommons.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.Nullable;

public class BitmapUtils {
    public static @Nullable Bitmap getBitmap(Context context, Parcelable parcelable)
    {
        if (parcelable instanceof Bitmap)
            return (Bitmap) parcelable;
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && parcelable instanceof Icon)
            return  getBitmap(context, (Icon) parcelable);
        else if (parcelable instanceof Drawable)
            return getBitmap((Drawable) parcelable);
        else
            return null;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static @Nullable Bitmap getBitmap(Context context, Icon icon)
    {
        return getBitmap(icon.loadDrawable(context));
    }

    public static @Nullable Bitmap getBitmap(Drawable drawable)
    {
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            return bitmapDrawable.getBitmap();
        }

        Bitmap bitmap;
        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
