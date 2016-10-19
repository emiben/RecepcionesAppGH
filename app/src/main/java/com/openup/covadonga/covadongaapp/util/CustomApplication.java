package com.openup.covadonga.covadongaapp.util;

import android.app.Application;
import android.content.Context;

/**
 * Created by Emilino on 11/09/2015.
 */
public class CustomApplication extends Application {
    private static Context context;

    public void onCreate(){
        context=getApplicationContext();
    }

    public static Context getCustomAppContext(){
        return context;
    }
}

