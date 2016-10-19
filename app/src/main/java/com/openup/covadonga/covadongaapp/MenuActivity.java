package com.openup.covadonga.covadongaapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.openup.covadonga.covadongaapp.util.DBHelper;
import com.openup.covadonga.covadongaapp.util.SincronizeData;
import com.openup.covadonga.covadongaapp.util.WebServices;

import org.ksoap2.serialization.SoapObject;


public class MenuActivity extends ActionBarActivity {

    private Button          btnProcessPO;
    private Button          btnSincProv;
    private Button          btnSincUPC;
    private Button          btnSincOrders;
    private Button          btnReport;
    private Button          btnSettings;
    private ProgressDialog  pDialog;
    private Activity        mYourActivityContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mYourActivityContext = this;
        getViewElements();
        setActions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_menu, menu);
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

    public void getViewElements(){
        btnProcessPO = (Button) findViewById(R.id.btnProcessPO);
        btnSincProv = (Button) findViewById(R.id.btnSincProv);
        btnSincUPC = (Button) findViewById(R.id.btnSincUPC);
        btnSincOrders = (Button) findViewById(R.id.btnSincOrders);
        btnReport = (Button) findViewById(R.id.btnReports);
        btnSettings = (Button) findViewById(R.id.btnSettings);
    }

    public void setActions(){
        final Context ctx = this;
        btnProcessPO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startListaClienteActivity();
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    // Add the buttons
                builder.setTitle("Recepcion con Orden de Compra?");
                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startListaClienteActivity(0);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startListaClienteActivity(1);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        btnSincProv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sincronizarProv();
            }
        });

        btnSincUPC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sincronizarUPC();
            }
        });

        btnSincOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sincronizarOrders();
            }
        });

        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startReportsActivity();
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSettingsActivity();
            }
        });

    }

    public void printUPCErrors(String[] resu){
        String[] print = resu[0].split(";");
        for(int i = 0; i < print.length; i++){
            Toast.makeText(this, print[i], Toast.LENGTH_SHORT).show();
        }
    }

    private void startListaClienteActivity(int recType) {
        Bundle bundle = new Bundle();
        bundle.putInt("recType", recType);
        Intent i = new Intent(this, ListaProveedorActivity.class);
        i.putExtras(bundle);
        startActivity(i);
    }

    private void startReportsActivity() {
        Intent i = new Intent(this, ReportesActivity.class);
        startActivity(i);
    }

    private void startSettingsActivity() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    public void sincronizarProv() {
        pDialog = ProgressDialog.show(this, null, "Consultando datos...", true);
        final Activity finalThis = mYourActivityContext;
        new Thread() {
            public void run() {
                try {
                    sincronizar();
                } catch (final Exception e) {
                    e.getMessage();
                    finalThis.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                            "Error! Por favor intente nuevamente. Desc: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
//                    Toast.makeText(getApplicationContext(),
//                            "Error! Por favor intente nuevamente. Desc: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                pDialog.dismiss();
            }
        }.start();
    }

    public void sincronizarUPC(){
        pDialog = ProgressDialog.show(this, null, "Enviando datos...", true);
        final String[] resp = {""};
        final SincronizeData sd = new SincronizeData();
        new Thread() {
            public void run() {
                try {
                    resp[0] = sd.sendUPC();
                } catch (Exception e) {
                    e.getMessage();
                }
                pDialog.dismiss();
                ((Activity) mYourActivityContext).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if(!resp[0].equals("")){
                            printUPCErrors(resp);
                        }
                    }
                });
            }
        }.start();
    }

    private void sincronizar(){

        WebServices ws = new WebServices();
        String[] columYVal = new String[4];
        SoapObject resultado_xml = null;
        int i = 0;
        columYVal[i++] = "IsVendor"; //colum
        columYVal[i++] = "Y"; //val

        columYVal[i++] = "IsActive"; //colum
        columYVal[i++] = "Y"; //val

        resultado_xml = ws.webServiceQry("QueryCBPartner", "C_BPartner", columYVal);
        if(ws.getMessage() == "EOFException"){
            resultado_xml = ws.webServiceQry("QueryCBPartner", "C_BPartner", columYVal);
        }else if(ws.getMessage() == "Error!!"){
            Toast.makeText(getApplicationContext(),
                    "Error! Por favor intente nuevamente!!", Toast.LENGTH_SHORT).show();
        }else{
            insertVendors(resultado_xml);
        }
    }

    private void insertVendors(SoapObject so){

        SoapObject dataResult = (SoapObject)so.getProperty(0);

        int tam = dataResult.getPropertyCount();
        String delims = "[=;]";

        DBHelper db = new DBHelper(this);
        db.openDB(1);
        db.executeSQL("DELETE FROM c_bpartner");

        try{
            if(tam > 0) {
                for (int i = 0; i < tam; i++) {
                    SoapObject dataRow = (SoapObject) dataResult.getProperty(i);
                    String col1[] = dataRow.getProperty(0).toString().split(delims); //C_BPartner_ID
                    String col2[] = dataRow.getProperty(1).toString().split(delims); //Created
                    String col3[] = dataRow.getProperty(2).toString().split(delims); //IsRecieptPO--
                    String col4[] = dataRow.getProperty(3).toString().split(delims); //m_pricelist_id--
                    String col5[] = dataRow.getProperty(4).toString().split(delims); //Name
                    String col6[] = dataRow.getProperty(5).toString().split(delims); //Name2
                    String col7[] = dataRow.getProperty(6).toString().split(delims); //Updated

                    if(col4[1].toString().equals("null")){
                        col4[1] = String.valueOf(0);
                    }
                    String qry = "Insert into c_bpartner values (";
                    qry = qry + col4[1] + ",'" + col3[1] + "'," + col1[1] + ",'" + col2[1] + "','" + col7[1] + "','";
                    qry = qry + col5[1].replace("'", "") + "','" + col6[1].replace("'", "") +"')";

                    db.executeSQL(qry);
                }
            }
        } catch (Exception e){
            System.out.print(e);
        }finally {
            db.close();
        }
    }

    public void sincronizarOrders(){
        pDialog = ProgressDialog.show(this, null, "Enviando datos...", true);
        final SincronizeData sd = new SincronizeData();
        new Thread() {
            public void run() {
                try {
                    sd.sendOrders();
                } catch (Exception e) {
                    e.getMessage();
                }
                pDialog.dismiss();
            }
        }.start();
    }



}
