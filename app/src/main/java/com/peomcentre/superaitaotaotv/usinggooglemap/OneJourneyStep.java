package com.peomcentre.superaitaotaotv.usinggooglemap;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by superaitaotaoTV on 14/11/14.
 */

//OneJourneyStep stores a single step in a leg obtained from Google direction api
public class OneJourneyStep {
    private String distance, duration, maneuver, travelMode, htmlInstruction;
    private LatLng start_location, end_location;
    public OneJourneyStep(LatLng start_location, LatLng end_location,String distance, String duration,
                          String maneuver, String travelMode, String htmlInstruction){
        this.start_location = start_location;
        this.end_location = end_location;
        this.distance = distance;
        this.duration = duration;
        this.maneuver = maneuver;
        this.travelMode = travelMode;
        this.htmlInstruction = htmlInstruction;
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

    public String getManeuver() {
        return maneuver;
    }

    public void setManeuver(String maneuver) {
        this.maneuver = maneuver;
    }

    public String getTravelMode() {
        return travelMode;
    }

    public void setTravelMode(String travelMode) {
        this.travelMode = travelMode;
    }

    public LatLng getEnd_location() {
        return end_location;
    }

    public void setEnd_location(LatLng end_location) {
        this.end_location = end_location;
    }

    public LatLng getStart_location() {
        return start_location;
    }

    public void setStart_location(LatLng start_location) {
        this.start_location = start_location;
    }

    public String getHtmlInstruction() {
        return htmlInstruction;
    }

    public void setHtmlInstruction(String htmlInstruction) {
        this.htmlInstruction = htmlInstruction;
    }
}
