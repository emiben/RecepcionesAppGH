package com.openup.covadonga.covadongaapp.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Emilino on 07/09/2015.
 */
public class InitialLoad {
    private Context ctx;
    private static final String KEY_POS_TAB= "poTab";

    /** SBouissa 09/06/2015
     *	Contructor vacio para permitir obtener sql connection
     */
    public InitialLoad() {
        ctx = null;
    }

    /**
     *	Contructor
     */
    public InitialLoad(Context ctxIn) {
        ctx = ctxIn;
    }

    public void initialLoad_copyDB() {
        if(!Env.isEnvLoad(ctx)){
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                String docPath = Environment.getExternalStorageDirectory()
                        + File.separator + Env.APP_DIRECTORY
                        + File.separator + Env.DOC_DIRECTORY;//exteralstoraje/AppIBos
                String auxPath = Environment.getExternalStorageDirectory()
                        + File.separator + Env.APP_DIRECTORY
                        + File.separator + Env.DOC_AUXILIAR;//exteralstoraje/AppIBos

                String qsPath = Environment.getExternalStorageDirectory()
                        + File.separator + Env.APP_DIRECTORY
                        + File.separator + Env.DOC_QS;// - AppIBoS/QS

                String dbPath = Environment.getExternalStorageDirectory()
                        + CovadongaDB.getDB_Path();
                String dbPathName = Environment.getExternalStorageDirectory()
                        + CovadongaDB.getDB_PathName();
                File f = new File (dbPath);
                if(!f.exists()){
                    if(f.mkdirs()){
                        Env.setDB_Path(ctx,dbPathName);
                    }else {
                        Env.setDB_Path(ctx, CovadongaDB.DB_NAME);
                    }
                }else if(f.isDirectory()){
                    File fDB = new File(dbPathName);
                    fDB.delete();
                    Env.setDB_Path(ctx,dbPathName);
                }else if(f.isFile()) {
                    if (f.mkdirs()) {
                        Env.setDB_Path(ctx, dbPathName);
                    } else {
                        Env.setDB_Path(ctx, CovadongaDB.DB_NAME);
                    }
                }
                f.canRead();f.canWrite();f.canExecute();
                //Create document folder
                File doc = new File(docPath);
                if(!doc.exists() || doc.isFile()){
                    if(doc.mkdirs()){
                        Env.setAppDirName(ctx,docPath);
                    }else{
                        Env.setAppDirName(ctx,"");
                    }
                }else{
                    Env.setAppDirName(ctx,docPath);
                }
                doc.canRead();doc.canWrite();doc.canExecute();
                //Create auxiliar folder
                File aux = new File(auxPath);
                if(!aux.exists() || aux.isFile()){
                    if(aux.mkdirs()){
                        // Env.setAppDirName(ctx,auxPath);
                    }else{
                        // Env.setAppDirName(ctx,"");
                    }
                }
                aux.canRead();aux.canWrite();aux.canExecute();
                //Create QS folder
                aux.canRead();aux.canWrite();aux.canExecute();
                File qs = new File(qsPath);
                if(!qs.exists()||qs.isFile()){
                    if(qs.mkdirs()){
                        qs.canRead();qs.canWrite(); qs.canExecute();
                        copyFilesFromAssetsQS(qs);
                    }else{

                    }
                }
            }else{
                Env.setDB_Path(ctx, CovadongaDB.DB_NAME);
            }
            OutputStream outputStream= null;
            try{
                AssetManager assetManager = ctx.getResources().getAssets();
                //AssetFileDescriptor afd = assetManager.openFd(OpUpDB.DB_NAME);
                InputStream is1 = assetManager.open(Env.DB_NAME_ASSETS);
                // InputStream is = ctx.getResources().getAssets().open(OpUpDB.DB_NAME);
                String path = Env.getDB_PathName(ctx);
                File f = new File(path);
                outputStream = new FileOutputStream(f);
                int buffersize = 9048576;
                byte[] buffer = new byte[buffersize];
                int available = 0;
                //Copy
                while((available = is1.read(buffer)) >= 0){
                    outputStream.write(buffer,0,available);
                }
                //Close
                outputStream.close();
                //Set Conetext
                setContextTest();
            }catch (IOException io){
                io.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }finally {

            }
            loadContext();
            //MessageIBos.toastMsg(ctx,"Load DV.. Ok!!");
        }
    }

    private void copyFilesFromAssetsQS(File dst) {
        try{
            AssetManager assetManager = ctx.getResources().getAssets();
            String[] files = assetManager.list(Env.ASSETS_QSOUT);
            if (files.length > 0) {
                for(int i = 0; i<files.length;i++ ){
                    InputStream is1 = assetManager.open(Env.ASSETS_QSOUT+File.separator+files[i]);
                    // InputStream is = ctx.getResources().getAssets().open(OpUpDB.DB_NAME);
                    FileOutputStream outputStream = new FileOutputStream(dst+File.separator+files[i]);
                    int buffersize = 9048576;
                    byte[] buffer = new byte[buffersize];
                    int available = 0;
                    //Copy
                    while((available = is1.read(buffer)) >= 0){
                        outputStream.write(buffer,0,available);
                    }
                    //Close
                    outputStream.close();
                    //copyFile(new File(),dst);
                }
            }else{

            }
        }catch (IOException io){
            io.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setContextTest() {
        Env.setIsEnvLoad(ctx, true);
        Env.setContext(ctx, KEY_POS_TAB, 1);
        Env.setContext(ctx, "#Timeout", 10000000);
    }

    private void loadContext() {

        // Carga variables en contexto si la sincronizacion fue exitosa


        if (Env.isEnvLoad(ctx))
        {
            //Consultar luego la clase que se encarga de leer de la base de datos
            CovadongaDB con=null;
            Cursor rs = null;
            String sql = new String("SELECT ID_ROUTE, UPDATE_USER FROM GCGT_RE_ROUTE LIMIT 1");
            try{
                con = new CovadongaDB(ctx);
                con.openDB(CovadongaDB.READ_ONLY);
                rs = con.querySQL(sql, null);
                if(rs.moveToFirst()){
                    do {
                        Env.setContext(ctx, "#" + rs.getInt(0), rs.getString(1));
                    } while(rs.moveToNext());
                }
            }catch(Exception e){
                String mje = e.getMessage();
            }finally{
                con.closeDB(rs);
            }

        }
    }
}
