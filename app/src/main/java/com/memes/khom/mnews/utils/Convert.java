package com.memes.khom.mnews.utils;


import android.annotation.SuppressLint;
import android.content.Context;

import com.memes.khom.memsnews.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Convert {

    //--------------------- Date -----------------------------------

    public static Calendar getDateFromString(String strDate) {

        Calendar calendar = Calendar.getInstance();

        String[] formats = new String[]{"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss.SSS", "HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss", "dd.MM.yyyy"};

        for (int i = 0; i < formats.length; i++) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat(formats[i], Locale.US);
                java.util.Date date = formatter.parse(strDate);
                calendar.setTime(date);
                return calendar;
            } catch (Exception ex) {
                if (i == formats.length - 1)        //that was a last chance
                    throw new RuntimeException(ex);    //no success on this point, throw an exception
            }
        }

        return calendar;
    }


    public static String getDateTimeFromDouble(Long date) {
        String res = "";
        try {
            DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            Date input = new Date(date);
            res = inputFormat.format(input);
            return res;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return res;
    }

    //1 minute = 60 seconds
    //1 hour = 60 x 60 = 3600
    //1 day = 3600 x 24 = 86400
    @SuppressLint("DefaultLocale")
    public static String printDifference(Long startDate, Long endDate, Context cnx) {

        String res;
        //milliseconds
        long different = endDate - startDate;

        //System.out.println("startDate : " + startDate);
        //System.out.println("endDate : " + endDate);
       // System.out.println("different : " + different);

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;

        // different = different % minutesInMilli;
        // long elapsedSeconds = different / secondsInMilli;

        if (elapsedDays > 0) res = String.format("%d " + cnx.getString(R.string.days) + " ",
                elapsedDays);
        else if (elapsedHours > 0) {

            res = String.format("%d " + cnx.getString(R.string.hours) + " ",
                    elapsedHours);

        } else res = String.format("%d " + cnx.getString(R.string.minutes) + " ",
                elapsedMinutes);

        return res + cnx.getString(R.string.time_ago);
    }

}
