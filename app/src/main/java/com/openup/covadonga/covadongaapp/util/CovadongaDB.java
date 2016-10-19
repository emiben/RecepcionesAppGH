package com.openup.covadonga.covadongaapp.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.io.File;
import java.sql.Connection;

/**
 * Created by Emilino on 07/09/2015.
 */
public class CovadongaDB extends SQLiteOpenHelper {
    private String 				sqlCreate;
    private String 				sqlUpdate;
    private SQLiteDatabase db;
    private SQLiteStatement stm;
    public static final int 	READ_ONLY = 0;
    public static final int 	READ_WRITE = 1;
    public static final String 	DB_NAME = "dbCovadonga";
    public static final String 	DB_DIRECTORY = "DataBaseApp";
    public static final int 	DB_VERSION = 1;
    private Context ctx;

    private Connection _Connection = null;
    /**
     * @param context
     * @param name
     * @param factory
     * @param version
     */

    public CovadongaDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param context
     * @param name
     * @param factory
     * @param version
     * @param errorHandler
     */

    public CovadongaDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version,
                   DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
        // TODO Auto-generated constructor stub
    }

    //Implementaciones
	 /* *** Constructor ***
	 * @author OpenUp
	 * @param ctx
	 */
    public CovadongaDB(Context ctx){
        super(ctx, Env.getDB_PathName(ctx), null, DB_VERSION);
        this.ctx = ctx;
    }

    public static String getDB_Path() {
        return File.separator +
                Env.APP_DIRECTORY +
                File.separator +
                DB_DIRECTORY;
    }

    public static String getDB_PathName() {
        return File.separator +
                Env.APP_DIRECTORY +
                File.separator +
                DB_DIRECTORY +
                File.separator +
                DB_NAME;
    }

    /**
     * Open database in mode read or read write
     * @author OpenUp
     * @param type
     * @return
     * @return SQLiteDatabase
     */
    public SQLiteDatabase openDB(int type){
        if(type == READ_ONLY){
            db = getReadableDatabase();
        }else if(type == READ_WRITE){
            db = getWritableDatabase();
        }
        return db;
    }

    /**
     * Get SQL with parameters
     * @author OpenUp
     * @param sql
     * @param values
     * @return
     * @return Cursor
     */
    public Cursor querySQL(String sql, String [] values){
        //LogM.log(ctx, getClass(), Level.FINE, "SQL=" + sql);
        return db.rawQuery(sql, values);
    }

    /**
     * Close DB
     * @author OpenUp
     * @param rs
     * @return void
     */
    public void closeDB(Cursor rs){
        if(rs != null && !rs.isClosed())
            rs.close();
        db.close();
        //LogM.log(ctx, getClass(), Level.INFO, "Closed");
    }

    /**
     * Update table
     * @author OpenUp
     * @param table
     * @param values
     * @param where
     * @param argmWhere
     * @return void
     */
    public void updateSQL(String table, ContentValues values, String where, String [] argmWhere){
        db.update(table, values, where, argmWhere);
    }

    /**
     * Execute SQL
     * @author OpenUp
     * @param sql
     * @return void
     */
    public void executeSQL(String sql){
        //LogM.log(ctx, getClass(), Level.FINE, "SQL=" + sql);
        db.execSQL(sql);
    }

    /**
     * Execute SQL with parameters
     * @author OpenUp
     * @param sql
     * @param param
     * @return void
     */
    public void executeSQL(String sql, Object [] param){
        //LogM.log(ctx, getClass(), Level.FINE, "SQL=" + sql);
        db.execSQL(sql, param);
    }

    /**
     * Insert on table
     * @author OpenUp
     * @param table
     * @param columnaNull
     * @param values
     * @return void
     */
    public long insertSQL(String table, String columnaNull, ContentValues values){
        return db.insert(table, columnaNull, values);
    }

    public SQLiteDatabase getDB(){
        return db;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
