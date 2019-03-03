package com.example.weather_app;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements LocationListener {

    TextView data,current_temperature,current_wind,current_humidity,unit;
    //ImageButton fetch;
    ToggleButton forecast_method;
    ImageView icon;
    private LocationManager locationManager;
    protected Location location;
    private LineChart tempchart;
    private String longitude, latitude, test;
    protected ArrayList<String> temperature = new ArrayList<String>();
    protected ArrayList<String> pressure = new ArrayList<String>();
    protected ArrayList<String> sea_level = new ArrayList<String>();
    protected ArrayList<String> grnd_level = new ArrayList<String>();
    protected ArrayList<String> humidity = new ArrayList<String>();
    protected ArrayList<String> weather_condition = new ArrayList<String>();
    protected ArrayList<String> weather_description = new ArrayList<String>();
    protected ArrayList<String> clouds = new ArrayList<String>();
    protected ArrayList<String> wind_speed = new ArrayList<String>();
    protected ArrayList<String> wind_direction = new ArrayList<String>();
    protected ArrayList<String> time = new ArrayList<String>();
    protected String type="forecast";
    protected String filter="hourly"; // For option Hourly/Daily
    protected String ctemperature, chumidity, cwind,cunit,weather_icon;
    protected BarChart windchart;
    protected String aapid = ""; // Please provide your aapdi over here


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        data = (TextView) findViewById(R.id.error);
        //fetch = (ImageButton) findViewById(R.id.imageButton);
        tempchart = (LineChart) findViewById(R.id.linechart);
        current_temperature = (TextView) findViewById(R.id.temperature);
        current_humidity = (TextView) findViewById(R.id.humidity);
        current_wind = (TextView) findViewById(R.id.windy);
        unit = (TextView) findViewById(R.id.unit);
        forecast_method = (ToggleButton) findViewById(R.id.toggleButton);
        icon = (ImageView) findViewById(R.id.weathericon);
        windchart = (BarChart) findViewById(R.id.windchart);

        forecast_method.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    filter = "daily";
                    new processdata().execute();
                } else {
                    // The toggle is disabled
                    filter = "hourly";
                    new processdata().execute();
                }
            }
        });

        //tempchart.setViewPortOffsets(50f, 0f, 5000f, 0f);
        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        //provider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            data.setText("Please grant location access to application");

            return;
        }

        if (!isLocationEnabled()) {
            showAlert();

        }


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, this);
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        // Initialize the location fields
        if (location != null) {
            //System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
            locationManager.removeUpdates(this);

        } else {
            data.setText("Waiting for Location");
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        new processdata().execute();

    }

/*    private void fetchlocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        onLocationChanged(location);
        //locationManager.removeUpdates(this);

    }
*/
    // For graph
    private void setData() {
        ArrayList<String> xVals = setXAxisValues();

        ArrayList<Entry> yVals = setYAxisValues();

        LineDataSet set1;
        XAxis xaxis = tempchart.getXAxis();


        // create a dataset and give it a type
        set1 = new LineDataSet(yVals, "DataSet 1");
        set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);

        // set the line to be drawn like this "- - - - - -"
        // set1.enableDashedLine(10f, 5f, 0f);
        // set1.enableDashedHighlightLine(10f, 5f, 0f);
        set1.setColor(Color.BLACK);
        set1.setCircleColor(Color.BLACK);
        set1.setLineWidth(1f);
        //set1.setCircleRadius(3f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(9f);
        set1.setDrawFilled(true);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);
        data.setValueTextSize(15f);

        // set data
        tempchart.setData(data);

        windchart.setDescription("Wind Speed");
        windchart.setDescriptionTextSize(50f);


        if(filter == "hourly"){
            // Wind chart

            ArrayList<BarEntry> BarEntry = new ArrayList<>();

            for (int i = 0; i < wind_speed.size(); i++) {
                BarEntry.add(new BarEntry(Float.parseFloat(wind_speed.get(i)), i));
            }

            BarDataSet dataSet = new BarDataSet(BarEntry, "Wind Speed");

            BarData winddata = new BarData(time, dataSet);
            dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
            windchart.setData(winddata);
        }

    }

    // Getting X-axis for graph
    // This is used to store x-axis values
    private ArrayList<String> setXAxisValues(){

        return time;
    }

    // Getting y-axis for graph
    // This is used to store Y-axis values
    private ArrayList<Entry> setYAxisValues(){
        ArrayList<Entry> yVals = new ArrayList<Entry>();

        for (int i = 0; i < temperature.size(); i++) {
            yVals.add(new Entry(Float.parseFloat(temperature.get(i)), i));
        }

        return yVals;
    }

    @Override
    public void onLocationChanged(Location location) {

        int lat = (int) (location.getLatitude());
        int lng = (int) (location.getLongitude());
        latitude = String.valueOf(lat);
        longitude = String.valueOf(lng);
        System.out.println("\n\n"+latitude+"\n\n");
        System.out.println("\n\n"+longitude+"\n\n");
        data.setText("");

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {

        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    // Checking if location service is enabled or not
    protected boolean isLocationEnabled(){
        String le = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) getSystemService(le);
        if(!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            return false;
        } else {
            return true;
        }
    }

    // Redirecting user to enable location service and give permission
    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    // Checking internet connection for accessing site
    private boolean checkInternetConenction() {
        // get Connectivity Manager object to check connection
        ConnectivityManager connec
                =(ConnectivityManager)getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

        // Check for network connections
        if ( connec.getNetworkInfo(0).getState() ==
                android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() ==
                        android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() ==
                        android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED ) {
            //Toast.makeText(this, " Connected ", Toast.LENGTH_LONG).show();
            return true;
        }else if (
                connec.getNetworkInfo(0).getState() ==
                        android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() ==
                                android.net.NetworkInfo.State.DISCONNECTED  ) {
            //Toast.makeText(this, " Not Connected ", Toast.LENGTH_LONG).show();
            return false;
        }
        return false;
    }


    // Connecting to url and than processing data that is obtained
    private class processdata extends AsyncTask<Void, Void, String>{


        protected String doInBackground(Void... params){

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            temperature.clear();
            pressure.clear();
            sea_level.clear();
            grnd_level.clear();
            humidity.clear();
            weather_condition.clear();
            weather_description.clear();
            clouds.clear();
            wind_speed.clear();
            wind_direction.clear();
            time.clear();


            // Will contain the raw JSON response as a string.
            String forecastJson = null;

            for(int j=0; j<2; j++){

                try {

                    while (latitude == null){
                        Thread.currentThread();
                        Thread.sleep(5000);
                        System.out.println("\nWaiting........\n\n");
                        //fetchlocation();
                    }

                    // Construct the URL for the OpenWeatherMap query
                    // Possible parameters are avaiable at OWM's forecast API page, at
                    // http://openweathermap.org/API#forecast

                    URL url;

                    if (j == 0){

                        type="current";
                        Uri.Builder builder = new Uri.Builder();
                        builder.scheme("https")
                                .authority("api.openweathermap.org")
                                .appendPath("data")
                                .appendPath("2.5")
                                .appendPath("weather")
                                //.appendPath("daily")
                                .appendQueryParameter("lat", latitude)
                                .appendQueryParameter("lon", longitude)
                                .appendQueryParameter("units", "imperial")
                                .appendQueryParameter("mode", "json")
                                .appendQueryParameter("APPID", aapid);

                        url = new URL(builder.build().toString());
                    }
                    else{
                        type="forecast";
                        Uri.Builder builder = new Uri.Builder();
                        builder.scheme("https")
                                .authority("api.openweathermap.org")
                                .appendPath("data")
                                .appendPath("2.5")
                                .appendPath("forecast")
                                //.appendPath("daily")
                                .appendQueryParameter("lat", latitude)
                                .appendQueryParameter("lon", longitude)
                                .appendQueryParameter("units", "imperial")
                                .appendQueryParameter("mode", "json")
                                .appendQueryParameter("APPID", aapid);

                        url = new URL(builder.build().toString());
                        //System.out.println("\n\nForecasting\n");
                    }

                    // Temperature unit
                    cunit="F";

                    // Create the request to OpenWeatherMap, and open the connection
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        // Nothing to do.
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                        // But it does make debugging a *lot* easier if you print out the completed
                        // buffer for debugging.
                        buffer.append(line).append("\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty.  No point in parsing.
                        return null;
                    }
                    forecastJson = buffer.toString();

                    // Processing and cleaning Json data

                    try {
                        JSONObject full_data = new JSONObject(forecastJson);

                        String date=null,datedaily=null;
                        String swapchar=null;
                        String time_check=null,temp_time=null,temp_date=null;
                        float min=0, max=0,avg=0;
                        int count=0;
                        if(type.equals("forecast")){
                            JSONArray data_list = full_data.getJSONArray("list");
                            for (int i=0; i<data_list.length(); i++){


                                JSONObject main_reader = data_list.getJSONObject(i);

                                // Fetching time
                                time_check = main_reader.getString("dt_txt");
                                if(i == 0){
                                    date = String.valueOf(time_check.charAt(8)) + String.valueOf(time_check.charAt(9));
                                    datedaily = String.valueOf(time_check.charAt(5)) + String.valueOf(time_check.charAt(6)) + "/" + date;
                                }

                                temp_date = String.valueOf(time_check.charAt(8)) + String.valueOf(time_check.charAt(9));

                                if(filter.equals("hourly")){
                                    if(!temp_date.equals(date)){
                                        break;
                                    }
                                    temp_time = String.valueOf(time_check.charAt(11)) + String.valueOf(time_check.charAt(12));
                                    System.out.println("----------"+time_check.charAt(11)+"-----------"+String.valueOf(time_check.charAt(11)));
                                    int ampm = Integer.parseInt(temp_time);
                                    System.out.println(ampm);
                                    if(ampm > 12){
                                        ampm = ampm - 12;
                                        temp_time = ampm + " PM";
                                    }
                                    else if(ampm == 12){
                                        temp_time = temp_time + " PM";
                                    }
                                    else{
                                        temp_time = temp_time + " AM";
                                    }
                                    time.add(i,temp_time);
                                }
                                else{

                                    if(i == 0){
                                        swapchar = datedaily;
                                    }
                                    else {

                                        swapchar = String.valueOf(time_check.charAt(5)) + String.valueOf(time_check.charAt(6));
                                        swapchar = swapchar + "/" + temp_date;
                                    }

                                    JSONObject data_reader = main_reader.getJSONObject("main");
                                    float temp1 = Float.parseFloat(data_reader.getString("temp"));

                                    if(swapchar.equals(datedaily)){

                                        if(i == 0){
                                            min = temp1;
                                            max = temp1;
                                        }
                                        if(temp1 == min || temp1 == max){
                                            if(i == (data_list.length() - 1)){

                                                time.add(count,swapchar);
                                                avg = (min+max)/2;
                                                String minmax = Float.toString(min); //+ " " + Float.toString(max);
                                                temperature.add(count,minmax);
                                            }
                                            continue;
                                        }
                                        else if(temp1 > max){
                                            max = temp1;

                                            if(i == (data_list.length() - 1)){

                                                time.add(count,swapchar);
                                                avg = (min+max)/2;
                                                String minmax = Float.toString(min); //+ " " + Float.toString(max);
                                                temperature.add(count,minmax);
                                            }
                                            continue;
                                        }
                                        else if(temp1 < min){

                                            min = temp1;
                                            if(i == (data_list.length() - 1)){

                                                time.add(count,swapchar);
                                                avg = (min+max)/2;
                                                String minmax = Float.toString(min); //+ " " + Float.toString(max);
                                                temperature.add(count,minmax);

                                            }
                                            continue;
                                        }
                                        else {
                                            continue;
                                        }
                                    }
                                    else{

                                        avg = (min+max)/2;
                                        String minmax = Float.toString(min); //+ " " + Float.toString(max);
                                        time.add(count,datedaily);
                                        temperature.add(count,minmax);
                                        count++;

                                        min = temp1;
                                        max = temp1;
                                        datedaily = swapchar;
                                        continue;
                                    }



                                }


                                //time.add(i,main_reader.getString("dt_txt"));
                                // Fetching main
                                if (filter.equals("hourly")){

                                    JSONObject data_reader = main_reader.getJSONObject("main");

                                    temperature.add(i,data_reader.getString("temp"));
                                    System.out.println("\n\n"+data_reader.getString("temp"));
                                    pressure.add(i,data_reader.getString("pressure"));
                                    sea_level.add(i,data_reader.getString("sea_level"));
                                    grnd_level.add(i,data_reader.getString("grnd_level"));
                                    humidity.add(i,data_reader.getString("humidity"));

                                    // Fetching weather
                                    JSONArray data_reader_arr = main_reader.getJSONArray("weather");
                                    JSONObject temp = data_reader_arr.getJSONObject(0);

                                    weather_condition.add(i,temp.getString("main"));
                                    weather_description.add(i,temp.getString("description"));

                                    // Fetching clouds
                                    data_reader = main_reader.getJSONObject("clouds");
                                    clouds.add(i,data_reader.getString("all"));

                                    // Fetching wind
                                    data_reader = main_reader.getJSONObject("wind");
                                    wind_speed.add(i,data_reader.getString("speed"));
                                    wind_direction.add(i,data_reader.getString("deg"));
                                }


                            }
                        }
                        else if(type.equals("current")){

                            JSONArray data_reader =full_data.getJSONArray("weather");
                            String concate=null;

                            for (int i=0; i<data_reader.length(); i++){
                                JSONObject sub_reader = data_reader.getJSONObject(i);

                                weather_icon = sub_reader.getString("icon");
                            }

                            JSONObject data_reader1 = full_data.getJSONObject("main");
                            ctemperature = data_reader1.getString("temp");
                            chumidity = data_reader1.getString("humidity");

                            data_reader1 = full_data.getJSONObject("wind");
                            cwind = data_reader1.getString("speed");

                        }
                        else{
                            System.out.print("Unknown type captured:"+type);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        System.out.println("\n\nJSON exception catched for full_data\n\n");
                    }

                    if(j == 1){

                        // Calling graph operations
                        tempchart.invalidate();
                        tempchart.setDescription("Temperature Forecast");
                        tempchart.setScaleEnabled(false);
                        tempchart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                        tempchart.getXAxis().setTextSize(20f);
                        tempchart.setNoDataTextDescription("Not able to load data");

                        if(filter == "daily"){
                            windchart.invalidate();
                            windchart.clear();
                        }
                        else {
                            windchart.invalidate();
                            windchart.setScaleEnabled(false);
                            windchart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                            windchart.getXAxis().setTextSize(10f);
                        }

                        // add data
                        setData();

                        // get the legend (only possible after setting data)
                        Legend l = tempchart.getLegend();

                        // modify the legend ...
                        // l.setPosition(LegendPosition.LEFT_OF_CHART);
                        l.setForm(Legend.LegendForm.LINE);
                    }


                    //return forecastJson;
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    System.out.println("Interrupted Exception catched");
                    return null;
                }catch (MalformedURLException e) {
                    e.printStackTrace();
                    System.out.println("Malformed url exception catched");
                    return null;
                } catch (IOException e) {
                    Log.e("PlaceholderFragment", "Error ", e);
                    System.out.println("IOException exception catched="+e);
                    // If the code didn't successfully get the weather data, there's no point in attemping
                    // to parse it.
                    return null;
                } finally{
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                        //System.out.println("\n\nClosing connection\n\n");

                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e("PlaceholderFragment", "Error closing stream", e);
                        }
                    }
                }
            }

            return forecastJson;
        }

        // Display data to user
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s != null){
                Log.i("json", s);
                //data.setText(s);
                current_temperature.setText(ctemperature);
                current_humidity.setText("Humidity:"+chumidity);
                current_wind.setText("Wind:"+cwind+"Mph");
                unit.setText(cunit);
                String str = "i"+weather_icon;
                String uri = "@drawable/"+ str;  // where myresource (without the extension) is the file

                int imageResource = getResources().getIdentifier(uri, null, getPackageName());

                Drawable path = getResources().getDrawable(imageResource);

                System.out.println("\n\n====="+str+"======"+path);
                icon.setImageDrawable(path);
            }
            else{
                data.setText("Not able to fetch data!!!!!\n Try after some time...\n"+test);
            }

        }

    }
}
