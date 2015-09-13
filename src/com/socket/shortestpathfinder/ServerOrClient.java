package com.socket.shortestpathfinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ServerOrClient extends Activity implements OnClickListener {
	Button btnServer, btnClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.serverorclient);
		btnServer = (Button) findViewById(R.id.btnServer);
		btnClient = (Button) findViewById(R.id.btnClient);

		btnServer.setOnClickListener(this);
		btnClient.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btnServer:
			startActivity(new Intent(getApplicationContext(),
					ServerSection.class));
			break;

		case R.id.btnClient:
			startActivity(new Intent(getApplicationContext(),
					ClientSection.class));
			break;

		default:
			break;
		}

	}

}
