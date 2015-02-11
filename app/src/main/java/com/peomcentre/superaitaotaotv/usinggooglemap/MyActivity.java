package com.peomcentre.superaitaotaotv.usinggooglemap;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MyActivity extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private final static LatLng clickTimeAdd = new LatLng(37.78564, -122.39712);
    private GoogleMap map;
    private LocationClient locationClient;
    private LocationRequest locationRequest;
    private Location currentLocation;
    private ArrayList<OnePlace> manyPlaces;
    private ArrayList<OneJouneySegment> manySegments;

    private Button getDirectionButton;
    private Button getInstructionButton;
    private RadioGroup travelModeRadio;

    private List<Polyline> polylines;
    private Marker blueMarker;
    private List<Marker> coffeeShopMarkerList;
    private List<Marker> routeMarkerList;

    private double coffeeShopLat=0, coffeeShopLng=0;
    private String travelMode;

    private boolean isHelpVisible;
    private TextView helpTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        getDirectionButton = (Button) findViewById(R.id.get_direction_button);
        getInstructionButton = (Button) findViewById(R.id.get_detail_instruction_button);
        travelModeRadio = (RadioGroup) findViewById(R.id.travel_mode_radio);

        //this contains the coffee places around the start location
        manyPlaces = new ArrayList<OnePlace>();
        //this contains the list of markers of the coffee places
        coffeeShopMarkerList = new ArrayList<Marker>();
        //this contains the different segments of the route, used in Walking & Bicycling, red
        manySegments = new ArrayList<OneJouneySegment>();
        //this contains the polylines that make up the whole route
        polylines = new ArrayList<Polyline>();
        //this contains the list of markers for the route, green
        routeMarkerList = new ArrayList<Marker>();

        setUpMapIfNeeded();

        //check whether GooglePlayService exists
        if (!(ConnectionResult.SUCCESS == GooglePlayServicesUtil.isGooglePlayServicesAvailable(this))) {
            Toast.makeText(this, "Please Install Google Play before using this app", Toast.LENGTH_SHORT);
        }

        //default travelMode is walking
        travelMode = "walking";
        travelModeRadio.check(R.id.walking_radio);
        travelModeRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.bicycle_radio:
                        travelMode = "bicycling";
                        break;
                    case R.id.walking_radio:
                        travelMode = "walking";
                        break;
                    case R.id.transit_radio:
                        travelMode = "transit";
                }
            }
        });

        //add a purple marker at ClickTime
        Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(37.78564, -122.39712)).title("ClickTime"));
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
        marker.showInfoWindow();
        map.setMyLocationEnabled(true);

        //locationClient allows retrieval of current location
        locationClient = new LocationClient(this, this, this);
        locationRequest = new LocationRequest();
        // Use high accuracy
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        locationRequest.setInterval(5);
        // Set the fastest update interval to 1 second
        locationRequest.setFastestInterval(1);

        getDirectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLocation != null) {
                    clearPreviousMarkers();
                    //coffeeShopLng
                    if (coffeeShopLng != 0) {
                        String toCoffeeShopUrl = makeDirectionURL(currentLocation.getLatitude(), currentLocation.getLongitude(),
                                coffeeShopLat, coffeeShopLng);
                        new getDirectionAyncTask(toCoffeeShopUrl).execute();
                        String toClickTimeUrl = makeDirectionURL(coffeeShopLat, coffeeShopLng, clickTimeAdd.latitude, clickTimeAdd.longitude);
                        new getDirectionAyncTask(toClickTimeUrl).execute();
                    } else {
                        String toClickTimeUrl = makeDirectionURL(currentLocation.getLatitude(), currentLocation.getLongitude(), clickTimeAdd.latitude, clickTimeAdd.longitude);
                        new getDirectionAyncTask(toClickTimeUrl).execute();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Connection Problem, please check your network", Toast.LENGTH_SHORT).show();
                }
            }
        });

        getInstructionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewInstructionIntent = new Intent(MyActivity.this, InstructionPage.class);
                String instructionText = getTextInstructions();
                if (instructionText.equals("")) {
                    Toast.makeText(MyActivity.this, "You need to get directions first", Toast.LENGTH_SHORT).show();
                } else {
                    viewInstructionIntent.putExtra("manySegments", getTextInstructions());
                    startActivity(viewInstructionIntent);
                }
            }
        });

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (blueMarker != null) {
                    blueMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
                if (coffeeShopMarkerList.contains(marker)) {
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    blueMarker = marker;
                    coffeeShopLat = marker.getPosition().latitude;
                    coffeeShopLng = marker.getPosition().longitude;
                }
                marker.showInfoWindow();
                return true;
            }
        });

        helpTextView = (TextView) findViewById(R.id.help_text_view);
        isHelpVisible = false;
    }

    private void clearPreviousMarkers(){
        manySegments.clear();
        //remove markers
        for(Marker m: routeMarkerList) {
            m.remove();
        }
        routeMarkerList.clear();
        //remove polylines
        if(polylines!=null){
            for(Polyline line: polylines){
                line.remove();
            }
        }
    }

    //this creates the detail text instructions from the route information stored
    private String getTextInstructions(){
        StringBuilder builder = new StringBuilder();
        if(manySegments==null||manySegments.size()==0){return "";}
        for(int i=0; i<manySegments.size(); i++){
            OneJouneySegment segment = manySegments.get(i);
            builder.append("From: "+ segment.getStartAddress()+"\nTo: "+segment.getEndAddress()+"\n\n");
            for(int j=0; j<manySegments.get(i).getManySteps().size(); j++){
                OneJourneyStep step = segment.getManySteps().get(j);
                builder.append("Step "+(j+1)+": "+step.getHtmlInstruction()+"\n"+
                        "Distance: "+step.getDistance()+";Duration: "+step.getDuration()+"\n\n");
            }
            builder.append("\n");
        }

        return builder.toString();
    }

    //set up google map
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (map != null) {
                // The Map is verified. It is now safe to manipulate the map.

            }
        }
    }

    //this Asynctask download JSON from Google Direction Api
    private class getDirectionAyncTask extends AsyncTask<Void, Void, String> {
        private ProgressDialog progressDialog;
        String url;
        getDirectionAyncTask(String urlPass){
            url = urlPass;
            Log.i("DirectionURL",url);
        }
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            progressDialog = new ProgressDialog(MyActivity.this);
            progressDialog.setMessage("Fetching route, Please wait...");
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }
        @Override
        protected String doInBackground(Void... params) {
            JSONParser jParser = new JSONParser();
            String json = jParser.getJSONFromUrl(url);
            return json;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.hide();
            if(result!=null){
                drawPath(result);
                getInstructionFromJSON(result);
            }
        }
    }

    //this method get detail instructions from JSON downloaded & add to the existing manySegments
    private void getInstructionFromJSON(String result){
        try{
            final JSONObject json = new JSONObject(result);
            JSONObject route = (JSONObject)json.getJSONArray("routes").get(0);
            JSONArray routeSegments = route.getJSONArray("legs");
            for(int i=0; i<routeSegments.length(); i++){
                JSONObject segment = (JSONObject)routeSegments.get(i);
                String distance = segment.getJSONObject("distance").getString("text");
                String duration = segment.getJSONObject("duration").getString("text");
                String startAddress = segment.getString("start_address");
                String endAddress = segment.getString("end_address");
                Log.i("Segment","From "+startAddress+" To "+endAddress);
                JSONObject startLocationObject = segment.getJSONObject("start_location");
                LatLng startLocation = new LatLng(startLocationObject.getDouble("lat"),startLocationObject.getDouble("lng"));
                JSONObject endLocationObject = segment.getJSONObject("end_location");
                LatLng endLocation = new LatLng(endLocationObject.getDouble("lat"),endLocationObject.getDouble("lng"));
                OneJouneySegment oneJouneySegment = new OneJouneySegment(startAddress,endAddress,distance,duration,
                        startLocation,endLocation);
                JSONArray manySteps = segment.getJSONArray("steps");
                ArrayList<OneJourneyStep> manyJourneySteps = new ArrayList<OneJourneyStep>();
                for(int j=0; j<manySteps.length(); j++){
                    JSONObject step = (JSONObject)manySteps.get(j);
                    String stepDistance = step.getJSONObject("distance").getString("text");
                    String stepDuration = step.getJSONObject("duration").getString("text");
                    LatLng stepStartLocation = new LatLng(step.getJSONObject("start_location").getDouble("lat"),
                            step.getJSONObject("start_location").getDouble("lng"));
                    LatLng stepEndLocation = new LatLng(step.getJSONObject("end_location").getDouble("lat"),
                            step.getJSONObject("end_location").getDouble("lng"));
                    String maneuver = "null";
                    String htmlInstruction = Html.fromHtml(step.getString("html_instructions")).toString();
                    Marker marker =  map.addMarker(new MarkerOptions().position(stepStartLocation).title(htmlInstruction));
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    routeMarkerList.add(marker);
                    //step.getString("maneuver");

                    String travelMode = step.getString("travel_mode");
                    OneJourneyStep oneStep = new OneJourneyStep(stepStartLocation,stepEndLocation,stepDistance,
                            stepDuration,maneuver,travelMode,htmlInstruction);
                    manyJourneySteps.add(oneStep);
                }
                oneJouneySegment.setManySteps(manyJourneySteps);
                manySegments.add(oneJouneySegment);
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    //this AsyncTask download and decode JSON from Google Place Api
    private class getCoffeePlaceAsyncTask extends AsyncTask<Void, Void, String>{
        String url;
        getCoffeePlaceAsyncTask(String url){
            this.url = url;
            Log.i("URL:",url);
        }
        @Override
        protected String doInBackground(Void... voids) {
            String result = JSONParser.getJSONFromUrl(url);
            Log.i("JSON",result);
            return result;
        }
        @Override
        protected void onPostExecute(String s) {
            if (s!=null){
                try {
                    manyPlaces = new ArrayList<OnePlace>();
                    final JSONObject json = new JSONObject(s);
                    JSONArray placeArray = json.getJSONArray("results");
                    for(int i=0; i<placeArray.length(); i++){
                        JSONObject onePlace = placeArray.getJSONObject(i);
                        String name = onePlace.getString("name");
                        JSONObject location = onePlace.getJSONObject("geometry").getJSONObject("location");
                        double lat = location.getDouble("lat");
                        double lng = location.getDouble("lng");
                        Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(lat,lng)).title(name));
                        coffeeShopMarkerList.add(marker);
                        manyPlaces.add(new OnePlace(name, lat, lng));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //ExpandableListViewAdapter adapter= new ExpandableListViewAdapter(MyActivity.this,manyPlaces);
                //listView.setAdapter(adapter);
            }
        }
    }

    /*sample Google Place Api
    https://maps.googleapis.com/maps/api/place/search/json?keyword=coffee+donut&location=37.787930,-122.4074990
            &radius=10000&key=YOUR_API_KEY}*/

    public String makePlaceUrl (double sourcelat, double sourcelog){
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/place/search/json");
        urlString.append("?keyword=");
        urlString.append("coffee+donut");
        urlString.append("&location=");
        urlString.append(Double.toString(sourcelat)+",");
        urlString.append(Double.toString( sourcelog));
        urlString.append("&radius=");
        urlString.append("10000");
        urlString.append("&key=AIzaSyAZKmWv5ZYQ8xvID2OEQKnaNwkIvKwq3Ag");
        return urlString.toString();
    }

    /* sample Google Direction Api:
    https://maps.googleapis.com/maps/api/directions/json?origin=37.8694038,-122.2510964&destination=37.78564,-122.39712
    &waypoints=37.87583,-122.260025&key=AIzaSyAxkURR1ks-IHM0JplMBvX82Q4-Mp2DR0M&departure_time=1416025870&mode=transit&alternatives=true*/

    public String makeDirectionURL (double sourcelat, double sourcelng, double destlat, double destlog ){
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(sourcelat);
        urlString.append(",");
        urlString.append(sourcelng);
        urlString.append("&destination=");// to
        urlString.append(Double.toString(destlat));
        urlString.append(",");
        urlString.append(Double.toString(destlog));
        urlString.append("&key=AIzaSyAxkURR1ks-IHM0JplMBvX82Q4-Mp2DR0M");
        urlString.append("&departure_time="+System.currentTimeMillis()/1000);
        urlString.append("&mode=");
        urlString.append(travelMode +"&alternatives=true");
        return urlString.toString();
    }

    //this function draws route on the map
    public void drawPath(String  result) {
        try {
            //Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
                JSONObject routes = routeArray.getJSONObject(0);
                JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
                String encodedString = overviewPolylines.getString("points");
                List<LatLng> list = decodePoly(encodedString);

                for (int z = 0; z < list.size() - 1; z++) {
                    LatLng src = list.get(z);
                    LatLng dest = list.get(z + 1);
                    Polyline line = map.addPolyline(new PolylineOptions()
                            .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude, dest.longitude))
                            .width(3)
                            .color(Color.BLUE).geodesic(true));
                    polylines.add(line);
                }
        }
        catch (JSONException e) {

        }
    }


    //this decodes the "overview polylines" from google api into points
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationClient.connect();
    }

    @Override
    protected void onStop() {
        if (locationClient.isConnected()){
            locationClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        currentLocation = locationClient.getLastLocation();
        if (currentLocation == null) {
            locationClient.requestLocationUpdates(locationRequest, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    currentLocation = location;
                    new getCoffeePlaceAsyncTask(makePlaceUrl(currentLocation.getLatitude(), currentLocation.getLongitude())).execute();
                    locationClient.removeLocationUpdates(this);
                    //restart the activity due to some android wifi bug..
                    if (locationRequest.getNumUpdates() > 20) {
                        recreate();
                    }

                }
            });
        }else {
            new getCoffeePlaceAsyncTask(makePlaceUrl(currentLocation.getLatitude(), currentLocation.getLongitude())).execute();
        }
    }

    @Override
    public void onDisconnected() {
        // Display the connection status
        Log.i("Location", "disconnected");

    }
    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        ConnectionResult.RESOLUTION_REQUIRED);
                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
             //showErrorDialog(connectionResult.getErrorCode());
            Toast.makeText(this,"Fail to connect to server",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //click on the help button to show/hide help information
        if (id == R.id.help) {
            if(isHelpVisible){
                helpTextView.setVisibility(View.GONE);
            }else{
                helpTextView.setVisibility(View.VISIBLE);
            }
            isHelpVisible = !isHelpVisible;
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
