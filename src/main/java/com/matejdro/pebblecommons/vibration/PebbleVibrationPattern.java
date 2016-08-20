package com.matejdro.pebblecommons.vibration;

import com.matejdro.pebblecommons.util.TextUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PebbleVibrationPattern
{
    public static final int MAX_SEGMENTS = 20;
    public static final int MAX_LENGTH_MS = 10000;

    public static final List<Byte> EMPTY_VIBRATION_PATTERN = Arrays.asList(new Byte[]{0, 0});

    public static List<Byte> parseVibrationPattern(String pattern)
    {
        String split[] = pattern.split(",");

        List<Byte> bytes = new ArrayList<>(MAX_SEGMENTS * 2);
        int max = Math.min(MAX_SEGMENTS, split.length);
        int total = 0;

        for (int i = 0; i < max; i++)
        {
            try
            {
                int segment = Integer.parseInt(split[i].trim());
                segment = Math.min(segment, MAX_LENGTH_MS - total);
                total += segment;

                bytes.add((byte) (segment & 0xFF));
                bytes.add((byte) ((segment >> 8) & 0xFF));

                if (total >= MAX_LENGTH_MS)
                    break;

            } catch (NumberFormatException e)
            {
            }
        }

        if (bytes.size() == 0)
        {
            bytes.add((byte) 0);
            bytes.add((byte) 0);
        }

        return bytes;
    }

    public static List<Byte> getFromAndroidVibrationPattern(long[] pattern)
    {
        List<Byte> bytes = new ArrayList<>(MAX_SEGMENTS * 2);
        int max = Math.min(MAX_SEGMENTS, pattern.length);
        int total = 0;

        //Android pattern has one extra pause, so we start at first vibration (pause,vib,pause,vib... instead of vib,pause,vib,pause...)
        for (int i = 1; i < max; i++)
        {
            long segment = pattern[i];
            segment = Math.min(segment, MAX_LENGTH_MS - total);
            total += segment;

            bytes.add((byte) (segment & 0xFF));
            bytes.add((byte) ((segment >> 8) & 0xFF));

            if (total >= MAX_LENGTH_MS) //Maximum total vibration length is 10000 for now
                break;
        }

        if (bytes.size() == 0)
        {
            bytes.add((byte) 0);
            bytes.add((byte) 0);
        }

        return bytes;
    }

    public static long[] convertToAndroidPattern(String pebblePattern)
    {
        List<Long> androidPattern = new ArrayList<>();
        androidPattern.add(0L); // Add beginning pause

        String[] split = pebblePattern.split(",");

        for (String patternSegmentString : split)
        {
            try
            {
                int segment = Integer.parseInt(patternSegmentString.trim());
                androidPattern.add((long) segment);

            } catch (NumberFormatException e)
            {
            }

        }

        long[] primitiveAndroidPattern = new long[androidPattern.size()];
        for (int i = 0; i < androidPattern.size(); i++)
        {
            primitiveAndroidPattern[i] = androidPattern.get(i);
        }

        return primitiveAndroidPattern;
    }

    public static boolean validateVibrationPattern(String pattern)
    {
        if (pattern.trim().isEmpty())
            return false;

        String split[] = pattern.split(",", -1);

        for (String segment : split)
        {
            try
            {
                String trimmedSegment = segment.trim();
                Integer number = Integer.parseInt(trimmedSegment);
                if (number < 0)
                    return false;
            }
            catch (NumberFormatException e)
            {
                return false;
            }
        }

        return true;
    }
}
