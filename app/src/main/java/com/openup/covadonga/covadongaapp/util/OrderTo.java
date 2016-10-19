package com.openup.covadonga.covadongaapp.util;

/**
 * Created by Emilino on 04/08/2015.
 */
public class OrderTo{
    private String      adClientId;
    private String      adOrgId;
    private String      orderId;
    private String      partnerId;
    private String      date;
    private String      warehouseId;
    private String      deviceId;
    private OrderLine[] oL;

    public OrderTo() {
    }

    public String getAdClientId() {
        return adClientId;
    }

    public void setAdClientId(String adClientId) {
        this.adClientId = adClientId;
    }

    public String getAdOrgId() {
        return adOrgId;
    }

    public void setAdOrgId(String adOrgId) {
        this.adOrgId = adOrgId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public OrderLine[] getoL() {
        return oL;
    }

    public void setoL(OrderLine[] oL) {
        this.oL = oL;
    }

    public int getOLSize(){
        return oL.length;
    }

}