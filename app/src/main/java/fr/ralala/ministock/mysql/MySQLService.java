package fr.ralala.ministock.mysql;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import fr.ralala.ministock.MainApplication;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * MySQL (HTTP(S)) service.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class MySQLService extends Service {
  private static final int BLOCKING_QUEUE_CAPACITY = 100;
  private MySQLSocketThread mSocketThread = null;
  private BlockingQueue<MySQLBroadcastMessage> mQueue;

  private BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (action != null && action.equals(MainApplication.ACTION_STRING_A2S)) {
        final String name = MySQLBroadcastMessage.class.getSimpleName();
        if (intent.hasExtra(name)) {
          MySQLBroadcastMessage bm = (MySQLBroadcastMessage) intent.getSerializableExtra(name);
          switch (bm.getBroadcastType()) {
            case SEND:
              try {
                mQueue.put(bm);
              } catch (Exception e) {
                ((MainApplication) getApplication()).sendBroadcastFromServiceToActivity(
                    new MySQLBroadcastMessage(e));
              }
              break;
          }
        }
      }
    }
  };

  /**
   * Called when the service is created.
   */
  @Override
  public void onCreate() {
    super.onCreate();
    Log.i(getClass().getSimpleName(), "Service created");
    mQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
    MainApplication.registerServiceBroadcast(this, mServiceReceiver);
    mSocketThread = new MySQLSocketThread(mQueue, (MainApplication) getApplication());
    mSocketThread.start();
  }

  /**
   * Called when the service is destroyed.
   */
  @Override
  public void onDestroy() {
    Log.i(getClass().getSimpleName(), "Service destroyed");
    if (mSocketThread != null)
      mSocketThread.kill();
    mSocketThread = null;
    unregisterReceiver(mServiceReceiver);
    super.onDestroy();
  }

  /**
   * Called when the service is binded.
   *
   * @param intent Not used.
   * @return null
   */
  @Override
  public IBinder onBind(final Intent intent) {
    return null;
  }
}
