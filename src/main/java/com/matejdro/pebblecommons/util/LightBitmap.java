package com.matejdro.pebblecommons.util;

import android.graphics.Bitmap;

/**
 * Lighter bitmap container that allows much faster access to getPixel and setPixel methods than Android's {@link Bitmap}.
 */
public class LightBitmap
{
    private int width;
    private int height;
    private int[] pixels;

    public LightBitmap(Bitmap bitmap)
    {
        width = bitmap.getWidth();
        height = bitmap.getHeight();

        pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
    }

    public int getPixel(int x, int y)
    {
        return pixels[y * width + x];
    }

    public void setPixel(int x, int y, int pixel)
    {
        pixels[y * width + x] = pixel;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public Bitmap toBitmap()
    {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
}
