package com.peomcentre.superaitaotaotv.usinggooglemap;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by superaitaotaoTV on 14/11/14.
 */
//OneJourneySegment store information of a leg from Google Direciton Api
public class OneJouneySegment {
    private String distance, duration, startAddress, endAddress;
    private LatLng startLocation, endLocation;
    private ArrayList<OneJourneyStep> manySteps;
    public OneJouneySegment(String startAddress, String endAddress, String distance, String duration, LatLng startLocation,
                            LatLng endLocation){
        this.startAddress = startAddress;
        this.endAddress = endAddress;
        this.distance = distance;
        this.duration = duration;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    public ArrayList<OneJourneyStep> getManySteps() {
        return manySteps;
    }

    public void setManySteps(ArrayList<OneJourneyStep> manySteps) {
        this.manySteps = manySteps;
    }

    public LatLng getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(LatLng startLocation) {
        this.startLocation = startLocation;
    }

    public LatLng getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(LatLng endLocation) {
        this.endLocation = endLocation;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
