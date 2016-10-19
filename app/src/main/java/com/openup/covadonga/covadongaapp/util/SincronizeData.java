package com.openup.covadonga.covadongaapp.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.widget.Toast;

import org.ksoap2.serialization.SoapObject;

/**
 * Created by Emilino on 22/09/2015.
 */
public class SincronizeData {

    private WebServices ws;

    public String sendUPC(){
        String res = "";
        ws = new WebServices();
        String[] columYVal = new String[6];
        DBHelper db = null;
        String qryUPC = "select * from uy_productupc where uy_productupc_id < 1000";

        try{
            db = new DBHelper(CustomApplication.getCustomAppContext());
            db.openDB(1);
            Cursor rs = db.querySQL(qryUPC, null);

            if(rs.moveToFirst()){
                do{
                    int i = 0;
                    columYVal[i++] = "UPC"; //colum
                    columYVal[i++] = String.valueOf(rs.getLong(2)); //val
                    columYVal[i++] = "M_Product_ID"; //colum
                    columYVal[i++] = String.valueOf(rs.getInt(1)); //val
                    columYVal[i++] = "IsMobile"; //colum
                    columYVal[i++] = "Y"; //val
//                    ws.webServiceIns("CreateProductUPC", "UY_ProductUpc", columYVal);
//                    if(ws.getMessage() == "EOFException"){
//                        ws.webServiceIns("CreateProductUPC", "UY_ProductUpc", columYVal);
//                    }else

                    do{
                        ws.webServiceIns("CreateProductUPC", "UY_ProductUpc", columYVal);
                    }while (ws.getMessage() == "EOFException");

                    if (ws.getMessage().toString().toLowerCase().contains("error")){
                        res = res + "El UPC " +
                                ws.getMessage().toString().substring(132, 142) +
                                " no se pudo sincronizar!;";
                        int resu = db.deleteSQL("uy_productupc","uy_productupc_id = "+rs.getInt(0), null);
                    }else if(ws.getResponse() != null) {
                        String resultado = (String) ws.getResponse().getAttribute(0);

                        if (Integer.valueOf(resultado) > 0) {
                            int resu = db.deleteSQL("uy_productupc","uy_productupc_id = "+rs.getInt(0), null);
                        }
                    }
                }while(rs.moveToNext());

            }
        }catch (Exception e) {
            e.getMessage();
        } finally {
            db.close();
        }
        return res;
    }

    public void sendOrders(){
        Env env = new Env();
        DBHelper db = null;
        Cursor ordCurs;
        OrderTo ord = null;
        OrderTo[] orders;

        ////Selecciono las ordenes marcadas como finalizadas
        try{
            db = new DBHelper(CustomApplication.getCustomAppContext());
            db.openDB(1);
            SoapObject resp = new SoapObject();
            ws = new WebServices();
            int i = 0;
            String qryOrd = "select c_order_id, c_bpartner_id, fecha, uy_mb_inout_id, ad_org_id from c_order where finalizado = 'Y'";
            ordCurs = db.querySQL(qryOrd, null);
            orders = new OrderTo[ordCurs.getCount()];
            if(ordCurs.moveToFirst()) {
                do {
                    String qryWarehouse = "select warehouse_id from ad_org where ad_org_id = " + ordCurs.getString(4);
                    Cursor wHCurs = db.querySQL(qryWarehouse, null);
                    ord = new OrderTo();
                    ord.setAdClientId(ordCurs.getString(3));
                    ord.setAdOrgId(ordCurs.getString(4));
                    ord.setOrderId(ordCurs.getString(0));
                    ord.setPartnerId(ordCurs.getString(1));
                    ord.setDate(ordCurs.getString(2));
                    if(wHCurs.moveToFirst()){
                        ord.setWarehouseId(wHCurs.getString(0));
                    }
                    ord.setDeviceId("dev 1");
                    addOrderLines(ord, ordCurs.getString(0));
                    orders[i] = ord;
                    i++;
                } while (ordCurs.moveToNext());
                ///////llamar al WS pasandole order
                ws.SoapCallerInOrder(orders);
                if(ws.getMessage() == "EOFException"){
                    ws.SoapCallerInOrder(orders);
                }
                ////Con la respuesta llamar a la func que elimina los datos de las ordenes
                resp = ws.getResponse();
                SoapObject resSoap1 = (SoapObject)resp.getProperty(0);
                int tam = resSoap1.getPropertyCount();
                String resArre[] = new String[tam/2];
                int pos = 0;
                for (int j=0; j<tam; j++){
                    String resu = resSoap1.getProperty(j).toString();
                    if(!resu.equals("UY_OrderRT_ID")){
                        resArre[pos] = resu;
                        pos++;
                    }
                }
                separaArre(resArre);
            }
        }catch (Exception e) {
            e.getMessage();
        } finally {
            db.close();
        }
    }

    private void separaArre(String resArre[]){
        int tam = resArre.length;
        int ok=0, err=0;
        for(int i=0; i<tam; i++){
            if(resArre[i].startsWith("OK")){
                ok++;
            }else{
                err++;
            }
        }
        String[] okArre = new String[ok];
        String[] errArre = new String[err];
        for(int i=0; i<tam; i++){
            int posOK = 0, posErr=0;
            if(resArre[i].startsWith("OK")){
                okArre[posOK] = resArre[i];
                posOK++;
            }else{
                errArre[posErr] = resArre[i];
                posErr++;
            }
        }
        /////Llamar a las funciones de eliminar y a la de update la de errores
        delSyncOrders(okArre);
        insertErrors(errArre);

    }

    private void addOrderLines(OrderTo ord, String ordId){
        Env env = new Env();
        DBHelper db = null;
        Cursor ordLineCurs;
        OrderLine ordLine = null;
        OrderLine[] orderLines;

        try {
            db = new DBHelper(CustomApplication.getCustomAppContext());
            db.openDB(0);
            int i = 0;
            String qryOrdLine = "select l.c_orderline_id, l.c_order_id, l.m_product_id," +
                    " l.qtyinvoiced, l.qtydelivered, l.factura_id, f.fecha, l.ad_org_id" +
                    " from c_orderline l left join factura f" +
                    " on l.factura_id = f.factura_id" +
                    " where l.c_order_id = " + ordId +
                    " and l.c_orderline_id not in" +
                    " (select c_orderline_id from c_orderline where qtyordered = 0 and qtydelivered = 0 and qtyinvoiced = 0)"+
                    " order by datetime(daterecep) asc";
            ordLineCurs = db.querySQL(qryOrdLine, null);
            orderLines = new OrderLine[ordLineCurs.getCount()];
            if (ordLineCurs.moveToFirst()) {
                do {
                    ordLine = new OrderLine();
                    ordLine.setAdClientId("1000006");
                    ordLine.setAdOrgId(ordLineCurs.getString(7));
                    ordLine.setOrderLineId(ordLineCurs.getString(0));
                    ordLine.setOrderId(ordLineCurs.getString(1));
                    ordLine.setProductId(ordLineCurs.getString(2));
                    ordLine.setQtyInvoiced(ordLineCurs.getString(3));
                    ordLine.setQtyDelivered(ordLineCurs.getString(4));
                    ordLine.setNroFactura(ordLineCurs.getString(5));
                    ordLine.setFechaFactura(ordLineCurs.getString(6));
                    orderLines[i] = ordLine;
                    i++;
                } while (ordLineCurs.moveToNext());
                ord.setoL(orderLines);
            }
        }catch (Exception e) {
            e.getMessage();
        } finally {
        }
    }


    public void delSyncOrders(String[] orders){
        int tam = orders.length;
        int ordId;
        String qryProdId;
        String qryDelPLs;

        for(int i=0; i<tam; i++){
            DBHelper db = null;
            String[] split = orders[i].split("-");
            ordId = Integer.valueOf(split[2].toString().trim());
            qryProdId = "select distinct m_product_id from c_orderline where c_order_id = " + ordId;
            qryDelPLs = "delete from priceListProducts where c_bpartner_id not in (Select c_bpartner_id from c_order)";

            try{
                db = new DBHelper(CustomApplication.getCustomAppContext());
                db.openDB(1);
                Cursor rsProds = db.querySQL(qryProdId, null);
                String whereProd = "";
                String whereOrd = " c_order_id = " + ordId;
                if(rsProds.moveToFirst()) {
                    do {
                        whereProd = " m_product_id = "+ rsProds.getInt(0) +" and m_product_id not in" +
                        " (select distinct m_product_id from c_orderline where c_order_id <> "+ ordId +")";

                        int resuUpc = db.deleteSQL("uy_productupc", whereProd, null);
                        int resuProd = db.deleteSQL("m_product", whereProd, null);

                    } while (rsProds.moveToNext());
                }
                int resuRep = db.deleteSQL("reports", whereOrd, null);
                int resuFac = db.deleteSQL("factura", whereOrd, null);
                int resuOL = db.deleteSQL("c_orderline", whereOrd, null);
                int resuOrd = db.deleteSQL("c_order", whereOrd, null);
                db.executeSQL(qryDelPLs);

            }catch (Exception e) {
                e.getMessage();
            } finally {
                db.close();
            }
        }
    }


    public void insertErrors(String[] errors){
        int tam = errors.length;

        for(int i=0; i<tam; i++){
            DBHelper db = null;
            String[] split = errors[i].split("-");
            String qry = "insert into reports values ("+split[2].toString().trim()+",'"+split[0]+"','"+split[1]+"')";
            String qryWhere = " c_order_id = " + split[2];

            try{
                db = new DBHelper(CustomApplication.getCustomAppContext());
                db.openDB(1);
                db.executeSQL(qry);
                if(split.length>3){
                    ContentValues cv = new ContentValues();
                    cv.put("uy_mb_inout_id", split[3]);
                    db.updateSQL("c_order", cv, qryWhere, null);
                }


            }catch (Exception e) {
                e.getMessage();
            } finally {
                db.close();
            }
        }
    }



}
