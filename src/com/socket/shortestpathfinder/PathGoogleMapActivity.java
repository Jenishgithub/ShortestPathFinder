package com.socket.shortestpathfinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class PathGoogleMapActivity extends FragmentActivity {

	// here we come to nepal
	LatLng MYPLACE;
	LatLng FRENPLACE;

	double mylat, mylong, hislat, hislong;

	GoogleMap googleMap;
	final String TAG = "PathGoogleMapActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		boolean net_yes_or_no = isOnline();
		if (net_yes_or_no == false) {
			Toast.makeText(getApplicationContext(), "NO Internet Connection",
					Toast.LENGTH_LONG).show();
			finish();
		}
		setContentView(R.layout.activity_path_google_map);
		Bundle extra = getIntent().getExtras();
		if (extra != null) {
			mylat = extra.getDouble("mylat");
			mylong = extra.getDouble("mylong");
			hislat = extra.getDouble("hislat");
			hislong = extra.getDouble("hislong");

			MYPLACE = new LatLng(mylat, mylong);
			FRENPLACE = new LatLng(hislat, hislong);

		}

		SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		// this try catch added by me remove later
		try {
			googleMap = fm.getMap();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MarkerOptions options = new MarkerOptions();
		options.position(MYPLACE);
		options.position(FRENPLACE);

		// googleMap.addMarker(options);
		String url = getMapsApiDirectionsUrl();

		ReadTask downloadTask = new ReadTask();
		downloadTask.execute(url);

		googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MYPLACE, 13));
		addMarkers();

		// added by me .. you can delete this later
		if (googleMap == null) {
			Toast.makeText(getApplicationContext(),
					"Sorry!!! somethings wrong", Toast.LENGTH_LONG).show();

		}

	}

	private boolean isOnline() {
		// TODO Auto-generated method stub
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		return cm.getActiveNetworkInfo() != null
				&& cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}

	private String getMapsApiDirectionsUrl() {
		// TODO Auto-generated method stub
		StringBuilder urlString = new StringBuilder();
		urlString.append("http://maps.googleapis.com/maps/api/directions/json");
		urlString.append("?origin=");
		urlString.append(Double.toString(MYPLACE.latitude));

		urlString.append(",");
		urlString.append(Double.toString(MYPLACE.longitude));

		urlString.append("&destination=");
		urlString.append(Double.toString(FRENPLACE.latitude));

		urlString.append(",");
		urlString.append(Double.toString(FRENPLACE.longitude));

		// urlString.append("&waypoints=optimize:true|");
		// urlString.append(Double.toString(MAITIGHAR.latitude));
		//
		// urlString.append(",");
		// urlString.append(Double.toString(MAITIGHAR.longitude));

		urlString.append("&sensor=false&mode=driving&alternatives=true");
		return urlString.toString();
	}

	private void addMarkers() {
		if (googleMap != null) {

			googleMap.addMarker(new MarkerOptions().position(MYPLACE).title(
					"Start"));
			googleMap.addMarker(new MarkerOptions().position(FRENPLACE).title(
					"End"));
		}
	}

	private class ReadTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... url) {
			String data = "";
			try {
				HttpConnection http = new HttpConnection();
				data = http.readUrl(url[0]);
			} catch (Exception e) {
				// this exception is caught
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			new ParserTask().execute(result);
		}
	}

	private class ParserTask extends
			AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

		@Override
		protected List<List<HashMap<String, String>>> doInBackground(
				String... jsonData) {

			JSONObject jObject;
			List<List<HashMap<String, String>>> routes = null;

			try {
				jObject = new JSONObject(jsonData[0]);
				PathJSONParser parser = new PathJSONParser();
				routes = parser.parse(jObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return routes;
		}

		@Override
		protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
			ArrayList<LatLng> points = null;
			PolylineOptions polyLineOptions = null;
			// traversing through routes
			try {
				for (int i = 0; i < routes.size(); i++) {
					points = new ArrayList<LatLng>();
					polyLineOptions = new PolylineOptions();
					List<HashMap<String, String>> path = routes.get(i);
					/**
					 * get() method returns the elements at i location in the
					 * list
					 */
					for (int j = 0; j < path.size(); j++) {
						HashMap<String, String> point = path.get(j);

						double lat = Double.parseDouble(point.get("lat"));
						double lng = Double.parseDouble(point.get("lng"));
						LatLng position = new LatLng(lat, lng);
						points.add(position);
					}
					polyLineOptions.addAll(points);
					polyLineOptions.width(4);
					polyLineOptions.color(Color.BLUE);
				}
				googleMap.addPolyline(polyLineOptions);
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(getApplicationContext(),
						"Oops!! no internet connection", Toast.LENGTH_LONG)
						.show();
				startActivity(new Intent(getApplicationContext(),
						IfNoInternet.class));

			}
		}
	}

}
