package com.openup.covadonga.covadongaapp.util;

/**
 * Created by emiliano on 19/07/16.
 */
public class AdOrg {

    private int adOrgId;
    private String adOrgName;

    public AdOrg(int adOrgId, String adOrgName){
        this.adOrgId = adOrgId;
        this.adOrgName = adOrgName;
    }

    public int getAdOrgId() {
        return adOrgId;
    }

    public void setAdOrgId(int adOrgId) {
        this.adOrgId = adOrgId;
    }

    public String getAdOrgName() {
        return adOrgName;
    }

    public void setAdOrgName(String name) {
        this.adOrgName = name;
    }

    @Override
    public String toString () {
        return adOrgName;
    }
}
