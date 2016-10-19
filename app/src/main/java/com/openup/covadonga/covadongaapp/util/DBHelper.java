package com.openup.covadonga.covadongaapp.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.io.File;
import java.sql.Connection;

/**
 * Created by Emilino on 07/09/2015.
 */
public class DBHelper extends SQLiteOpenHelper {
    private String sqlCreate;
    private String sqlUpdate;
    private SQLiteDatabase db;
    private SQLiteStatement stm;
    public static final int READ_ONLY = 0;
    public static final int READ_WRITE = 1;
    public static final String DB_NAME = "covadonga";
    public static final String DB_DIRECTORY = "DataBase";
    public static final int DB_VERSION = 1;
    private Context ctx;

    private Connection _Connection = null;

    /**
     * @param context
     * @param name
     * @param factory
     * @param version
     */

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public DBHelper(Context ctx) {
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

    public SQLiteDatabase openDB(int type) {
        if (type == READ_ONLY) {
            db = getReadableDatabase();
        } else if (type == READ_WRITE) {
            db = getWritableDatabase();
        }
        return db;
    }



    public boolean existsColumnInTable(DBHelper inDatabase, String inTable, String columnToCheck) {
        Cursor mCursor = null;
        try {
            // Query 1 row
            mCursor = inDatabase.querySQL("SELECT * FROM " + inTable + " LIMIT 0", null);

            // getColumnIndex() gives us the index (0 to ...) of the column - otherwise we get a -1
            if (mCursor.getColumnIndex(columnToCheck) != -1)
                return true;
            else
                return false;

        } catch (Exception Exp) {
            // Something went wrong. Missing the database? The table?
            System.out.print("When checking whether a column exists in the table, an error occurred: " + Exp.getMessage());
            return false;
        } finally {
            if (mCursor != null) mCursor.close();
        }
    }

    /**
     * Get SQL with parameters
     *
     * @param sql
     * @param values
     * @return Cursor
     * @author OpenUp
     */
    public Cursor querySQL(String sql, String[] values) {
        //LogM.log(ctx, getClass(), Level.FINE, "SQL=" + sql);
        return db.rawQuery(sql, values);
    }

    /**
     * Close DB
     *
     * @param rs
     * @return void
     * @author OpenUp
     */
    public void closeDB(Cursor rs) {
        if (rs != null && !rs.isClosed())
            rs.close();
        db.close();
        //LogM.log(ctx, getClass(), Level.INFO, "Closed");
    }

    /**
     * Update table
     *
     * @param table
     * @param values
     * @param where
     * @param argmWhere
     * @return void
     * @author OpenUp
     */
    public int updateSQL(String table, ContentValues values, String where, String[] argmWhere) {
        return db.update(table, values, where, argmWhere);
    }

    public int deleteSQL(String table, String whereClause, String[] whereArgs){
        return db.delete(table, whereClause, whereArgs);
    }

    /**
     * Execute SQL
     *
     * @param sql
     * @return void
     * @author OpenUp
     */
    public void executeSQL(String sql) {
        //LogM.log(ctx, getClass(), Level.FINE, "SQL=" + sql);
        db.execSQL(sql);
    }

    /**
     * Execute SQL with parameters
     *
     * @param sql
     * @param param
     * @return void
     * @author OpenUp
     */
    public void executeSQL(String sql, Object[] param) {
        //LogM.log(ctx, getClass(), Level.FINE, "SQL=" + sql);
        db.execSQL(sql, param);
    }

    /**
     * Insert on table
     *
     * @param table
     * @param columnaNull
     * @param values
     * @return void
     * @author OpenUp
     */
    public long insertSQL(String table, String columnaNull, ContentValues values) {
        return db.insert(table, columnaNull, values);
    }

    /**
     * Load a Connection
     *
     * @param conn
     * @param type
     * @return
     * @author sbouissa 15/07/2015
     */
    public static void loadConnection(DBHelper conn, int type) {
        if (conn != null
                && !conn.isOpen()) {
            conn.openDB(type);
            if (type == READ_WRITE)
                conn.beginTransaction();
        }
    }

    /**
     * Verifi if is open database
     *
     * @return boolean
     * @author sbouissa 15/07/2015
     */
    public boolean isOpen() {
        boolean ok = false;
        if (db != null) {
            ok = db.isOpen();
        }
        return ok;
    }

    /**
     * Begin transaction
     *
     * @return void
     * @author sbouissa 15/07/2015
     */
    public void beginTransaction() {
        db.beginTransaction();
    }

    /**
     * End transaction
     *
     * @return void
     * @author sbouissa 15/07/2015
     */
    public void endTransaction() {
        db.endTransaction();
    }

    public SQLiteDatabase getDB() {
        return db;
    }


    /**
     * Se recibe nombre de tabla a consultar clausula where (id = 123)
     *
     * @param table
     * @param where
     * @return
     */
    public static boolean exists(String table, String where, Context ctxIn) {
        boolean retValue = false;
        DBHelper conn = null;
        Cursor rs = null;
        try {
            String sql = "SELECT * FROM " + table + " WHERE " + where;
            conn = new DBHelper(ctxIn);
            loadConnection(conn, READ_ONLY);
            rs = conn.querySQL(sql, null);
            if (rs.moveToFirst()) {
                retValue = true;
            }
        } catch (SQLiteException e) {
            e.getMessage();
        } finally {
            conn.closeDB(rs);
        }
        //	Return
        return retValue;
    }


    /**
     * Se recibe nombre de tabla a consultar clausula where (id = 123)
     *
     * @param table
     * @param cols
     * @return
     */
    public static boolean inserting(String table, String cols, String values, Context ctxIn) {
        boolean retValue = false;
        DBHelper conn = null;
        Cursor rs = null;
        try {
            String sql = "INSERT INTO " + table + " " + cols + " VALUES(" + values + ")";
            conn = new DBHelper(ctxIn);
            loadConnection(conn, READ_ONLY);
            conn.executeSQL(sql);
        } catch (SQLiteException e) {
            e.getMessage();
        } finally {
            conn.closeDB(rs);
        }
        //	Return
        return retValue;
    }
}