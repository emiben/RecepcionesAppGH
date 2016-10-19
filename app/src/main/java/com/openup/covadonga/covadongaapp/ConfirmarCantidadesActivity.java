package com.openup.covadonga.covadongaapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.openup.covadonga.covadongaapp.util.CustomApplication;
import com.openup.covadonga.covadongaapp.util.DBHelper;

public class ConfirmarCantidadesActivity extends ActionBarActivity {

    private EditText    facturado;
    private EditText    recibido;
    private TextView    producto;
    private int         ordId;
    private long        barCode;
    private Spinner     facturas;
    private Button      cancell;
    private Button      ok;
    private Button      corregir;
    private int         prodID;
    private int         type; //0 viene del Scan con codigo de Barra, 1 Viene del Click de la lista
    private String      lastInvoice = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmar_cantidades);

        getViewElements();
        getBundleData();
        getProd();
        loadInvoices();
        setActions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_confirmar_cantidades, menu);
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

    @Override
    public void onResume()
    {
        super.onResume();

        getProd2();
        loadInvoices();
        setActions();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            if(requestCode == 2){
                if (resultCode == RESULT_OK) {
                    lastInvoice = data.getStringExtra("key").toString();
                }
            }

        } catch (Exception ex) {
            Toast.makeText(this, ex.toString(),
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) < 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            onBackPressed();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        intent.putExtra("key", facturas.getSelectedItem().toString());
        setResult(RESULT_OK, intent);
        finish();
    }

    public void getViewElements(){
        facturado = (EditText) findViewById(R.id.etInvoiced);
        recibido = (EditText) findViewById(R.id.etReceived);
        producto =  (TextView) findViewById(R.id.tvProduct);
        facturas = (Spinner) findViewById(R.id.spinInvoices);
        cancell = (Button) findViewById(R.id.btnCancel);
        ok = (Button) findViewById(R.id.btnOK);
        corregir = (Button) findViewById(R.id.btnResetQty);
    }

    public void setActions(){
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (producto.getText().equals("")) {
                    Toast.makeText(getApplicationContext(),
                            "No hay producto asociado. Cancele y vuelva a escanear!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    insertCant(false);
                }
            }
        });

        corregir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (producto.getText().equals("")) {
                    Toast.makeText(getApplicationContext(),
                            "No hay producto asociado. Cancele y vuelva a escanear!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    insertCant(true);
                }
            }
        });

        cancell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                intent.putExtra("key", facturas.getSelectedItem().toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    public void loadInvoices(){
        String fact = getfacturas();
        String spinnerArray[] = fact.split(";");
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        facturas.setAdapter(spinnerArrayAdapter);
        if(!lastInvoice.equalsIgnoreCase("")){
            facturas.setSelection(spinnerArrayAdapter.getPosition(lastInvoice));
        }
    }

    public String getfacturas(){
        DBHelper db = null;
        String qry = "select factura_id from factura where c_order_id = " + ordId;
        String res = "";

        try {
            Cursor rs = null;
            db = new DBHelper(this);
            db.openDB(0);
            rs = db.querySQL(qry, null);

            if(rs.moveToFirst()) {
                do{
                    res = res + rs.getString(0) + ";";
                }while(rs.moveToNext());
            }

        }catch (Exception e) {
            e.getMessage();
        } finally {
            db.close();
        }
        return res;
    }

    public void getBundleData() {
        // get the Intent that started this Activity
        Intent in = getIntent();
        // get the Bundle that stores the data of this Activity
        Bundle b = in.getExtras();
        if (null != b) {
            ordId = b.getInt("c_order_id");
            barCode = b.getLong("barcode");
            prodID = b.getInt("m_product_id");
            type = b.getInt("type");
            lastInvoice = b.getString("lastInvoice");
        }
    }



    private void getProd(){
        DBHelper db = null;
        Cursor rs = null;
        String qry = "";

        if(type == 0){
            qry = "select p.name" +
                    " from c_orderline ol JOIN m_product p" +
                    " ON ol.m_product_id = p.m_product_id" +
                    " JOIN uy_productupc up" +
                    " ON ol.m_product_id = up.m_product_id" +
                    " where ol.c_order_id = " + ordId +
                    " and up.upc = " + barCode;
        }else{
            qry = "select p.name" +
                    " from c_orderline ol JOIN m_product p" +
                    " ON ol.m_product_id = p.m_product_id" +
                    " where ol.c_order_id = " + ordId +
                    " and ol.m_product_id = " + prodID;
        }


        try {
            db = new DBHelper(CustomApplication.getCustomAppContext());
            db.openDB(0);
            rs = db.querySQL(qry, null);

            if(rs.moveToFirst()) {
                producto.setText(rs.getString(0));
            }else{         ///Agergar else para asociar el codigo de barra
                startAsociarBarCodeActivity(barCode);
            }
        }catch (Exception e) {
            e.getMessage();
        } finally {
            db.close();
        }
    }

    private void getProd2(){
        DBHelper db = null;
        Cursor rs = null;
        String qry = "";

        if(type == 0){
            qry = "select p.name" +
                    " from c_orderline ol JOIN m_product p" +
                    " ON ol.m_product_id = p.m_product_id" +
                    " JOIN uy_productupc up" +
                    " ON ol.m_product_id = up.m_product_id" +
                    " where ol.c_order_id = " + ordId +
                    " and up.upc = " + barCode;
        }else{
            qry = "select p.name" +
                    " from c_orderline ol JOIN m_product p" +
                    " ON ol.m_product_id = p.m_product_id" +
                    " where ol.c_order_id = " + ordId +
                    " and ol.m_product_id = " + prodID;
        }


        try {
            db = new DBHelper(CustomApplication.getCustomAppContext());
            db.openDB(0);
            rs = db.querySQL(qry, null);

            if(rs.moveToFirst()) {
                producto.setText(rs.getString(0));
            }
        }catch (Exception e) {
            e.getMessage();
        } finally {
            db.close();
        }
    }

    private void insertCant(boolean reset){
        /*if(recibido.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "Por favor ingrese cantidad Recibida!",
                    Toast.LENGTH_SHORT).show();
        }else{*/
        if(facturado.getText().toString().equals("")){
            facturado.setText("0");
        }
        if(recibido.getText().toString().equals("")){
            recibido.setText("0");
        }
        DBHelper db = null;
        Cursor ctlCurs, ctlCurs2;
        String update = "", where = "";
        String ctl = "Select daterecep from c_orderline";
        String addDate = "update c_orderline set daterecep = datetime('now')";

        if(!reset){
            update = "update c_orderline set qtyinvoiced = qtyinvoiced + (" + facturado.getText().toString() + "),"
                    + " qtydelivered = qtydelivered + (" + recibido.getText().toString() + "),"
                    + " factura_id = '" + facturas.getSelectedItem().toString() + "'";
        }else {
            update = "update c_orderline set qtyinvoiced = " + facturado.getText().toString() + ","
                    + " qtydelivered = " + recibido.getText().toString() + ","
                    + " factura_id = '" + facturas.getSelectedItem().toString() + "'";
        }

        if(type == 0){
            where = " where c_order_id = " + ordId
                    + " and m_product_id = (select m_product_id from uy_productupc where upc = "+ barCode + ")";
        }else{
            where = " where c_order_id = " + ordId + " and m_product_id = " + prodID;
        }



        try {
            db = new DBHelper(CustomApplication.getCustomAppContext());
            db.openDB(1);

            ctlCurs2 = db.querySQL("select qtyordered, qtydelivered, qtyinvoiced from c_orderline " + where, null);
            if(ctlCurs2.moveToFirst()){
                double qtyOrdered = ctlCurs2.getDouble(0);
                double qtyDelivered = ctlCurs2.getDouble(1);
                double qtyInvoiced = ctlCurs2.getDouble(2);
                if(ordId >= 1000000 && qtyOrdered < (Double.valueOf(recibido.getText().toString()) + qtyDelivered)){
                    Toast.makeText(getApplicationContext(),
                            "La cantidad ingresada total es mayor a la pendiente: "+ qtyOrdered,
                            Toast.LENGTH_SHORT).show();
                }else if(ordId >= 1000000 && qtyOrdered < (Double.valueOf(facturado.getText().toString()) + qtyInvoiced)){
                    Toast.makeText(getApplicationContext(),
                            "La cantidad facturada total es mayor a la pendiente: "+ qtyOrdered,
                            Toast.LENGTH_SHORT).show();
                }else{
                    db.executeSQL(update + where);
                    ctlCurs = db.querySQL(ctl + where, null);
                    if(ctlCurs.moveToFirst() && (ctlCurs.isNull(0) || ctlCurs.getString(0).equals(""))){
                        db.executeSQL(addDate + where);
                    }

                    Intent intent = getIntent();
                    intent.putExtra("key", facturas.getSelectedItem().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }

        }catch (Exception e) {
            e.getMessage();
        } finally {
            db.close();
        }
        //}
    }

    public void startAsociarBarCodeActivity(long barCode){
        Intent i = new Intent(this, AsociarBarCodeActivity.class);
        Bundle b = new Bundle();
        b.putInt("c_order_id", ordId);
        b.putLong("barcode", barCode);
        b.putString("lastInvoice", lastInvoice);
        i.putExtras(b);
        //finish();
        //startActivity(i);
        startActivityForResult(i, 2);
    }

    public void insertUPC(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder1.setView(input);
        builder1.setCancelable(true);
        builder1.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        barCode = Integer.valueOf(input.getText().toString());
                        loadInvoices();
                        setActions();
                    }
                });
        builder1.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }



}
