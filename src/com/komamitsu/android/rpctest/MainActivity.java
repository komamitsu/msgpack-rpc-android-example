package com.komamitsu.android.rpctest;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.msgpack.rpc.Client;
import org.msgpack.rpc.Server;
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

  private static final int PORT = 9090;
  private static final String HOST = "127.0.0.1";
  private RpcServerRunner serverRunner;
  private RpcClientRunner clientRunner;

  private static class RpcClientRunner {
    protected ExecutorService executor;

    public void start() throws UnknownHostException {
      EventLoop loop = EventLoop.defaultEventLoop();

      Client client;
      client = new Client(HOST, PORT, loop);

      final RPCInterface rpcInterface = client.proxy(RPCInterface.class);

      executor = Executors.newFixedThreadPool(5);
      final AtomicInteger count = new AtomicInteger();

      final int max = 50000;
      for (int i = 0; i < max; i++) {
        final int a = i;

        executor.execute(new Runnable() {

          @Override
          public void run() {
            int b = (max - a) * 7;
            int expectedSum = a + b;
            int sum = rpcInterface.add(a, b);
            if (expectedSum != sum) {
              Log.w(TAG, "wrong result: a=" + a + ", b=" + b + ", result=" + sum);
            }
            int incrementedCount = count.incrementAndGet();
            if (incrementedCount % 500 == 0) {
              Log.i(TAG, "count: " + incrementedCount);
            }
          }
        });
      }
    }

    public void stop() {
      if (executor != null)
        executor.shutdownNow();
    }
  }

  private static class RpcServer {
    @SuppressWarnings("unused")
    public int add(int a, int b) {
      // Log.d(TAG, String.format("add: a=%d, b=%d", a, b));
      return a + b;
    }
  }

  private static class RpcServerRunner {
    private final Server server = new Server();

    public void start() throws UnknownHostException, IOException {
      final EventLoop eventLoop = EventLoop.defaultEventLoop();
      server.serve(new RpcServer());
      server.listen(HOST, PORT);
      Executors.newSingleThreadExecutor().execute(new Runnable() {

        @Override
        public void run() {
          try {
            eventLoop.join();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      });
    }

    public void stop() {
      if (server != null)
        server.close();
    }
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
    try {
      serverRunner = new RpcServerRunner();
      serverRunner.start();

      clientRunner = new RpcClientRunner();
      clientRunner.start();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();

    if (serverRunner != null)
      serverRunner.stop();

    if (clientRunner != null)
      clientRunner.stop();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.activity_main, menu);
    return true;
  }

}
