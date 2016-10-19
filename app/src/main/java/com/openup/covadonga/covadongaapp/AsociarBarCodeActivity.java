package com.openup.covadonga.covadongaapp;

import android.content.ContentValues;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.openup.covadonga.covadongaapp.util.CustomApplication;
import com.openup.covadonga.covadongaapp.util.DBHelper;


public class AsociarBarCodeActivity extends ActionBarActivity {

    private EditText    etFilter;
    private ListView    lvProds;
    private Button      btnBack;
    private Button      btnOK;
    private int         ordId;
    private String      barCode;
    private String[]    prods;
    private String      lastInvoice;
    // Listview Adapter
    private ArrayAdapter<String> adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asociar_bar_code);

        getViewElements();
        getOrderId();
        loadProds();
        setActions();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_asociar_bar_code, menu);
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
        etFilter = (EditText) findViewById(R.id.editTextProd);
        lvProds = (ListView) findViewById(R.id.listViewProd);
        btnBack = (Button) findViewById(R.id.btnBackProd);
        btnOK = (Button) findViewById(R.id.btnOkProd);
    }

    public void setActions(){

        etFilter.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                AsociarBarCodeActivity.this.adaptador.getFilter().filter(cs);
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

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                asociateBarCode();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void getOrderId() {
        // get the Intent that started this Activity
        Intent in = getIntent();
        // get the Bundle that stores the data of this Activity
        Bundle b = in.getExtras();
        if (null != b) {
            ordId = b.getInt("c_order_id");
            barCode = String.valueOf(b.getLong("barcode"));
            lastInvoice = b.getString("lastInvoice");
            //barCode = b.getString("barcode");
        }
    }

    public void loadProds(){
        DBHelper db = null;
        String qry = "select p.m_product_id, p.name" +
                        " from c_orderline ol JOIN m_product p" +
                        " ON ol.m_product_id = p.m_product_id" +
                        " where ol.c_order_id = " + ordId +
                        " order by p.name";

        try {
            db = new DBHelper(getApplicationContext());
            db.openDB(0);
            Cursor rs = db.querySQL(qry, null);

            if(rs.moveToFirst()){
                int count = rs.getCount();
                prods = new String[count];
                for(int i = 0; i < count; i++){
                    prods[i] = rs.getString(0)+"; "+rs.getString(1);
                    rs.moveToNext();
                }

            }
            adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, prods);
            lvProds.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            lvProds.setAdapter(adaptador);

        }catch (Exception e) {
            e.getMessage();
        } finally {
            db.close();
        }
    }

    public void asociateBarCode(){
        SparseBooleanArray checkedItems = lvProds.getCheckedItemPositions();
        int prodId = 0;
        if (checkedItems != null) {
            String item = "";
            for (int j=0; j<checkedItems.size(); j++) {
                if (checkedItems.valueAt(j)) {
                    String[] items = lvProds.getAdapter().getItem(checkedItems.keyAt(j)).toString().split(";");
                    prodId = Integer.parseInt(items[0]);
                }
            }
            insertBarCode(prodId);
        }
    }

    private void insertBarCode(int prodId){
            DBHelper db = null;
            int id = 1;

            try {
                db = new DBHelper(this);
                db.openDB(1);
                String qry = "select max(uy_productupc_id) from uy_productupc where uy_productupc_id < 1000";
                Cursor rs = db.querySQL(qry, null);
                if(rs.moveToFirst()){
                    id = id + rs.getInt(0);
                }
                ContentValues cv = new ContentValues();
                cv.put("uy_productupc_id", id);
                cv.put("m_product_id", prodId);
                cv.put("upc", barCode);

                long res = db.insertSQL("uy_productupc", null, cv);

                //rs = db.querySQL(qry, null);
                if(res > 0){
                    Toast.makeText(getApplicationContext(), "Actualizado!",
                            Toast.LENGTH_SHORT).show();
                    startConfirmarCantidadesActivity(Long.parseLong(barCode), 0, 0);
                }else{
//                    Toast.makeText(getApplicationContext(), "Error al actulizar!",
//                            Toast.LENGTH_SHORT).show();
                    errorPKUPC();
                }

            }catch (Exception e) {
                e.getMessage();
            } finally {
                db.close();
            }
    }

    public void errorPKUPC(){
        DBHelper db = null;
        db = new DBHelper(this);
        db.openDB(1);
        try{
            String qry = "select p.name from m_product p join uy_productupc" +
                    " u on p.m_product_id = u.m_product_id where u.upc = "+barCode;
            Cursor rs = db.querySQL(qry, null);
            if(rs.moveToFirst()){
                Toast.makeText(getApplicationContext(),
                        "El CB esta asociado al producto: " + rs.getString(0),
                        Toast.LENGTH_LONG).show();
            }
        }catch (Exception e) {
            e.getMessage();
        } finally {
            db.close();
        }
    }

//    public void startConfirmarCantidadesActivity(){
//        Intent i = new Intent(this, ConfirmarCantidadesActivity.class);
//        Bundle b = new Bundle();
//        b.putInt("c_order_id", ordId);
//        b.putLong("barcode", Long.parseLong(barCode));
//        i.putExtras(b);
//        finish();
//        startActivity(i);
//    }

    public void startConfirmarCantidadesActivity(long barCode, int prdID, int type){
        Intent i = new Intent(this, ConfirmarCantidadesActivity.class);
        Bundle b = new Bundle();
        //b.putInt("c_order_id", ordId);
//        b.putLong("barcode", barCode);
//        b.putInt("m_product_id", prdID);
//        b.putInt("type", type);
        b.putString("key", lastInvoice);
        i.putExtras(b);
        setResult(RESULT_OK, i);
        //startActivity(i);
        finish();
        //startActivityForResult(i, 1);
    }

}
