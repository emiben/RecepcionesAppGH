package com.openup.covadonga.covadongaapp;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.openup.covadonga.covadongaapp.util.DBHelper;


public class ReportesActivity extends ActionBarActivity {

    private EditText etFilter;
    private ListView lvOrdRep;
    private Button btnBack;
    private TextView tvOrder;
    private ProgressDialog pDialog;
    // Listview Adapter
    private ArrayAdapter<String> adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportes);

        getViewElements();
        loadOrders();
        setActions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reportes, menu);
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
        etFilter = (EditText) findViewById(R.id.editTextFilterRep);
        lvOrdRep = (ListView) findViewById(R.id.listViewOrdRep);
        btnBack = (Button) findViewById(R.id.btnBack);
    }

    public void setActions() {

        etFilter.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                ReportesActivity.this.adaptador.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void loadOrders(){
        DBHelper db = null;
        String qry = "select * from reports";

        try {
            db = new DBHelper(getApplicationContext());
            db.openDB(0);
            Cursor rs = db.querySQL(qry, null);
            int tam = rs.getCount();
            String[] prov = new String[tam];
            rs.moveToFirst();
            for(int i = 0; i < tam; i++){
                String qryOrder = "select documentno from c_order where c_order_id = "+rs.getString(0);
                Cursor rsOrd = db.querySQL(qryOrder, null);
                rsOrd.moveToFirst();
                prov[i] = "Orden: " + rsOrd.getString(0) + " - " + rs.getString(1) + " - " + rs.getString(2);
                rs.moveToNext();
            }
            adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, prov);
            lvOrdRep.setAdapter(adaptador);

        }catch (Exception e) {
            e.getMessage();
        } finally {
            db.close();
        }

    }
}
