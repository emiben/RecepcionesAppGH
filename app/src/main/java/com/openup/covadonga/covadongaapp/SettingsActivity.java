package com.openup.covadonga.covadongaapp;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.openup.covadonga.covadongaapp.util.CustomApplication;
import com.openup.covadonga.covadongaapp.util.DBHelper;

public class SettingsActivity extends AppCompatActivity {

    private Button  btnReset;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getViewElements();
        setActions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
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

    private void getViewElements(){
        btnReset = (Button) findViewById(R.id.btnReset);
    }

    private void setActions(){
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteData();
            }
        });
    }

    private void deleteData(){
        pDialog = ProgressDialog.show(this, null, "Reiniciando a estado inicial...", true);
        DBHelper db = null;
        try{
            db = new DBHelper(CustomApplication.getCustomAppContext());
            db.openDB(1);

            db.deleteSQL("c_bpartner", "", null);
            db.deleteSQL("c_order", "", null);
            db.deleteSQL("c_orderline", "", null);
            db.deleteSQL("factura", "", null);
            db.deleteSQL("m_product", "", null);
            db.deleteSQL("priceListProducts", "", null);
            db.deleteSQL("reports", "", null);
            db.deleteSQL("uy_productupc", "", null);

        }catch (Exception e) {
            e.getMessage();
        } finally {
            db.close();
        }
        pDialog.dismiss();
    }
}
