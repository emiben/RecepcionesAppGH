package com.openup.covadonga.covadongaapp.util;

/**
 * Created by Emilino on 24/09/2015.
 */
public class OrderLine {
    private String      adClientId;
    private String      adOrgId;
    private String      orderLineId;
    private String      orderId;
    private String      productId;
    private String      qtyInvoiced;
    private String      qtyDelivered;
    private String      NroFactura;
    private String      FechaFactura;

    public OrderLine() {
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

    public String getOrderLineId() {
        return orderLineId;
    }

    public void setOrderLineId(String orderLineId) {
        this.orderLineId = orderLineId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getQtyInvoiced() {
        return qtyInvoiced;
    }

    public void setQtyInvoiced(String qtyInvoiced) {
        this.qtyInvoiced = qtyInvoiced;
    }

    public String getQtyDelivered() {
        return qtyDelivered;
    }

    public void setQtyDelivered(String qtyDelivered) {
        this.qtyDelivered = qtyDelivered;
    }

    public String getNroFactura() {
        return NroFactura;
    }

    public void setNroFactura(String nroFactura) {
        NroFactura = nroFactura;
    }

    public String getFechaFactura() {
        return FechaFactura;
    }

    public void setFechaFactura(String fechaFactura) {
        FechaFactura = fechaFactura;
    }
}
