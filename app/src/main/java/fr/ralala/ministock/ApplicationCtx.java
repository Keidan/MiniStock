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

import fr.ralala.ministock.db.DB;
import fr.ralala.ministock.db.DBBroadcastMessage;
import fr.ralala.ministock.models.SettingsKeys;

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
  private DB mDB;
  private SharedPreferences mPrefs;
  private String mItemsData = null;

  @Override
  public void onCreate() {
    super.onCreate();
    mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    mDB = DB.getInstance(this);
  }

  public void setItemsData(final String itemsData) {
    mItemsData = itemsData;
  }
  public String getItemsData() {
    return mItemsData;
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
    return mPrefs.getString(SettingsKeys.CFG_PROTOCOL, getString(R.string.default_protocol).toUpperCase(Locale.getDefault()));
  }

  /**
   * Returns the username.
   *
   * @return String
   */
  public String getUsername() {
    return mPrefs.getString(SettingsKeys.CFG_USERNAME, getString(R.string.default_username));
  }

  /**
   * Returns the password.
   *
   * @return String
   */
  public String getPassword() {
    return mPrefs.getString(SettingsKeys.CFG_PASSWORD, getString(R.string.default_password));
  }

  /**
   * Returns the host address.
   *
   * @return String
   */
  public String getHOST() {
    return mPrefs.getString(SettingsKeys.CFG_HOST, getString(R.string.default_host));
  }

  /**
   * Returns the host port.
   *
   * @return int
   */
  public int getPort() {
    try {
      return Integer.parseInt(mPrefs.getString(SettingsKeys.CFG_PORT, getString(R.string.default_port)));
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
    return mPrefs.getString(SettingsKeys.CFG_PAGE, getString(R.string.default_page));
  }

  /**
   * Checks if an update is available when the application starts
   *
   * @return boolean
   */
  public boolean isCheckUpdateStartup() {
    return mPrefs.getBoolean(SettingsKeys.CFG_CHECK_UPDATE_ON_START, Boolean.parseBoolean(getString(R.string.default_check_update_startup)));
  }
}
