package com.openup.covadonga.covadongaapp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.openup.covadonga.covadongaapp.util.CustomApplication;
import com.openup.covadonga.covadongaapp.util.DBHelper;
import com.openup.covadonga.covadongaapp.util.CustomListAdapter;
import com.openup.covadonga.covadongaapp.util.Env;
import com.openup.covadonga.covadongaapp.util.Order;
import com.openup.covadonga.covadongaapp.util.SincronizeData;

import device.common.DecodeStateCallback;
import device.common.ScanConst;
import device.common.DecodeResult;
import device.sdk.ScanManager;

public class ProcesarOrdenActivity extends ActionBarActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager   mViewPager;
    private String[] ordenes;
    private int         tam;
    private int         entradaScan = 1;
    //private boolean     isInFront;



    //Nuevo Scanner
    private static ScanManager mScanner = new ScanManager();
    //private static ScanManager mScanner = null;
    private static DecodeResult mDecodeResult = new DecodeResult();
    private boolean mKeyLock = false;

    private static TextView mBarCode = null;
    private static String mResult = "";

    private AlertDialog mDialog = null;


    //Fin Nuevo Scanner
    private Handler mCallbackHandler = new Handler();
    private DecodeStateCallback mStateCallback = new DecodeStateCallback(mCallbackHandler) {
        public void onChangedState(int state) {
            if (state == ScanConst.STATE_ON) {
                if (getEnableDialog().isShowing()) {
                    getEnableDialog().dismiss();
                }
            } else {
                if (!getEnableDialog().isShowing()) {
                    getEnableDialog().show();
                }
            }
        };
    };

    public static class ScanResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mScanner != null) {
                mScanner.aDecodeGetResult(mDecodeResult.recycle());
                if(!mDecodeResult.symName.equals("READ_FAIL")){
                    mBarCode.setText(mDecodeResult.toString());
                }
            }
        }
    }

    private AlertDialog getEnableDialog() {
        if (mDialog == null) {
            AlertDialog dialog = new AlertDialog.Builder(this).create();
            dialog.setTitle(R.string.app_name);
            dialog.setMessage("Your scanner is disabled. Do you want to enable it?");

            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(ScanConst.LAUNCH_SCAN_SETTING_ACITON);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            dialog.dismiss();
                        }
                    });
            dialog.setCancelable(false);
            mDialog = dialog;
        }
        return mDialog;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mScanner != null) {
            mScanner.aDecodeAPIDeinit();
        }
        mScanner = null;
    }

    private void initScanner() {
        if(mScanner == null){
            mScanner = new ScanManager();
            mScanner.aDecodeSetTriggerMode(ScanConst.TriggerMode.DCD_TRIGGER_MODE_ONESHOT);
        }

//        if(mDecodeResult == null){
//            DecodeResult mDecodeResult = new DecodeResult();
//        }

//            mScanner.aDecodeSetBeepEnable(0);
//            mScanner.aDecodeSetTriggerOn(1);
//            mScanner.aDecodeSymSetEnable(ScanConst.SymbologyID.DCD_SYM_UPCA, 0);

        if (mScanner != null) {

            mScanner.aDecodeAPIInit();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}
            mScanner.aDecodeSetDecodeEnable(1);
            mScanner.aDecodeSetResultType(ScanConst.ResultType.DCD_RESULT_USERMSG);
            //mScanner.aDecodeSetResultType(ScanConst.ResultType.DCD_RESULT_COPYPASTE);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_procesar_orden);

        getTabsInfo();

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 1; i <= tam; i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.

            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        mBarCode = new TextView(this);
        initScanner();
    }

    public void passUPC(String upc){
        for (int i = 0; i < tam; i++) {
//            PlaceholderFragment fragment = (PlaceholderFragment) getFragmentManager().findFragmentByTag(ordenes[i].toString());
            Fragment fr = getSupportFragmentManager().findFragmentByTag("android:switcher:"
                    + R.id.pager + ":" + mViewPager.getCurrentItem());
            ((PlaceholderFragment) fr).insertUPC(upc);
        }

//                PlaceholderFragment fr = new PlaceholderFragment();
//                fr.insertUPC(mResult);
    }


//    @Override
//    public void onPause() {
//        super.onPause();
//        isInFront = false;
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_procesar_orden, menu);

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
            newOrderProcess();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            PlaceholderFragment ph = new PlaceholderFragment();
            return ph.newInstance(position + 1, ordenes[position]);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return tam;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            return ordenes[position-1].toUpperCase(l);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    @SuppressLint("ValidFragment")
    public class PlaceholderFragment extends Fragment { //static
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private ListView    lvProducts;
        private CustomListAdapter adapter;
        private Button      scan;
        private Button      finalizar;
        private int         docID;
        private int         ordId;

        private long        barCode;
        private String      lastInvoice = "";
        private boolean     isInFront;
        private ProgressDialog  pDialog;

        ////Nuevo Scanner
        private String strBarcode = "";

        ////////////Fin Nuevo Scanner

//        //Scan de ordenes
//        //send by BarcodeService
//        public static final String ACTION_BARCODE_SERVICE_BROADCAST = "action_barcode_broadcast";
//        private BroadcastReceiver mReceiver = new BarcodeReceiver();
//        //send by the BarcodeService
//        public static final String KEY_BARCODE_STR = "key_barcode_string";
//        private String strBarcode = "";
//        public static final String KEY_ACTION = "KEY_ACTION";
//        public static final String TONE = "TONE=100";
//        private Intent intentService = new Intent("com.hyipc.core.service.barcode.BarcodeService2D");

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public PlaceholderFragment newInstance(int sectionNumber, String docuID) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putInt("docID", Integer.parseInt(docuID));
            fragment.setArguments(args);
            return fragment;
        }

        public int getShownDoc() {
            return getArguments().getInt("docID", 0);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_procesar_orden, container, false);
//            Intent in = getShownIndex();
//            Bundle b = in.getExtras();
//            if (null != b) {
            docID = getShownDoc();
//            }
            lvProducts = (ListView) rootView.findViewById(R.id.listViewFragment);
            scan = (Button) rootView.findViewById(R.id.btnScan);
            finalizar = (Button) rootView.findViewById(R.id.btnEnd);

            //Scan de ordenes
//            intentService.putExtra(KEY_ACTION, TONE);
//            getActivity().startService(intentService);

            setActions();
            loadProducts();

            return rootView;
        }

        @Override
        public void onPause() {
            super.onPause();
            isInFront = false;
        }

        @Override
        public void onResume()
        {
            super.onResume();
            isInFront = true;

//            //Scan de ordenes
//            intentService.putExtra(KEY_ACTION, "INIT");
//            getActivity().startService(intentService);
//            // register receiver
//            IntentFilter filter = new IntentFilter();
//            filter.addAction(ACTION_BARCODE_SERVICE_BROADCAST);
//            getActivity().registerReceiver(mReceiver, filter);

            loadProducts();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            Env e = new Env();
            try {
                super.onActivityResult(requestCode, resultCode, data);

                if(requestCode == 1){
                    if (resultCode == RESULT_OK) {
                        lastInvoice = data.getStringExtra("key").toString();
                        e.setEntradaScan(1);
                    }
                }

            } catch (Exception ex) {
                Toast.makeText(getActivity().getBaseContext(), ex.toString(),
                        Toast.LENGTH_SHORT).show();
            }

        }

        public int getOrdId() {
            return ordId;
        }


        public void insertUPC(String upc){
            Env e = new Env();
            Fragment fr = getSupportFragmentManager().findFragmentByTag("android:switcher:"
                    + R.id.pager + ":" + mViewPager.getCurrentItem());

            if(e.getEntradaScan() == 1 && isInFront){
                //strBarcode = intent.getExtras().getString(KEY_BARCODE_STR);
                strBarcode = upc;
                e.setEntradaScan(2);
                insertUPCPDA(((PlaceholderFragment) fr).getOrdId());
                mBarCode.setText("");
                //insertUPCPDA(ordId);
            }
        }

////        //Scan de ordenes
//        public class BarcodeReceiver extends BroadcastReceiver {
//
//            public void onReceive(Context ctx, Intent intent) {
//                Env e = new Env();
//                Fragment fr = getSupportFragmentManager().findFragmentByTag("android:switcher:"
//                        + R.id.pager + ":" + mViewPager.getCurrentItem());
//
//                if(e.getEntradaScan() == 1 && isInFront){
//                    strBarcode = intent.getExtras().getString(KEY_BARCODE_STR);
//                    //strBarcode = "";
//                    e.setEntradaScan(2);
//                    insertUPCPDA(((PlaceholderFragment) fr).getOrdId());
//                    //insertUPCPDA(ordId);
//                }
//            }
//        }


        public void loadProducts(){
            ArrayList<Order> orderResults = GetSearchResults();
            adapter = new CustomListAdapter(getActivity().getBaseContext(), orderResults);
            lvProducts.setAdapter(adapter);
        }

        private ArrayList<Order> GetSearchResults(){
            DBHelper db = null;
            ArrayList<Order> results = new ArrayList<Order>();
            Cursor rs = null;
            String qry = "select p.m_product_id, p.name, ol.qtyordered," +
                    " ol.qtyinvoiced, ol.qtydelivered" +
                    " from c_orderline ol JOIN m_product p" +
                    " ON ol.m_product_id = p.m_product_id" +
                    " where ol.c_order_id = ";

            try {
                db = new DBHelper(CustomApplication.getCustomAppContext());
                db.openDB(0);
                String qryAux = "select c_order_id from c_order where documentno = " + docID + "";
                Cursor rsAux = db.querySQL(qryAux, null);
                rsAux.moveToFirst();
                ordId = rsAux.getInt(0);
                qry = qry + ordId + " order by p.name";

                rs = db.querySQL(qry, null);

                if(rs.moveToFirst()){
                    do{
                        Order sr1 = new Order();
                        sr1.setProdID(rs.getInt(0));
                        sr1.setCodigoDesc(rs.getString(1));
                        sr1.setCantOrdenada(rs.getFloat(2));
                        sr1.setCantFactura(rs.getFloat(3));
                        sr1.setCantRecibida(rs.getFloat(4));
                        results.add(sr1);
                    } while (rs.moveToNext());
                }

            } catch (Exception e) {
                e.getMessage();
            } finally {
                db.close();
            }

            return results;
        }

        public void setActions() {

            lvProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                    // TODO Auto-generated method stub
                    Order ord = (Order) arg0.getItemAtPosition(position);
                    startConfirmarCantidadesActivity(0, ord.getProdID(), 1, ordId);
                }
            });

            scan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    insertUPC();
                    //Scan de ordenes
//                    intentService.putExtra(KEY_ACTION, "UP");
//                    getActivity().startService(intentService);
//                    try {
//                        Thread.sleep(10);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    intentService.putExtra(KEY_ACTION, "DOWN");
//                    getActivity().startService(intentService);

                    //insertUPCPDA();
                }
            });


            finalizar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Finalizar Orden?");
                    // Add the buttons
                    builder.setPositiveButton(R.string.txtOK, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finalizeOrder();
                            SyncOrd();
                        }
                    });
                    builder.setNegativeButton(R.string.txtCancell, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });

            mBarCode.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(!s.equals("") && TextUtils.isDigitsOnly(s.toString())){
                        insertUPC(s.toString());
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    //s.append("A");
                }
            });
        }

        public void SyncOrd(){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Sincronizar Orden?");
            // Add the buttons
            builder.setPositiveButton(R.string.txtOK, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    sincronizarOrders();
                }
            });
            builder.setNegativeButton(R.string.txtCancell, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    getActivity().finish();
                    restartActivity();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        public void restartActivity(){
            if(ordenes.length > 1){
                String strOrdenes = "";
                for(int i = 0; i < ordenes.length; i++){
                    if(!ordenes[i].equals(String.valueOf(docID))){
                        strOrdenes = strOrdenes + ordenes[i] + ";";
                    }
                }
                Intent i = new Intent(getActivity(), ProcesarOrdenActivity.class);
                Bundle b = new Bundle();
                b.putString("Ordenes", strOrdenes);
                i.putExtras(b);
                startActivity(i);
            }
        }

        public void sincronizarOrders(){
            pDialog = ProgressDialog.show(getActivity(), null, "Enviando datos...", true);
            final SincronizeData sd = new SincronizeData();
            new Thread() {
                public void run() {
                    try {
                        sd.sendOrders();
                        getActivity().finish();
                        restartActivity();
                    } catch (Exception e) {
                        e.getMessage();
                    }
                    pDialog.dismiss();
                }
            }.start();
        }

        public void insertUPC(){
            android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.Builder(getActivity());
            final EditText input = new EditText(getActivity());
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder1.setView(input);
            builder1.setTitle("Ingrese el Codigo de Barras");
            builder1.setCancelable(true);
            builder1.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            barCode = Long.valueOf(input.getText().toString());
                            startConfirmarCantidadesActivity(barCode, 0, 0, ordId);
                        }
                    });
            builder1.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            android.app.AlertDialog alert11 = builder1.create();
            alert11.show();
        }

        public void insertUPCPDA(int orderID){
            Long bc = Long.valueOf(strBarcode);
            startConfirmarCantidadesActivity(bc, 0, 0, orderID);
        }


        public void startConfirmarCantidadesActivity(long barCode, int prdID, int type, int orderID){
            Intent i = new Intent(getActivity().getBaseContext(), ConfirmarCantidadesActivity.class);
            Bundle b = new Bundle();
            b.putInt("c_order_id", orderID);
            b.putLong("barcode", barCode);
            b.putInt("m_product_id", prdID);
            b.putInt("type", type);
            b.putString("lastInvoice", lastInvoice);
            i.putExtras(b);
            //startActivity(i);
            startActivityForResult(i, 1);
        }

        public void finalizeOrder(){
            DBHelper db = null;
            int res;
            String date;
            String where = " c_order_id = " + ordId;
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            String mon = String.valueOf(month + 1);
            String dai = String.valueOf(day);
            if((month+1) < 10){
                mon = "0"+mon;
            }
            if(day < 10){
                dai = "0"+dai;
            }
            date = year+"-"+mon+"-"+dai+" 00:00:00";

            try {
                db = new DBHelper(CustomApplication.getCustomAppContext());
                db.openDB(1);
                ContentValues cv = new ContentValues();
                cv.put("finalizado", "Y");
                cv.put("fecha", date);

                res = db.updateSQL("c_order", cv, where, null);
                if(res > 0){
                    Toast.makeText(getActivity().getBaseContext(), "Orden Finalizada!",
                            Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getActivity().getBaseContext(), "Error al finalizar orden!",
                            Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e) {
                e.getMessage();
            } finally {
                db.close();
            }
        }
    }



    public void getTabsInfo() {
        // get the Intent that started this Activity
        Intent in = getIntent();
        // get the Bundle that stores the data of this Activity
        Bundle b = in.getExtras();
        if (null != b) {
            ordenes = b.getString("Ordenes").split(";");
            tam = ordenes.length;
        }
    }

    public void newOrderProcess() {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setTitle(R.string.new_supplier);
//            builder.setMessage(R.string.process_new_order)
//                .setPositiveButton(R.string.txtOK, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        startListaClienteActivity();
//                    }
//                })
//                    .setNegativeButton(R.string.txtCancell, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // User cancelled the dialog
//                    }
//                });
//
//        // Create the AlertDialog object and return it
//        builder.create();
//        builder.show();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    private void startListaClienteActivity(int recType) {
        Bundle bundle = new Bundle();
        bundle.putInt("recType", recType);
        Intent i = new Intent(this, ListaProveedorActivity.class);
        i.putExtras(bundle);
        startActivity(i);
    }

}
