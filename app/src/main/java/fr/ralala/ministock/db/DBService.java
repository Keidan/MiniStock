package fr.ralala.ministock.db;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import fr.ralala.ministock.ApplicationCtx;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * DB (HTTP(S)) service.
 * </p>
 *
 * @author Keidan
 * ******************************************************************************
 */
public class DBService extends Service {
  private static final int BLOCKING_QUEUE_CAPACITY = 100;
  private DBSocketThread mSocketThread = null;
  private BlockingQueue<DBBroadcastMessage> mQueue;

  private final BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (action != null && action.equals(ApplicationCtx.ACTION_STRING_A2S)) {
        final String name = DBBroadcastMessage.class.getSimpleName();
        if (intent.hasExtra(name)) {
          DBBroadcastMessage bm;
          if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
            bm = intent.getSerializableExtra(name, DBBroadcastMessage.class);
          else
            bm = (DBBroadcastMessage)intent.getSerializableExtra(name);
          if (bm.getBroadcastType() == DBBroadcastType.SEND) {
            try {
              mQueue.put(bm);
            } catch (Exception e) {
              ((ApplicationCtx) getApplication()).sendBroadcastFromServiceToActivity(
                new DBBroadcastMessage(e));
              Thread.currentThread().interrupt();
            }
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
    ApplicationCtx.registerServiceBroadcast(this, mServiceReceiver);
    mSocketThread = new DBSocketThread(mQueue, (ApplicationCtx) getApplication());
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
