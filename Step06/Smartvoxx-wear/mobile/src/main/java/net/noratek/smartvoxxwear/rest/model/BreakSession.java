package net.noratek.smartvoxxwear.rest.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by eloudsa on 06/09/15.
 */
public class BreakSession {

    private Long dbKey;

    @SerializedName("id")
    private String breakId;

    private String nameEN;
    private String nameFR;
    private String lastUpdate;


    public Long getDbKey() {
        return dbKey;
    }

    public void setDbKey(Long dbKey) {
        this.dbKey = dbKey;
    }

    public String getBreakId() {
        return breakId;
    }

    public void setBreakId(String breakId) {
        this.breakId = breakId;
    }

    public String getNameEN() {
        return nameEN;
    }

    public void setNameEN(String nameEN) {
        this.nameEN = nameEN;
    }

    public String getNameFR() {
        return nameFR;
    }

    public void setNameFR(String nameFR) {
        this.nameFR = nameFR;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
