package com.socket.shortestpathfinder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ServerSection extends Activity implements View.OnClickListener {

	static final int SocketServerPORT = 8080;

	TextView infoIp, chatMsg;
	Double latlng;
	String msgLog = "";

	List<ChatClient> userList;

	ServerSocket serverSocket;
	GPSTracker gps;
	Button btnShowMap;

	ProgressDialog pbWaitToConnec;
	double latitude, longitude;
	List<Double> latlnglist;
	String lat_thread_str, long_thread_str;
	String server_lat, server_long;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		infoIp = (TextView) findViewById(R.id.infoip);

		chatMsg = (TextView) findViewById(R.id.chatmsg);

		btnShowMap = (Button) findViewById(R.id.btnShowMap);

		gps = new GPSTracker(ServerSection.this);
		btnShowMap.setOnClickListener(this);

		pbWaitToConnec = ProgressDialog.show(this, "", "Waiting for friend...",
				false);

		pbWaitToConnec.setCancelable(true);

		// check if GPS enabled
		if (gps.canGetLocation()) {

			latitude = gps.getLatitude();
			longitude = gps.getLongitude();

			server_lat = Double.toString(latitude);
			server_long = Double.toString(longitude);

		} else {
			// can't get location
			// GPS or Network is not enabled
			// Ask user to enable GPS/network in settings
			gps.showSettingsAlert();
		}

		infoIp.setText(getIpAddress());

		userList = new ArrayList<ChatClient>();

		ChatServerThread chatServerThread = new ChatServerThread();
		chatServerThread.start();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private class ChatServerThread extends Thread {

		@Override
		public void run() {
			Socket socket = null;

			try {
				serverSocket = new ServerSocket(SocketServerPORT);

				while (true) {
					socket = serverSocket.accept();

					ServerSection.this.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							pbWaitToConnec.dismiss();

						}
					});
					ChatClient client = new ChatClient();
					userList.add(client);
					ConnectThread connectThread = new ConnectThread(client,
							socket);
					connectThread.start();
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}

	}

	private class ConnectThread extends Thread {

		Socket socket;
		ChatClient connectClient;
		String msgToSend = "";

		ConnectThread(ChatClient client, Socket socket) {
			connectClient = client;
			this.socket = socket;
			client.socket = socket;
			client.chatThread = this;
		}

		@Override
		public void run() {
			DataInputStream dataInputStream = null;
			DataOutputStream dataOutputStream = null;

			try {
				dataInputStream = new DataInputStream(socket.getInputStream());
				dataOutputStream = new DataOutputStream(
						socket.getOutputStream());

				String n = dataInputStream.readUTF();

				// server receives client's latitude and longitude
				lat_thread_str = dataInputStream.readUTF();
				long_thread_str = dataInputStream.readUTF();

				Log.d("crossover", "lat and long values are :" + lat_thread_str
						+ long_thread_str);

				connectClient.name = n;

				ServerSection.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						chatMsg.setText(msgLog);
					}
				});

				// after receiving client's latitude and longitude, server
				// sending its own latitude and longitude

				dataOutputStream.writeUTF(server_lat);
				dataOutputStream.writeUTF(server_long);

				// dataOutputStream.writeUTF("Welcome " + n + "\n");
				dataOutputStream.flush();

				// broadcastMsg(n + " join our chat.\n");
				latlnglist = new ArrayList<Double>();
				int count = 0;
				while (true) {
					// if ((dataInputStream.available() > 0) && (count < 2)) {
					// count++;
					// String newMsg = dataInputStream.readUTF();
					//
					// msgLog += n + ": " + newMsg;
					// latlng = Double.parseDouble(newMsg);
					// latlnglist.add(latlng);
					//
					// Log.d("crosssover", "value is:" + latlnglist);
					// ServerSection.this.runOnUiThread(new Runnable() {
					//
					// @Override
					// public void run() {
					// chatMsg.setText(msgLog);
					// }
					// });
					//
					// // broadcastMsg(n + ": " + newMsg);
					// if (count == 2) {
					// ServerSection.this.runOnUiThread(new Runnable() {
					//
					// @Override
					// public void run() {
					// // TODO Auto-generated method stub
					// btnShowMap.setVisibility(View.VISIBLE);
					// infoIp.setVisibility(View.GONE);
					// }
					// });
					//
					// }
					// }

					if ((lat_thread_str != null) && (long_thread_str != null)) {
						btnShowMap.setVisibility(View.VISIBLE);
						break;
					}

					if (!msgToSend.equals("")) {
						dataOutputStream.writeUTF(msgToSend);
						dataOutputStream.flush();
						msgToSend = "";
					}

				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (dataInputStream != null) {
					try {
						dataInputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (dataOutputStream != null) {
					try {
						dataOutputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				userList.remove(connectClient);

				// start intent to start google map here.

			}

		}

		private void sendMsg(String msg) {
			msgToSend = msg;
		}

	}

	// private void broadcastMsg(String msg) {
	// for (int i = 0; i < userList.size(); i++) {
	// userList.get(i).chatThread.sendMsg(msg);
	// msgLog += "- send to " + userList.get(i).name + "\n";
	// }
	//
	// ServerSection.this.runOnUiThread(new Runnable() {
	//
	// @Override
	// public void run() {
	// chatMsg.setText(msgLog);
	// }
	// });
	// }

	private String getIpAddress() {
		String ip = "";
		try {
			Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
					.getNetworkInterfaces();
			while (enumNetworkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = enumNetworkInterfaces
						.nextElement();
				Enumeration<InetAddress> enumInetAddress = networkInterface
						.getInetAddresses();
				while (enumInetAddress.hasMoreElements()) {
					InetAddress inetAddress = enumInetAddress.nextElement();

					if (inetAddress.isSiteLocalAddress()) {
						ip += "SiteLocalAddress: "
								+ inetAddress.getHostAddress() + "\n";
					}

				}

			}

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ip += "Something Wrong! " + e.toString() + "\n";
		}

		return ip;
	}

	class ChatClient {
		String name;
		Socket socket;
		ConnectThread chatThread;

	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.btnShowMap:

			if ((lat_thread_str != null) && (long_thread_str != null)) {
				Intent igoogleMap = new Intent(getApplicationContext(),
						PathGoogleMapActivity.class);
				igoogleMap.putExtra("mylat", latitude);
				igoogleMap.putExtra("mylong", longitude);

				igoogleMap.putExtra("hislat",
						Double.parseDouble(lat_thread_str));
				igoogleMap.putExtra("hislong",
						Double.parseDouble(long_thread_str));

				startActivity(igoogleMap);
			} else {
				Toast.makeText(getApplicationContext(),
						"You are not connected to friend...",
						Toast.LENGTH_SHORT).show();

			}

			break;

		default:
			break;
		}

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		this.finish();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}

}
