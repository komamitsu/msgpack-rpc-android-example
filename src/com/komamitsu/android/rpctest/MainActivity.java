package com.komamitsu.android.rpctest;

import java.net.UnknownHostException;

import org.msgpack.rpc.Client;
import org.msgpack.rpc.loop.EventLoop;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
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

    /*
    Logger logger = LoggerFactory.getLogger(this.getClass());
    logger.info("hogehoge");
    */
    new AsyncTask<Void, Void, Integer>() {
      Exception e;

      @Override
      protected Integer doInBackground(Void... params) {
        EventLoop loop = EventLoop.defaultEventLoop();
        try {
          Client client = new Client("192.168.0.6", 9090, loop);
          RPCInterface rpcInterface = client.proxy(RPCInterface.class);
          return rpcInterface.add(123, 456);
        } catch (UnknownHostException e) {
          this.e = e;
          return null;
        }
      }

      @Override
      protected void onPostExecute(Integer result) {
        String msg = (e == null) ?
            ("result => " + result) : ("error => " + e.getMessage());
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
      }
    }.execute();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.activity_main, menu);
    return true;
  }

}
