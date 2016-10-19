package com.openup.covadonga.covadongaapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.openup.covadonga.covadongaapp.util.CustomApplication;
import com.openup.covadonga.covadongaapp.util.DBHelper;

import java.util.Calendar;


public class IngresoFacturasActivity extends ActionBarActivity {

    private String      strOrdenes;
    private String      prov;
    private String[]    ordenes;
    private int         tam;
    private Spinner     spinOrders;
    private EditText    serieFact;
    private EditText    noFact;
    private EditText    fecFact;
    private Button      btnInsert;
    private Button      btnAtras;
    private Button      btnSig;
    private int         day;
    private int         month;
    private int         year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingreso_facturas);


        getViewItems();
        getOrdersInfo();
        setActions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ingreso_facturas, menu);
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

    public void getViewItems(){
        spinOrders = (Spinner) findViewById(R.id.spinOrders);
        serieFact = (EditText) findViewById(R.id.etSerieFact);
        noFact = (EditText) findViewById(R.id.etNoFact);
        fecFact = (EditText) findViewById(R.id.etFecFact);
        btnInsert = (Button) findViewById(R.id.btnInsertFact);
        btnAtras = (Button) findViewById(R.id.btnBackFact);
        btnSig = (Button) findViewById(R.id.btnOkFact);

    }

    public void setActions(){
        btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertFact();
            }
        });

        fecFact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(1);
            }
        });

        btnSig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(invoicesCheck() == 0){
                    startProcesarOrdenActivity();
                }
            }
        });

        btnAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startListaOrdenesActivity();
                finish();
            }
        });
    }

    private void startListaOrdenesActivity() {
        Intent i = new Intent(this, ListaOrdenesActivity.class);
        Bundle b = new Bundle();
        b.putString("Prov", prov);

        i.putExtras(b);
        this.finish();
        startActivity(i);
    }


    public void getOrdersInfo() {
        // get the Intent that started this Activity
        Intent in = getIntent();
        // get the Bundle that stores the data of this Activity
        Bundle b = in.getExtras();
        if (null != b) {
            strOrdenes = b.getString("Ordenes");
            ordenes = b.getString("Ordenes").split(";");
            tam = ordenes.length;
            prov = b.getString("Prov");
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ordenes);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinOrders.setAdapter(spinnerArrayAdapter);
    }

    public void insertFact(){
        if(noFact.getText().toString().equals("") || fecFact.getText().toString().equals("")
                || serieFact.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "Por favor ingrese cantidad Nro. Factura y Fecha!",
                    Toast.LENGTH_SHORT).show();
        }else{
            DBHelper db = null;
            //String docId = spinOrders.getSelectedItem().toString();
            int ordId;
            String factId = serieFact.getText().toString() + noFact.getText().toString();
            String[] parsFecha = fecFact.getText().toString().split("/");
            if(Integer.valueOf(parsFecha[1]) < 10){
                parsFecha[1] = "0"+parsFecha[1];
            }
            if(Integer.valueOf(parsFecha[0]) < 10){
                parsFecha[0] = "0"+parsFecha[0];
            }
            String fecha = parsFecha[2] + "-" + parsFecha[1] + "-" + parsFecha[0] + " 00:00:00";

            String docId = "";
            for(int i=0; i<spinOrders.getCount(); i++){
                docId += spinOrders.getItemAtPosition(i).toString() + ",";
            }

            if (docId != null && docId.length() > 0){
                docId = docId.substring(0, docId.length()-1);
            }

            try{
                db = new DBHelper(CustomApplication.getCustomAppContext());
                db.openDB(1);
                //String qryAux = "select c_order_id from c_order where documentno = " + docId;
                String qryAux = "select c_order_id, fecha from c_order where documentno in (" + docId + ")";
                Cursor rsAux = db.querySQL(qryAux, null);
                ContentValues cv = new ContentValues();

                if(rsAux.moveToFirst()){
                    do{
                        Calendar ordDate = Calendar.getInstance();
                        Calendar valDate = Calendar.getInstance();
                        ordId = rsAux.getInt(0);
                        if(ordId >= 1000000){
                            String strOrdDate[] = rsAux.getString(1).substring(0,10).split("-");
                            ordDate.set(Integer.valueOf(strOrdDate[0].toString()), Integer.valueOf(strOrdDate[1].toString()), Integer.valueOf(strOrdDate[2].toString()));
                            valDate.set(Integer.valueOf(parsFecha[2].toString()), Integer.valueOf(parsFecha[1].toString()), Integer.valueOf(parsFecha[0].toString()));
                        }

                        if (ordId >= 1000000 && valDate.before(ordDate)) {
                            Toast.makeText(getApplicationContext(), "No se puede guardar la Factura "+ factId + ". Fecha anterior a la OC!",
                                    Toast.LENGTH_LONG).show();
                        }else{
                            cv.put("factura_id", factId);
                            cv.put("c_order_id", ordId);
                            cv.put("fecha", fecha);

                            if(db.insertSQL("factura", null, cv) == -1){
                                Toast.makeText(getApplicationContext(), "Error al guardar la Factura!!",
                                        Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(getApplicationContext(), "Factura guradada!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }while (rsAux.moveToNext());
                }
                serieFact.setText("");
                noFact.setText("");
                fecFact.setText("");

            }catch (Exception e) {
                e.getMessage();
            } finally {
                db.close();
            }

        }

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(this, datePickerListener,
                year, month,day);
    }

    private DatePickerDialog.OnDateSetListener datePickerListener
            = new DatePickerDialog.OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {
            final Calendar c = Calendar.getInstance();
            year = selectedYear;
            month = selectedMonth;
            day = selectedDay;

            // set selected date into textview
            fecFact.setText(day+"/"+(month+1)+"/"+year);
        }
    };

    private void startProcesarOrdenActivity() {
        Intent i = new Intent(this, ProcesarOrdenActivity.class);
        Bundle b = new Bundle();
        b.putString("Ordenes", strOrdenes);
        i.putExtras(b);
        this.finish();
        startActivity(i);
    }

    private int invoicesCheck(){
        int tam = spinOrders.getCount();
        int res = 0;
        DBHelper db = null;
        Cursor rs = null;
        String docsId = "";

        for (int i = 0; i < tam; i++){
            if(tam == 1){
                docsId = "(" + spinOrders.getItemAtPosition(i).toString() + ")";
            }else if(i == 0){
                docsId = "(" + spinOrders.getItemAtPosition(i).toString() + ",";
            }else if(i == (tam-1)){
                docsId =  docsId + spinOrders.getItemAtPosition(i).toString() + ")";
            }else{
                docsId = docsId + ",";
            }
        }

        try{
            db = new DBHelper(CustomApplication.getCustomAppContext());
            db.openDB(0);
            String qryAux = "select documentno from c_order where documentno in " + docsId
                            + " and c_order_id not in"
                            + " (select c_order_id from factura where c_order_id in"
                            + " (select c_order_id from c_order where documentno in " + docsId + "))";
            rs = db.querySQL(qryAux, null);
            if(rs.moveToFirst()){
                do{
                    Toast.makeText(getApplicationContext(),
                            "La orden No. "+rs.getString(0)+" no tiene facturas asociadas!!",
                            Toast.LENGTH_SHORT).show();
                }while (rs.moveToNext());
                res = 1;
            }

        }catch (Exception e) {
            e.getMessage();
        } finally {
            db.close();
        }
        return res;
    }

}
