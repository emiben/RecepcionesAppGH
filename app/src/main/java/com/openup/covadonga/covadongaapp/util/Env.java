package com.openup.covadonga.covadongaapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Emilino on 07/09/2015.
 */
public class Env {
    /**
     * Notificaciones

     */
    private static int notificationsCount=0;
    private static final String PROPERTY_REG_ID = "PID";
    private static final String PROPERTY_USER = "PU";
    private static final String PROPERTY_APP_VERSION = "PAV";
    private static final String PROPERTY_EXPIRATION_TIME = "PET";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1 ;
    private static final String SENDER_ID = "585544263746";
    private static final long EXPIRATION_TIME_MS = 1;
    private static int  entradaScan = 1;

    public static final String NAMESPACE = "http://3e.pl/ADInterface";


    //planeta
    public static final String clientURL = "http://planetauruguay.ddns.net:8273";
    //En OpUp
//    public static final String clientURL = "http://covadonga.noip.us:8273";
//    public static final String URL="http://covadonga.noip.us:8273/ADInterface-1.0/services/ADService";
    //local
    //public static final String clientURL = "http://192.168.13.113:8080";
    //En covadonga
    //public static final String clientURL = "http://10.0.0.254:8273";
    public static final String URL= clientURL + "/ADInterface-1.0/services/ADService";
    //public static final String URL="http://10.0.0.254:8273/ADInterface-1.0/services/ADService";
    //En base de Test
    //public static final String URL="http://200.71.26.66:6020/ADInterface-1.0/services/ADService";

    //local debug

//    public static final String URL="http://192.168.13.107:8080/ADInterface-1.0/services/ADService";

    //En base de Syl
//    public static final String URL="http://SBTPC:8080/ADInterface-1.0/services/ADService";

    /**************************************************************************
     *  User
     */

    public static String usr = null;
    public static String pass = null;
    public static String adUsr = null;
    public static int adOrgId = 1;

    /**************************************************************************
     *  Application Context
     */

    private static final String	SET_ENV = "#SET_ENV#";

    private final String APP_BASE_FOLDER = "AppCovadonga";
    private final String APP_DB_FOLDER = "DataBaseApp";
    private final String APP_DATA_FOLDER = "DataApp";

    /** Database Context*/
    public static final String		DB_VERSION = "#DB_Version";
    public static final String		DB_NAME = "#DB_Name";
    public static final String		APP_DIR_NAME = "#APP_DIR_Name";

    public static  final String DB_NAME_ASSETS = "dbCovadonga"; //name of file in assets folder
    /******************************************************************************
     * App Context
     */
    public static final String 		APP_DIRECTORY = "AppCovadonga";
    public static final String 		DOC_DIRECTORY = "Version";
    public static final String 		DOC_AUXILIAR = "Auxiliary";

    public static final String      DOC_QS = "QS";
    public static final String      ASSETS_QSOUT ="QSOUT" ;

    public static String getDB_PathName(Context ctx){
        return getContext(ctx, DB_NAME);
    }

    public static String getContext (Context ctx, String context)
    {
        if (ctx == null || context == null)
            throw new IllegalArgumentException ("Require Context");
        SharedPreferences pf = PreferenceManager.getDefaultSharedPreferences(ctx);
        return pf.getString(context, null);
    }	//	getContext

    public static int getNotificationsCount() {
        return notificationsCount;
    }

    public static void setNotificationsCount(int notCount) {
        notificationsCount = notCount;
    }
    public static boolean isEnvLoad(Context ctx){
        return getContextAsBoolean(ctx, SET_ENV);
    }

    public static boolean getContextAsBoolean (Context ctx, String context)
    {
        if (ctx == null || context == null)
            throw new IllegalArgumentException ("Require Context");

        String s = getContext(ctx, context);
        //
        return (s != null && s.equals("Y"));
    }	//	getContext

    public static void setDB_Path(Context ctx, String dbPathName) {
        // TODO Auto-generated method stub
        setContext(ctx, DB_NAME, dbPathName);
    }

    public static void setContext (Context ctx, String context, boolean value)
    {
        setContext(ctx, context, value ? "Y": "N");
    }	//	setContext

    public static void setContext (Context ctx, String context, String value){
        if (ctx == null || context == null)
            return;
        //
        if (value == null)
            value = "";
        SharedPreferences.Editor ep = getEditor(ctx);
        ep.putString(context, value);
        ep.commit();
    }	//	setContext

    public static void setContext(Context ctx, String context, int value) {
        // TODO Auto-generated method stub
        if (ctx == null || context == null)
            return;
        SharedPreferences.Editor ep = getEditor(ctx);
        ep.putString(context, String.valueOf(value));
        ep.commit();
    }	//	setContext

    private static SharedPreferences.Editor getEditor(Context ctx){
        SharedPreferences pf = PreferenceManager.getDefaultSharedPreferences(ctx);
        return pf.edit();
    }

    public static void setAppDirName(Context ctx, String value){
        setContext(ctx, APP_DIR_NAME, value);
    }

    public static void setIsEnvLoad(Context ctx, boolean value) {
        // TODO Auto-generated method stub
        setContext(ctx, SET_ENV, value);
    }


    public void setUser(String user){
        this.usr = user;
    }

    public String getUser(){
        return this.usr;
    }

    public void setPass(String password){
        this.pass = password;
    }

    public String getPass(){
        return this.pass;
    }

    public void setadusr(String adUser){
        this.adUsr = adUser;
    }

    public String getAdUsr(){
        return this.adUsr;
    }

    public static int getAdOrgId() {
        return adOrgId;
    }

    public static void setAdOrgId(int adOrgId) {
        Env.adOrgId = adOrgId;
    }

    //** metodos notifications
    public static String getDateFormatString(Date dateOrdered, String formato) {

        DateFormat fechaHora = new SimpleDateFormat(formato);
        String convertido = fechaHora.format(dateOrdered);

        return convertido;
    }

    public int getEntradaScan() {
        return entradaScan;
    }

    public void setEntradaScan(int entradaScan) {
        Env.entradaScan = entradaScan;
    }
}
