package com.socket.shortestpathfinder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ClientSection extends Activity {

	static final int SocketServerPORT = 8080;

	LinearLayout loginPanel, chatPanel;

	EditText editTextUserName, editTextAddress;
	Button buttonConnect, btnShowMapClient;
	TextView textPort;

	EditText editTextSay;
	Button buttonSend;
	Button buttonDisconnect;

	String msgLog = "";

	ChatClientThread chatClientThread = null;
	GPSTracker gpstracker;
	Double lat_client, long_client;
	TextView tvSuccesfulconnection;
	String server_lat;
	String server_long;
	String lat_thread_str, long_thread_str;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client_layout);

		loginPanel = (LinearLayout) findViewById(R.id.loginpanel);
		chatPanel = (LinearLayout) findViewById(R.id.chatpanel);

		tvSuccesfulconnection = (TextView) findViewById(R.id.tvSuccesfulconnection);

		editTextUserName = (EditText) findViewById(R.id.username);
		editTextAddress = (EditText) findViewById(R.id.address);

		buttonConnect = (Button) findViewById(R.id.connect);
		buttonDisconnect = (Button) findViewById(R.id.disconnect);

		buttonConnect.setOnClickListener(buttonConnectOnClickListener);
		buttonDisconnect.setOnClickListener(buttonDisconnectOnClickListener);

		editTextSay = (EditText) findViewById(R.id.say);
		buttonSend = (Button) findViewById(R.id.send);

		btnShowMapClient = (Button) findViewById(R.id.btnShowMapClient);

		buttonSend.setOnClickListener(buttonSendOnClickListener);

		btnShowMapClient.setOnClickListener(btnShowMapClientOnclicklisterner);

		gpstracker = new GPSTracker(ClientSection.this);
	}

	OnClickListener buttonDisconnectOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (chatClientThread == null) {
				return;
			}
			chatClientThread.disconnect();
		}

	};

	OnClickListener buttonSendOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (editTextSay.getText().toString().equals("")) {
				return;
			}

			if (chatClientThread == null) {
				return;
			}

			chatClientThread.sendMsg(editTextSay.getText().toString() + "\n");
		}

	};

	OnClickListener buttonConnectOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (gpstracker.canGetLocation()) {
				lat_client = gpstracker.getLatitude();
				long_client = gpstracker.getLongitude();
			}

			String lat_client_str = Double.toString(lat_client);
			String long_client_str = Double.toString(long_client);

			Log.d("crossover", "lat and long values:" + lat_client_str
					+ long_client_str);

			String textUserName = editTextUserName.getText().toString();
			if (textUserName.equals("")) {
				Toast.makeText(ClientSection.this, "Enter User Name",
						Toast.LENGTH_LONG).show();
				return;
			}

			String textAddress = editTextAddress.getText().toString();
			if (textAddress.equals("")) {
				Toast.makeText(ClientSection.this, "Enter Addresse",
						Toast.LENGTH_LONG).show();
				return;
			}

			msgLog = "";

			loginPanel.setVisibility(View.GONE);
			chatPanel.setVisibility(View.VISIBLE);

			chatClientThread = new ChatClientThread(textUserName, textAddress,
					SocketServerPORT, lat_client_str, long_client_str);
			chatClientThread.start();
		}

	};

	OnClickListener btnShowMapClientOnclicklisterner = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Toast.makeText(getApplicationContext(), "Show map in client",
					Toast.LENGTH_SHORT).show();
			Intent goToClientMap = new Intent(getApplicationContext(),
					ClientMap.class);
			goToClientMap.putExtra("clientlat",
					Double.parseDouble(lat_thread_str));
			goToClientMap.putExtra("clientlong",
					Double.parseDouble(long_thread_str));
			goToClientMap.putExtra("serverlat", Double.parseDouble(server_lat));
			goToClientMap.putExtra("serverlong",
					Double.parseDouble(server_long));

			startActivity(goToClientMap);

		}
	};

	private class ChatClientThread extends Thread {

		String name;
		String dstAddress;
		int dstPort;

		String msgToSend = "";
		boolean goOut = false;

		ChatClientThread(String name, String address, int port, String lat,
				String lng) {
			this.name = name;
			dstAddress = address;
			dstPort = port;
			lat_thread_str = lat;
			long_thread_str = lng;
		}

		@Override
		public void run() {
			Socket socket = null;
			DataOutputStream dataOutputStream = null;
			DataInputStream dataInputStream = null;

			try {
				socket = new Socket(dstAddress, dstPort);
				dataOutputStream = new DataOutputStream(
						socket.getOutputStream());
				dataInputStream = new DataInputStream(socket.getInputStream());
				dataOutputStream.writeUTF(name);

				// client sending its lattitude and longitude to server
				dataOutputStream.writeUTF(lat_thread_str);
				dataOutputStream.writeUTF(long_thread_str);

				dataOutputStream.flush();

				while (!goOut) {
					if (dataInputStream.available() > 0) {
						// client receiving server's latitude and longitude
						// after sending its own
						server_lat = dataInputStream.readUTF();
						server_long = dataInputStream.readUTF();

						Log.d("crossover",
								"server's latitude and longitude are:"
										+ server_lat + server_long);

						if ((!server_lat.isEmpty()) && (!server_long.isEmpty())) {
							break;
						}

						msgLog += dataInputStream.readUTF();

					}

					if (!msgToSend.equals("")) {
						dataOutputStream.writeUTF(msgToSend);
						dataOutputStream.flush();
						msgToSend = "";
					}
				}

			} catch (UnknownHostException e) {
				e.printStackTrace();
				final String eString = e.toString();
				ClientSection.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(ClientSection.this, eString,
								Toast.LENGTH_LONG).show();
					}

				});
			} catch (IOException e) {
				e.printStackTrace();
				final String eString = e.toString();
				ClientSection.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(ClientSection.this, eString,
								Toast.LENGTH_LONG).show();
					}

				});
			} finally {
				if (socket != null) {
					try {
						socket.close();
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

				if (dataInputStream != null) {
					try {
						dataInputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				ClientSection.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						loginPanel.setVisibility(View.GONE);
						chatPanel.setVisibility(View.GONE);
						btnShowMapClient.setVisibility(View.VISIBLE);
						tvSuccesfulconnection.setVisibility(View.VISIBLE);
					}

				});
			}

		}

		private void sendMsg(String msg) {
			msgToSend = msg;
		}

		private void disconnect() {
			goOut = true;
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}

}