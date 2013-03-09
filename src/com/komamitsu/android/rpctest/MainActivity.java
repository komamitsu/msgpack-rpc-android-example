package com.komamitsu.android.rpctest;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.msgpack.rpc.Client;
import org.msgpack.rpc.loop.EventLoop;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {
  public static interface RPCInterface {
    int add(int a, int b);
  }

  protected static final String TAG = MainActivity.class.getSimpleName();

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

    EventLoop loop = EventLoop.defaultEventLoop();
    Client client;
    try {
      client = new Client("192.168.0.6", 9090, loop);
    } catch (UnknownHostException e) {
      e.printStackTrace();
      return;
    }

    final RPCInterface rpcInterface = client.proxy(RPCInterface.class);

    ExecutorService executorService = Executors.newFixedThreadPool(30);
    final AtomicInteger count = new AtomicInteger();
    final int max = 10000;
    for (int i = 0; i < max; i++) {
      final int a = i;

      executorService.execute(new Runnable() {

        @Override
        public void run() {
          int b = (max - a) * 7;
          int expectedSum = a + b;
          int sum = rpcInterface.add(a, b);
          if (expectedSum != sum) {
            Log.w(TAG, "wrong result: a=" + a + ", b=" + b + ", result=" + sum);
          }
          int incrementedCount = count.incrementAndGet();
          if (incrementedCount % 500 == 0)
            Log.i(TAG, "count: " + incrementedCount);
        }
      });
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.activity_main, menu);
    return true;
  }

}
