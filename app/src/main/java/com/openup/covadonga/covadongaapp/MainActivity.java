package com.openup.covadonga.covadongaapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.openup.covadonga.covadongaapp.util.AdOrg;
import com.openup.covadonga.covadongaapp.util.CovadongaDB;
import com.openup.covadonga.covadongaapp.util.DBHelper;
import com.openup.covadonga.covadongaapp.util.Env;
import com.openup.covadonga.covadongaapp.util.InitialLoad;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private Activity mCtx = null;
    private String userIn="";
    private String pswIn="";
    private Button  btnLogIn;
    private Boolean retornoWS=false;
    private ProgressDialog pDialog;
    private String TAG  = "EBP";
    private Spinner spAdOrg;

    private EditText txtUser;
    private EditText txtPsw;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCtx = this;
        getViewElement();
        setActions();

        testDataBase();
        makeChangesOnDB();

        loadSpinnerData();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getViewElement(){
        btnLogIn = (Button) findViewById(R.id.btnLogin);
        txtUser = (EditText) findViewById(R.id.eTxtUserName);
        txtPsw = (EditText) findViewById(R.id.eTxtPassword);
        spAdOrg = (Spinner) findViewById(R.id.spinAdOrg);
    }

    public void setActions(){
        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userIn = txtUser.getText().toString();
                pswIn = txtPsw.getText().toString();
                //if(u.equals("admin") && p.equals("admin")){
                if (isOnline()) {
                    loginWS(userIn, pswIn);
                } else {

                    CharSequence text = getResources().getString(R.string.noInternet);
                    Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }


    private void startMenuActivity() {
        Intent i = new Intent(this, MenuActivity.class);
        startActivity(i);
    }

    private void testDataBase() {
        // Validate SD
        if(Env.isEnvLoad(this)){
            if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                if(!Env.getDB_PathName(this).equals(CovadongaDB.DB_NAME)){
                    finish();
                }
            }
        } else {
            InitialLoad initData = new InitialLoad(this);
            initData.initialLoad_copyDB();
        }
    }

    private boolean loginWS(String u, String p) {
        final Env e = new Env();
        final String[] adUsr = {""};
        pDialog = ProgressDialog.show(this, null, "Consultando..", true);
        new Thread(){
            public void run(){
                try{
                    adUsr[0] = loginWebServer(userIn,pswIn);
                }catch (Exception e){
                    e.getMessage();
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                pDialog.dismiss();
                (mCtx).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (adUsr[0].equals("-2")) {
                            CharSequence text =  getResources().getString(R.string.user_pws_error);
                            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                        } else if(adUsr[0].equals("-1")) {
                            CharSequence text =  getResources().getString(R.string.user_wh_error);
                            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                        } else if(adUsr[0].equals("")) {
                            CharSequence text =  "Error de Conexion. Intente denuevo.";
                            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                        }else if(Integer.valueOf(adUsr[0]) > 0){
                            e.setUser(userIn);
                            e.setPass(pswIn);
                            AdOrg org = (AdOrg) spAdOrg.getSelectedItem();
                            e.setAdOrgId(org.getAdOrgId());
                            e.setadusr(adUsr[0]);
                            startMenuActivity();
                        }else{
                            CharSequence text =  "Error! Intente denuevo.";
                            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }.start();
        return retornoWS;
    }

    private String loginWebServer(String userIn, String pswIn)
    {
        String reg = "";

        final String NAMESPACE = Env.NAMESPACE;
        final String URL=Env.URL;
        final String METHOD_NAME = "login";
        final String SOAP_ACTION = "http://3e.pl/ADInterface/ADServicePortType/loginRequest";

        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        SoapObject adLoginRequest = new SoapObject(NAMESPACE,"ADLoginRequest");
        PropertyInfo usrPI= new PropertyInfo();
        usrPI.setName("user");
        usrPI.setValue(userIn);
        usrPI.setNamespace(NAMESPACE);
        usrPI.setType(String.class);
        adLoginRequest.addProperty(usrPI);

        PropertyInfo pswPI= new PropertyInfo();
        pswPI.setName("pass");
        pswPI.setValue(pswIn);
        pswPI.setNamespace(NAMESPACE);
        pswPI.setType(String.class);
        adLoginRequest.addProperty(pswPI);

        PropertyInfo stage= new PropertyInfo();
        stage.setName("stage");
        stage.setValue(0);
        stage.setNamespace(NAMESPACE);
        stage.setType(String.class);
        adLoginRequest.addProperty(stage);

        request.addSoapObject(adLoginRequest);
        SoapSerializationEnvelope envelope =
                new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = false;

        envelope.setOutputSoapObject(request);
        HttpTransportSE transporte = new HttpTransportSE(URL);
        try
        {
            transporte.call(SOAP_ACTION, envelope);
            SoapObject resultado_xml =(SoapObject)envelope.getResponse();
            SoapObject resSoap1 = (SoapObject)resultado_xml.getProperty(0);
            SoapObject resSoap2 = (SoapObject)resSoap1.getProperty(0);

            String res = resSoap2.getAttribute(1).toString();

            if(res.equals("-2"))
            {
                Log.d(TAG, "Usuario no registrado o Error en User o Pass.");
                reg = "-2";
            }else if(res.equals("-1")){
                Log.d(TAG, "Usuario sin Warehouse.");
                reg = "-1";
            }else{
                reg = res;
            }

        }
        catch (Exception e)
        {
            Log.d(TAG, "Error registro en mi servidor: " + e.getCause() + " || " + e.getMessage());
        }
        return reg;
    }

    private boolean isOnline(){
        Boolean ret = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                ret = true;
            }
        }catch (Exception e){
            e.getMessage();
        }
        return ret;
    }


    public List < AdOrg> getAllLabels(){
        DBHelper db = new DBHelper(this);
        List< AdOrg > labels = new ArrayList < AdOrg > ();
        try{
            db.openDB(0);
            Cursor rs = db.querySQL("select ad_org_id, name from ad_org order by 2", null);

            // looping through all rows and adding to list
            if ( rs.moveToFirst () ) {
                do {
                    labels.add ( new AdOrg ( rs.getInt(0) , rs.getString(1) ) );
                } while (rs.moveToNext());
            }

        }catch (Exception e){
            //Generar el dialogo de error
            e.getMessage();
        }finally {
            db.close();
        }
        // returning labels
        return labels;
    }

    private void loadSpinnerData() {
        // Spinner Drop down elements
        List <AdOrg> lables = getAllLabels();
        // Creating adapter for spinner
        ArrayAdapter<AdOrg> dataAdapter = new ArrayAdapter<AdOrg>(this,
                android.R.layout.simple_spinner_item, lables);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spAdOrg.setAdapter(dataAdapter);
    }

    private void makeChangesOnDB(){
        //EB - #7150
        DBHelper db = new DBHelper(this);
        try{
            db.openDB(1);
            if(!db.existsColumnInTable(db, "m_product", "value")){
                db.executeSQL("ALTER TABLE m_product ADD COLUMN value TEXT;");
            }

        }catch (Exception e){
            //Generar el dialogo de error
            e.getMessage();
        }finally {
            db.close();
        }
    }
}
