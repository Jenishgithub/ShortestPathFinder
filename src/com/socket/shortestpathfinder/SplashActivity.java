package com.socket.shortestpathfinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle Var) {
		// TODO Auto-generated method stub
		super.onCreate(Var);
		setContentView(R.layout.splash);
		Thread timer = new Thread() {
			public void run() {
				try {
					sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();

				} finally {

					startActivity(new Intent(getApplicationContext(),
							ServerOrClient.class));
				}

			}

		};

		timer.start();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}

}