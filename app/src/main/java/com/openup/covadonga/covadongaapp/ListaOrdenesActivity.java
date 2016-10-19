package com.openup.covadonga.covadongaapp;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.openup.covadonga.covadongaapp.util.DBHelper;
import com.openup.covadonga.covadongaapp.util.Env;


public class ListaOrdenesActivity extends ActionBarActivity {

    private ListView    lvOrders;
    private Button      btnBack;
    private Button      btnOK;
    private String[]    prov;
    private String[]    orders;
    private int         tam;
    private int         recType; // 0 con orden, 1 sin orden
    // Listview Adapter
    private ArrayAdapter<String> adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_ordenes);

        getViewElements();
        getPrveedor();
        loadOrders();
        setActions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lista_ordenes, menu);
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
        lvOrders = (ListView) findViewById(R.id.listViewOrders);
        btnBack = (Button) findViewById(R.id.btnBackOrd);
        btnOK = (Button) findViewById(R.id.btnOkOrd);
    }

    public void setActions(){

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray checkedItems = lvOrders.getCheckedItemPositions();
                if (checkedItems.size() > 0) {
                    startIngresoFacturasActivity(checkedItems);
                } else {
                    Toast.makeText(getApplicationContext(), "Por Favor elija una Orden!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startListaProveedorActivity();
            }
        });
    }

    public void loadOrders(){
        DBHelper db = null;
        String qry1 = "select c_bpartner_id from c_bpartner where name = '" + prov[0] + "'";
        String qry2 = "";
        int provId, count;
        //String qry = "Select name from c_bpartner where name = '"+ prov[0] + "'";

        try {
            db = new DBHelper(getApplicationContext());
            db.openDB(0);
            Cursor rs = db.querySQL(qry1, null);
            rs.moveToFirst();
            provId = rs.getInt(0);
            qry2 = "select documentno from c_order where c_bpartner_id = " + provId + " and ad_org_id = " + Env.adOrgId;
            Cursor rs2 = db.querySQL(qry2, null);
            count = rs2.getCount();
            orders = new String[count];
            rs2.moveToFirst();
            for(int i = 0; i < count; i++){
                orders[i] = rs2.getString(0);
                rs2.moveToNext();
            }

            adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, orders);
            lvOrders.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            lvOrders.setAdapter(adaptador);

        }catch (Exception e) {
            e.getMessage();
        } finally {
            db.close();
        }

    }


    private void startIngresoFacturasActivity(SparseBooleanArray checkedItems) {
        Intent i = new Intent(this, IngresoFacturasActivity.class);
        Bundle b = new Bundle();

        String item = "";
        for (int j=0; j<checkedItems.size(); j++) {
            if (checkedItems.valueAt(j)) {
                item = item + lvOrders.getAdapter().getItem(
                        checkedItems.keyAt(j)).toString() + ";";
                //Log.i(TAG,item + " was selected");
                b.putString("Ordenes", item);
                b.putString("Prov", prov[0]);
            }
        }
        i.putExtras(b);
        if(!i.hasExtra("Ordenes")){
            Toast.makeText(getApplicationContext(), "Por Favor elija una Orden!", Toast.LENGTH_SHORT).show();
        }else {
            this.finish();
            startActivity(i);
        }
    }

    public void getPrveedor() {
        // get the Intent that started this Activity
        Intent in = getIntent();
        // get the Bundle that stores the data of this Activity
        Bundle b = in.getExtras();
        if (null != b) {
            prov = b.getString("Prov").split(";");
            tam = prov.length;
            recType = b.getInt("recType");
        }
    }

    private void startListaProveedorActivity() {
        Bundle bundle = new Bundle();
        bundle.putInt("recType", recType);
        Intent i = new Intent(this, ListaProveedorActivity.class);
        i.putExtras(bundle);
        this.finish();
        startActivity(i);
    }
}
