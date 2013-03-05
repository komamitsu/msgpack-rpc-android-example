package com.komamitsu.android.rpctest;

import java.net.UnknownHostException;

import org.msgpack.rpc.Client;
import org.msgpack.rpc.loop.EventLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static interface RPCInterface {
		int add(int a, int b);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		Logger logger = LoggerFactory.getLogger(this.getClass());
		logger.info("hogehoge");
		
		EventLoop loop = EventLoop	.defaultEventLoop();
		try {
			Client client = new Client("192.168.xxx.xxx", 9090, loop);
			RPCInterface rpcInterface = client.proxy(RPCInterface.class);
			int result = rpcInterface.add(123, 456);
			Toast.makeText(this, "result => " + result, Toast.LENGTH_LONG).show();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
