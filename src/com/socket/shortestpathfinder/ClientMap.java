package com.socket.shortestpathfinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class ClientMap extends FragmentActivity {

	GoogleMap googleMap;
	final String TAG = "PathGoogleMapActivity";

	Double clientlat, clientlong, serverlat, serverlong;
	LatLng CLIENT;
	LatLng SERVER;
	Button btnGoBackClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.clientmap);

		btnGoBackClient = (Button) findViewById(R.id.btnGoBackClient);

		boolean net_yes_or_no = isOnline();
		if (net_yes_or_no == false) {
			Toast.makeText(getApplicationContext(), "NO Internet Connection",
					Toast.LENGTH_LONG).show();
			finish();
		}

		btnGoBackClient.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.clientmap);
		googleMap = fm.getMap();

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			clientlat = extras.getDouble("clientlat");
			clientlong = extras.getDouble("clientlong");
			serverlat = extras.getDouble("serverlat");
			serverlong = extras.getDouble("serverlong");

		}

		CLIENT = new LatLng(clientlat, clientlong);
		SERVER = new LatLng(serverlat, serverlong);

		Log.d("crosover", "here lat and longs are:" + CLIENT + SERVER);

		MarkerOptions options = new MarkerOptions();
		options.position(CLIENT);

		options.position(SERVER);

		googleMap.addMarker(options);
		String url = getMapsApiDirectionsUrl();
		// String url1 = getMapsApiDirectionsUrl1();

		ReadTask downloadTask = new ReadTask();
		downloadTask.execute(url);

		googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SERVER, 13));
		addMarkers();

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
		urlString.append(Double.toString(CLIENT.latitude));

		urlString.append(",");
		urlString.append(Double.toString(CLIENT.longitude));

		urlString.append("&destination=");
		urlString.append(Double.toString(SERVER.latitude));

		urlString.append(",");
		urlString.append(Double.toString(SERVER.longitude));

		// urlString.append("&waypoints=optimize:true|");
		// urlString.append(Double.toString(WALL_STREET.latitude));
		//
		// urlString.append(",");
		// urlString.append(Double.toString(WALL_STREET.longitude));

		urlString.append("&sensor=false&mode=driving&alternatives=true");
		return urlString.toString();
	}

	private void addMarkers() {
		if (googleMap != null) {
			googleMap.addMarker(new MarkerOptions().position(CLIENT).title(
					"Through Point"));
			googleMap.addMarker(new MarkerOptions().position(SERVER).title(
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
			for (int i = 0; i < routes.size(); i++) {
				points = new ArrayList<LatLng>();
				polyLineOptions = new PolylineOptions();
				List<HashMap<String, String>> path = routes.get(i);
				/** get() method returns the elements at i location in the list */
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
		}
	}

}
