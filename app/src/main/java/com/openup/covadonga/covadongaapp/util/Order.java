package com.openup.covadonga.covadongaapp.util;

/**
 * Created by Emilino on 04/08/2015.
 */
public class Order {

    private int     prodID;
    private String  codigoDesc;
    private double  cantOrdenada;
    private double  cantFactura;
    private double  cantRecibida;

    public int getProdID() {
        return prodID;
    }

    public void setProdID(int prodID) {
        this.prodID = prodID;
    }

    public void setCodigoDesc(String codDes){
        this.codigoDesc = codDes;
    }

    public String getCodigoDesc(){
        return this.codigoDesc;
    }

    public void setCantOrdenada(double canOrd){
        this.cantOrdenada = canOrd;
    }

    public double getCantOrdenada(){
        return this.cantOrdenada;
    }

    public void setCantFactura(double canFac){
        this.cantFactura = canFac;
    }

    public double getCantFactura(){
        return this.cantFactura;
    }

    public void setCantRecibida(double canRec){
        this.cantRecibida = canRec;
    }

    public double getCantRecibida(){
        return this.cantRecibida;
    }
}
