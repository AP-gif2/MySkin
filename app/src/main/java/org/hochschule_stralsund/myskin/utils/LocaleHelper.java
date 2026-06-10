package org.hochschule_stralsund.myskin.utils;

import android.content.Context;
import android.content.res.Configuration;

public class LocaleHelper {

    private static final String SELECTED_LANGUAGE = "selected_language";

    public static void setLocale(Context context, String languageCode) {
        Configuration config = new Configuration();
        config.setLocale(new java.util.Locale(languageCode));
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }

    public static String getLocale(Context context) {
        return context.getResources().getConfiguration().locale.getLanguage();
    }
}
