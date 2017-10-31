package com.memes.khom.mnews.utils;


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
                "yyyy-MM-dd'T'HH:mm:ss","dd.MM.yyyy"};

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

}
