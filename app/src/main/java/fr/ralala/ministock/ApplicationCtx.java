package fr.ralala.ministock;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import fr.ralala.ministock.db.DB;
import fr.ralala.ministock.db.DBBroadcastMessage;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Application context.
 * </p>
 *
 * @author Keidan
 * ******************************************************************************
 */
public class ApplicationCtx extends Application {
  public static final String ACTION_STRING_A2S = "A2S";
  public static final String ACTION_STRING_S2A = "S2A";
  public static final int PREF_DEF_CAMERA_TIMEOUT = 1;
  public static final int PREF_DEF_SCAN_TIMEOUT = 3;
  public static final String PREF_KEY_PROTOCOL = "protocol";
  public static final String PREF_KEY_USERNAME = "user";
  public static final String PREF_KEY_PASSWORD = "pwd";
  public static final String PREF_KEY_HOST = "host";
  public static final String PREF_KEY_PORT = "port";
  public static final String PREF_KEY_PAGE = "page";
  public static final String PREF_KEY_CAMERA_TIMEOUT = "cameraTimeout";
  public static final String PREF_KEY_SCAN_TIMEOUT = "scanTimeout";

  private DB mDB;
  private SharedPreferences mPrefs;

  @Override
  public void onCreate() {
    super.onCreate();
    mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    mDB = DB.getInstance(this);
  }

  /**
   * Sends a broadcast message to an activity.
   *
   * @param msg Message.
   */
  public void sendBroadcastFromServiceToActivity(DBBroadcastMessage msg) {
    sendBroadcast(ACTION_STRING_S2A, msg);
  }

  /**
   * Sends a broadcast message to a service.
   *
   * @param msg Message.
   */
  public void sendBroadcastFromActivityToService(DBBroadcastMessage msg) {
    sendBroadcast(ACTION_STRING_A2S, msg);
  }

  /**
   * Sends a broadcast message.
   *
   * @param action Message action.
   * @param msg    Message.
   */
  private void sendBroadcast(String action, DBBroadcastMessage msg) {
    Intent intent = new Intent();
    intent.setAction(action);
    intent.putExtra(DBBroadcastMessage.class.getSimpleName(), msg);
    this.sendBroadcast(intent);
  }

  public static void registerServiceBroadcast(Service srv, BroadcastReceiver receiver) {
    IntentFilter intentFilter = new IntentFilter(ACTION_STRING_A2S);
    srv.registerReceiver(receiver, intentFilter);
  }

  public static void registerActivityBroadcast(Activity act, BroadcastReceiver receiver) {
    IntentFilter intentFilter = new IntentFilter(ACTION_STRING_S2A);
    act.registerReceiver(receiver, intentFilter);
  }

  /**
   * Returns the SharedPreferences.
   *
   * @return SharedPreferences
   */
  public SharedPreferences getSharedPreferences() {
    return mPrefs;
  }

  /**
   * Returns the DB instance.
   *
   * @return DB
   */
  public DB getDb() {
    return mDB;
  }

  /**
   * Tests whether https sockets is enabled.
   *
   * @return String
   */
  public String getProtocol() {
    return mPrefs.getString(PREF_KEY_PROTOCOL, getString(R.string.default_protocol).toUpperCase(Locale.getDefault()));
  }

  /**
   * Returns the username.
   *
   * @return String
   */
  public String getUsername() {
    return mPrefs.getString(PREF_KEY_USERNAME, getString(R.string.default_username));
  }

  /**
   * Returns the password.
   *
   * @return String
   */
  public String getPassword() {
    return mPrefs.getString(PREF_KEY_PASSWORD, getString(R.string.default_password));
  }

  /**
   * Returns the host address.
   *
   * @return String
   */
  public String getHOST() {
    return mPrefs.getString(PREF_KEY_HOST, getString(R.string.default_host));
  }

  /**
   * Returns the host port.
   *
   * @return int
   */
  public int getPort() {
    try {
      return mPrefs.getInt(PREF_KEY_PORT, Integer.parseInt(getString(R.string.default_port)));
    } catch (NumberFormatException nfe) {
      return 80;
    }
  }

  /**
   * Returns the host http(s) page.
   *
   * @return String
   */
  public String getPage() {
    return mPrefs.getString(PREF_KEY_PAGE, getString(R.string.default_page));
  }

  /**
   * Returns the index value of the getCameraTimeout.
   *
   * @return The index
   */
  public int getCameraTimeoutIndex() {
    return mPrefs.getInt(PREF_KEY_CAMERA_TIMEOUT, PREF_DEF_CAMERA_TIMEOUT);
  }

  /**
   * Returns the inactivity delay for the camera.
   *
   * @return The delay in milliseconds.
   */
  public long getCameraTimeout() {
    switch (getCameraTimeoutIndex()) {
      case 1: /* 1 minute*/
        return TimeUnit.MINUTES.toMillis(1);
      case 2: /* 5 minutes*/
        return TimeUnit.MINUTES.toMillis(5);
      case 3: /* 10 minutes*/
        return TimeUnit.MINUTES.toMillis(10);
      case 4: /* 30 minutes*/
        return TimeUnit.MINUTES.toMillis(30);
      case 5: /* 1 hour*/
        return TimeUnit.HOURS.toMillis(1);
      default:
        return 0L;
    }
  }


  /**
   * Returns the index value of the getCameraTimeout.
   *
   * @return The index
   */
  public int getScanTimeoutIndex() {
    return mPrefs.getInt(PREF_KEY_SCAN_TIMEOUT, PREF_DEF_SCAN_TIMEOUT);
  }

  /**
   * Returns the inactivity delay for the scans.
   *
   * @return The delay in milliseconds.
   */
  public long getScanTimeout() {
    return TimeUnit.SECONDS.toMillis(getScanTimeoutIndex());
  }
}
