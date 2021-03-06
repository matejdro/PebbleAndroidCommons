package com.matejdro.pebblecommons.pebble;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.ColorInt;

import com.matejdro.pebblecommons.util.LightBitmap;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineByte;
import ar.com.hjg.pngj.PngWriter;
import ar.com.hjg.pngj.chunks.PngChunkBKGD;
import ar.com.hjg.pngj.chunks.PngChunkPLTE;
import ar.com.hjg.pngj.chunks.PngChunkTRNS;


public class PebbleImageToolkit
{


    public static int[] PEBBLE_TIME_PALETTE = new int[64];
    public static HashMap<Integer, Byte> PEBBLE_TIME_PALETTE_MAP = new HashMap<>();

    static
    {
        int counter = 0;

        for (int r = 0x000000; r <= 0xFF0000; r += 0x550000)
        {
            for (int g = 0x000000; g <= 0x00FF00; g += 0x005500)
            {
                for (int b = 0x000000; b <= 0x0000FF; b += 0x000055)
                {
                    int color = r | g | b;
                    PEBBLE_TIME_PALETTE[counter] = color;
                    PEBBLE_TIME_PALETTE_MAP.put(color, (byte) counter);

                    counter++;
                }
            }
        }
    }

    public static Bitmap resizePreservingRatio(Bitmap original, int newWidth, int newHeight)
    {
        return resizePreservingRatio(original, newWidth, newHeight, true);
    }

    public static Bitmap resizePreservingRatio(Bitmap original, int newWidth, int newHeight, boolean filter)
    {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        if (newWidth / (float) originalWidth < newHeight / (float) originalHeight)
        {
            newHeight = originalHeight * newWidth / originalWidth;
        }
        else
        {
            newWidth = originalWidth * newHeight / originalHeight;
        }

        return Bitmap.createScaledBitmap(original, newWidth, newHeight, filter);
    }

    public static Bitmap resizeAndCrop(Bitmap original, int newWidth, int newHeight, boolean filter)
    {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        int originalNewWidth = newWidth;
        int originalNewHeight = newHeight;

        if (newWidth / (float) originalWidth < newHeight / (float) originalHeight)
        {
            newWidth = originalWidth * newHeight / originalHeight;
        }
        else
        {
            newHeight = originalHeight * newWidth / originalWidth;
        }

        Bitmap scaledBitmap =  Bitmap.createScaledBitmap(original, newWidth, newHeight, filter);
        return Bitmap.createBitmap(scaledBitmap, (newWidth - originalNewWidth) / 2, (newHeight - originalNewHeight) / 2, originalNewWidth, originalNewHeight, null, filter);
    }

    public static Bitmap multiplyBrightness(Bitmap original, float multiplier)
    {
        LightBitmap lightBitmap = new LightBitmap(original);
        for (int x = 0; x < lightBitmap.getWidth(); x++)
        {
            for (int y = 0; y < lightBitmap.getHeight(); y++)
            {
                int pixel = original.getPixel(x, y);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                lightBitmap.setPixel(x, y, Color.rgb((int) (r * multiplier), (int) (g * multiplier), (int) (b * multiplier)));
            }
        }

        return lightBitmap.toBitmap();
    }

    public static Bitmap createGrayscaleFromAlphaMask(Bitmap original)
    {
        return createMaskFromAlpha(original, Color.WHITE, true);
    }

    public static Bitmap createMaskFromAlpha(Bitmap original, @ColorInt int maskColor, boolean blackBackground)
    {
        int colorR = Color.red(maskColor);
        int colorG = Color.green(maskColor);
        int colorB = Color.blue(maskColor);

        LightBitmap lightBitmap = new LightBitmap(original);
        for (int x = 0; x < lightBitmap.getWidth(); x++)
        {
            for (int y = 0; y < lightBitmap.getHeight(); y++)
            {
                int pixel = original.getPixel(x, y);
                int alpha = Color.alpha(pixel);

                if (blackBackground)
                {
                    lightBitmap.setPixel(x, y, Color.rgb(alpha * colorR / 255, alpha * colorG / 255, alpha * colorB / 255));
                }
                else
                {
                    int revAlpha = 255 - alpha;
                    lightBitmap.setPixel(x, y, Color.rgb(revAlpha + alpha * colorR / 255, revAlpha + alpha * colorG / 255, revAlpha + alpha * colorB / 255));
                }
            }
        }

        return lightBitmap.toBitmap();
    }

    public static Bitmap reduceGrayscaleToBlackWhite(Bitmap original)
    {
        LightBitmap lightBitmap = new LightBitmap(original);
        for (int x = 0; x < lightBitmap.getWidth(); x++)
        {
            for (int y = 0; y < lightBitmap.getHeight(); y++)
            {
                int pixel = original.getPixel(x, y);
                int red = Color.red(pixel);
                red = red > 255 / 2 ? 255 : 0;

                lightBitmap.setPixel(x, y, Color.rgb(red, red, red));
            }
        }

        return lightBitmap.toBitmap();
    }


    public static Bitmap invertImage(Bitmap original)
    {
        LightBitmap lightBitmap = new LightBitmap(original);
        for (int x = 0; x < lightBitmap.getWidth(); x++)
        {
            for (int y = 0; y < lightBitmap.getHeight(); y++)
            {
                int pixel = lightBitmap.getPixel(x, y);
                lightBitmap.setPixel(x, y, invertColor(pixel));
            }
        }

        return lightBitmap.toBitmap();
    }

    public static @ColorInt int invertColor(@ColorInt int color)
    {
        int r = 255 - Color.red(color);
        int g = 255 - Color.green(color);
        int b = 255 - Color.blue(color);

        return Color.rgb(r, g, b);
    }

    public static @ColorInt int multiplyBrightness(@ColorInt int color, float multiplier)
    {
        int r = (int) (Color.red(color) * multiplier);
        int g = (int) (Color.green(color) * multiplier);
        int b = (int) (Color.blue(color) * multiplier);

        return Color.rgb(r, g, b);
    }

    public static int getLuminance(@ColorInt int color)
    {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return (r + r + b + g + g + g) / 6;
    }

    public static Bitmap ditherToPebbleTimeColors(Bitmap bitmap)
    {
        LightBitmap lightBitmap = new LightBitmap(bitmap);
        SeparatedColor[][] separatedColorArray = new SeparatedColor[lightBitmap.getWidth()][lightBitmap.getHeight()];

        for (int y = 0; y < lightBitmap.getHeight(); y++)
        {
            for (int x = 0; x < lightBitmap.getWidth(); x++)
            {
                separatedColorArray[x][y] = new SeparatedColor(lightBitmap.getPixel(x, y));
            }
        }

        for (int y = 0; y < lightBitmap.getHeight(); y++)
        {
            for (int x = 0; x < lightBitmap.getWidth(); x++)
            {
                SeparatedColor oldColor = separatedColorArray[x][y];
                SeparatedColor newColor = oldColor.getNearestPebbleTimeColor();
                lightBitmap.setPixel(x, y, newColor.toRGB());

                newColor.reverseSub(oldColor);

                if (x < lightBitmap.getWidth() - 1)
                    separatedColorArray[x + 1][y].addAndMultiplyAndDivide16(newColor, 7);

                if (y < lightBitmap.getHeight() - 1)
                {
                    if (x > 0)
                        separatedColorArray[x - 1][y + 1].addAndMultiplyAndDivide16(newColor, 3);

                    separatedColorArray[x][y + 1].addAndMultiplyAndDivide16(newColor, 5);

                    if (x < lightBitmap.getWidth() - 1)
                        separatedColorArray[x + 1][y + 1].addAndMultiplyAndDivide16(newColor, 1);
                }
            }
        }

        return lightBitmap.toBitmap();
    }

    public static Bitmap ditherToBlackWhite(Bitmap bitmap)
    {
        LightBitmap lightBitmap = new LightBitmap(bitmap);
        SeparatedColor[][] separatedColorArray = new SeparatedColor[lightBitmap.getWidth()][lightBitmap.getHeight()];

        for (int y = 0; y < lightBitmap.getHeight(); y++)
        {
            for (int x = 0; x < lightBitmap.getWidth(); x++)
            {
                separatedColorArray[x][y] = new SeparatedColor(lightBitmap.getPixel(x, y));
            }
        }

        for (int y = 0; y < lightBitmap.getHeight(); y++)
        {
            for (int x = 0; x < lightBitmap.getWidth(); x++)
            {
                SeparatedColor oldColor = separatedColorArray[x][y];
                SeparatedColor newColor = oldColor.getNearestBlackWhiteColor();
                lightBitmap.setPixel(x, y, newColor.toRGB());

                newColor.reverseSub(oldColor);

                if (x < lightBitmap.getWidth() - 1)
                    separatedColorArray[x + 1][y].addAndMultiplyAndDivide16(newColor, 7);

                if (y < lightBitmap.getHeight() - 1)
                {
                    if (x > 0)
                        separatedColorArray[x - 1][y + 1].addAndMultiplyAndDivide16(newColor, 3);

                    separatedColorArray[x][y + 1].addAndMultiplyAndDivide16(newColor, 5);

                    if (x < lightBitmap.getWidth() - 1)
                        separatedColorArray[x + 1][y + 1].addAndMultiplyAndDivide16(newColor, 1);
                }
            }
        }

        return lightBitmap.toBitmap();
    }


    public static void writeIndexedPebblePNG(Bitmap bitmap, OutputStream stream)
    {
        writeIndexedPebblePNG(bitmap, stream, null);
    }

    public static void writeIndexedPebblePNG(Bitmap bitmap, OutputStream stream, Integer transparentColor)
    {
        LightBitmap lightBitmap = new LightBitmap(bitmap);
        ImageInfo imageInfo = new ImageInfo(lightBitmap.getWidth(), lightBitmap.getHeight(), 8, false, false, true);
        PngWriter pngWriter = new PngWriter(stream, imageInfo);

        PngChunkPLTE paletteChunk = pngWriter.getMetadata().createPLTEChunk();
        paletteChunk.setNentries(64);
        for (int i = 0; i < 64; i++)
        {
            int color = PEBBLE_TIME_PALETTE[i];
            paletteChunk.setEntry(i, Color.red(color), Color.green(color), Color.blue(color));
        }

        if (transparentColor != null)
        {
            transparentColor &= 0x00FFFFFF;

            Byte index = PEBBLE_TIME_PALETTE_MAP.get(transparentColor);
            if (index == null)
                throw new IllegalArgumentException("Color is not supported by Pebble Time: " + Integer.toHexString(transparentColor));

            PngChunkTRNS transparencyChunk = pngWriter.getMetadata().createTRNSChunk();

            int[] paletteAlpha = new int[64];
            for (int i = 0; i < 64; i++)
                paletteAlpha[i] = 255;
            paletteAlpha[index] = 0;
            transparencyChunk.setPalletteAlpha(paletteAlpha);
        }

        for (int y = 0; y < lightBitmap.getHeight(); y++)
        {
            ImageLineByte imageLine = new ImageLineByte(imageInfo);
            for (int x = 0; x < lightBitmap.getWidth(); x++)
            {
                int pixel = lightBitmap.getPixel(x, y) & 0x00FFFFFF;
                Byte index = PEBBLE_TIME_PALETTE_MAP.get(pixel);

                if (index == null)
                    throw new IllegalArgumentException("Color is not supported by Pebble Time: " + Integer.toHexString(pixel));

                imageLine.getScanline()[x] = index;
            }

            pngWriter.writeRow(imageLine, y);
        }

        pngWriter.end();
    }

    public static void writeMaskedTwoBitPng(Bitmap blackAndWhiteBitmap, OutputStream stream, boolean transparentBlack)
    {
        LightBitmap lightBitmap = new LightBitmap(blackAndWhiteBitmap);
        ImageInfo imageInfo = new ImageInfo(lightBitmap.getWidth(), lightBitmap.getHeight(), 1, false, true, false);
        PngWriter pngWriter = new PngWriter(stream, imageInfo);

        PngChunkTRNS transparencyChunk = pngWriter.getMetadata().createTRNSChunk();
        transparencyChunk.setGray(transparentBlack ? 0 : 1);

        for (int y = 0; y < lightBitmap.getHeight(); y++)
        {
            ImageLineByte imageLine = new ImageLineByte(imageInfo);
            for (int x = 0; x < lightBitmap.getWidth(); x++)
            {
                int pixel = lightBitmap.getPixel(x, y) & 0x00FFFFFF;
                int r = Color.red(pixel);

                imageLine.getScanline()[x] = (byte) (r > 255 / 2 ? 1 : 0);
            }

            pngWriter.writeRow(imageLine, y);
        }

        pngWriter.end();
    }




    public static byte[] getIndexedPebbleImageBytes(Bitmap bitmap)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        writeIndexedPebblePNG(bitmap, outputStream);
        return outputStream.toByteArray();
    }

    public static byte getGColor8FromRGBColor(int color)
    {
        int r = Math.round(Color.red(color) / 85f);
        int g = Math.round(Color.green(color) / 85f);
        int b = Math.round(Color.blue(color) / 85f);

        return (byte) (0b11000000 | (r << 4) | (g << 2) | b);
    }

    public static class SeparatedColor
    {
        private int r;
        private int g;
        private int b;

        public SeparatedColor(int rgb)
        {
            this.r = Color.red(rgb);
            this.g = Color.green(rgb);
            this.b = Color.blue(rgb);
        }

        public SeparatedColor(int r, int g, int b)
        {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public void add(SeparatedColor other)
        {
            r += other.r;
            g += other.g;
            b += other.b;
        }

        public void sub(SeparatedColor other)
        {
            r -= other.r;
            g -= other.g;
            b -= other.b;
        }

        public void reverseSub(SeparatedColor other)
        {
            r = other.r - r;
            g = other.g - g;
            b = other.b - b;
        }

        public void multiply(double scalar)
        {
            r *= scalar;
            g *= scalar;
            b *= scalar;
        }

        public void addAndMultiplyAndDivide16(SeparatedColor quantError, int scalar)
        {
            r += quantError.r * scalar / 16;
            g += quantError.g * scalar / 16;
            b += quantError.b * scalar / 16;
        }


        public SeparatedColor getNearestPebbleTimeColor()
        {
            return new SeparatedColor((int) (r / 85f + 0.5f) * 85, (int) (g / 85f + 0.5f) * 85, (int) (b / 85f + 0.5f) * 85);
        }

        public SeparatedColor getNearestBlackWhiteColor()
        {
            int luma = (r + r + b + g + g + g) / 6;
            int color = luma > 255 / 2 ? 255 : 0;
            return new SeparatedColor(color, color, color);
        }


        public SeparatedColor copy()
        {
            return new SeparatedColor(r, g, b);
        }

        public int toRGB()
        {
            return Color.rgb(r, g, b);
        }
    }
}
