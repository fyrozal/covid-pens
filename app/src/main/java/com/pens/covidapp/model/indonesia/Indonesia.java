package com.pens.covidapp.model.indonesia;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Indonesia {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("positif")
    @Expose
    private String positif;
    @SerializedName("sembuh")
    @Expose
    private String sembuh;
    @SerializedName("meninggal")
    @Expose
    private String meninggal;
    @SerializedName("dirawat")
    @Expose
    private String dirawat;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPositif() {
        return positif;
    }

    public void setPositif(String positif) {
        this.positif = positif;
    }

    public String getSembuh() {
        return sembuh;
    }

    public void setSembuh(String sembuh) {
        this.sembuh = sembuh;
    }

    public String getMeninggal() {
        return meninggal;
    }

    public void setMeninggal(String meninggal) {
        this.meninggal = meninggal;
    }

    public String getDirawat() {
        return dirawat;
    }

    public void setDirawat(String dirawat) {
        this.dirawat = dirawat;
    }

    @Override
    public String toString() {
        return "Indonesia{" +
                "name='" + name + '\'' +
                ", positif='" + positif + '\'' +
                ", sembuh='" + sembuh + '\'' +
                ", meninggal='" + meninggal + '\'' +
                ", dirawat='" + dirawat + '\'' +
                '}';
    }
}