package org.example.models;

public class Lokacja {
    private String nazwaMiasta;
    private double szerokoscGeo;
    private double dlugoscGeo;


    public Lokacja() {}

    public Lokacja(String nazwaMiasta, double szerokoscGeo, double dlugoscGeo) {
        this.nazwaMiasta = nazwaMiasta;
        this.szerokoscGeo = szerokoscGeo;
        this.dlugoscGeo = dlugoscGeo;
    }


    public String getNazwaMiasta() { return nazwaMiasta; }


    public double getSzerokoscGeo() { return szerokoscGeo; }


    public double getDlugoscGeo() { return dlugoscGeo; }

}