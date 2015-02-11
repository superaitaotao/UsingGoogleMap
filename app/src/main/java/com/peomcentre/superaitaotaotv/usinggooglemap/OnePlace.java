package com.peomcentre.superaitaotaotv.usinggooglemap;

/**
 * Created by superaitaotaoTV on 14/11/14.
 */

//OnePlace stores information of a place obtained from Google Place api
public class OnePlace {
    private String name;
    private double lat, lng;
    public OnePlace(String name, double lat, double lng){
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
