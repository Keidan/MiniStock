package fr.ralala.ministock.mysql;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.ralala.ministock.MainApplication;
import fr.ralala.ministock.R;
import fr.ralala.ministock.models.ShoppingCartEntry;
import fr.ralala.ministock.ui.activities.MainActivity;
import fr.ralala.ministock.ui.utils.UIHelper;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * MySQL module.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class MySQL extends BroadcastReceiver {
  private static MySQL mInstance;
  private MainApplication mApp;
  private List<MySQLRequest> mList;
  private static int mId = 0;
  private MainActivity mActivity;
  private AlertDialog mAlertDialogError;

  private class MySQLRequest {
    int id;
    MySQLSuccessListener success;
    MySQLErrorListener error;

    MySQLRequest(int id, MySQLSuccessListener success, MySQLErrorListener error) {
      this.id = id;
      this.success = success;
      this.error = error;
    }
  }

  public interface MySQLSuccessListener {
    void onMySQLSuccess(MySQLAction action, Object data);
  }

  public interface MySQLErrorListener {
    void onMySQLError(MySQLAction action, int code, String description);
  }

  public static MySQL getInstance(MainApplication app) {
    if (mInstance == null) {
      mInstance = new MySQL(app);
    }
    return mInstance;
  }

  private MySQL(MainApplication app) {
    mApp = app;
    mList = new ArrayList<>();
  }

  /**
   * Defines the reference to the main activity.
   *
   * @param activity The main activity.
   */
  public void setActivity(MainActivity activity) {
    mActivity = activity;
  }

  /**
   * This function is called when the service sends a message.
   *
   * @param context The Android context.
   * @param intent  The broadcast intent.
   */
  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    if (action != null && action.equals(MainApplication.ACTION_STRING_S2A)) {
      final String name = MySQLBroadcastMessage.class.getSimpleName();
      if (intent.hasExtra(name)) {
        MySQLBroadcastMessage bm = (MySQLBroadcastMessage) intent.getSerializableExtra(name);
        switch (bm.getBroadcastType()) {
          case SHOW_PROGRESS:
            mActivity.runOnUiThread(() -> mActivity.progressShow());
            break;
          case READ:
            final String[] response = bm.getData();
            int requestId = Integer.parseInt(response[MySQLHelper.IDX_RESP_ID]);
            int code = Integer.parseInt(response[MySQLHelper.IDX_RESP_CODE]);
            String data = response[MySQLHelper.IDX_RESP_DATA];
            MySQLAction act = MySQLAction.fromString(response[MySQLHelper.IDX_RESP_ACTION]);
            for (int i = 0; i < mList.size(); i++) {
              MySQLRequest req = mList.get(i);
              if (req.id == requestId) {
                if (data == null)
                  mActivity.runOnUiThread(() -> req.error.onMySQLError(act, code, "HTTP(S) error"));
                else {
                  try {
                    JSONObject json = new JSONObject(data);
                    if (json.has("result")) {
                      String result = json.getString("result");
                      if (result.equals("success")) {
                        if (act == MySQLAction.LIST || act == MySQLAction.FIND) {
                          int idx = 0;
                          List<ShoppingCartEntry> list = new ArrayList<>();
                          while (json.has("item" + idx)) {
                            JSONObject obj = json.getJSONObject("item" + idx);
                            list.add(new ShoppingCartEntry(
                                obj.getString("id"),
                                obj.getString("title"),
                                obj.getString("image"),
                                obj.getInt("count"),
                                obj.getString("qrCodeId"),
                                obj.getString("creationDate"),
                                obj.getString("modificationDate")));
                            idx++;
                          }
                          mActivity.runOnUiThread(() -> req.success.onMySQLSuccess(act, list));
                        } else
                          mActivity.runOnUiThread(() -> req.success.onMySQLSuccess(act, null));
                      } else {
                        decodeError(req, act, json, code, data);
                      }
                    } else {
                      decodeError(req, act, json, code, data);
                    }
                  } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
                    mActivity.runOnUiThread(() -> req.error.onMySQLError(act, code, "Exception: " + e.getMessage()));
                  }
                }
                mList.remove(i);
              }
            }
            mActivity.runOnUiThread(() -> mActivity.progressHide());
            break;
          case SOCKET_ERROR: {
            mActivity.runOnUiThread(() ->
            {
              if (mAlertDialogError == null || !mAlertDialogError.isShowing()) {
                Throwable t = bm.getThrowable();
                String message = mActivity.getString(R.string.sql_error);
                message += "\n";
                message += t.getMessage();
                Log.e(getClass().getName(), "Exception: " + t.getMessage(), t);
                mAlertDialogError = UIHelper.showAlertDialog(mActivity, R.string.error, message);
                mActivity.progressHide();
              }
            });
            break;
          }
        }
      }
    }
  }

  /**
   * Decodes the error code.
   *
   * @param req  The MySQL request.
   * @param act  The associated action.
   * @param json The JSON response object.
   * @param code The HTTP code.
   * @param data The raw data.
   * @throws Exception If an error has occurred.
   */
  private void decodeError(MySQLRequest req, MySQLAction act, JSONObject json, int code, String data) throws Exception {
    int c;
    if (json.has("code"))
      c = json.getInt("code");
    else c = code;
    String desc;
    if (json.has("description"))
      desc = json.getString("description");
    else
      desc = data;
    mActivity.runOnUiThread(() -> req.error.onMySQLError(act, c, desc));
  }

  /**
   * Lists all entries in the database (asynchronous).
   *
   * @param context         The Android context.
   * @param listenerSuccess The listener called when the entry was deleted (data = List<ShoppingCartEntry>).
   * @param listenerError   The listener called when an error has occurred.
   */
  public void list(Context context, MySQLSuccessListener listenerSuccess, MySQLErrorListener listenerError) {
    //noinspection unchecked
    mList.add(new MySQLRequest(mId, listenerSuccess, listenerError));
    mApp.sendBroadcastFromActivityToService(
        new MySQLBroadcastMessage(MySQLBroadcastType.SEND, MySQLHelper.buildPostRequestForExecute(mId,
            mApp.getProtocol(), mApp.getHOST(), mApp.getPort(), mApp.getPage(), mApp.getUsername(), mApp.getPassword(), MySQLAction.LIST, null)));
    mId++;
  }

  /**
   * Search for an entry in the database (asynchronous).
   *
   * @param context         The Android context.
   * @param title           The title of the entry to be found.
   * @param listenerSuccess The listener called when the entry was deleted (data = List<ShoppingCartEntry>).
   * @param listenerError   The listener called when an error has occurred.
   */
  public void find(Context context, String title, MySQLSuccessListener listenerSuccess, MySQLErrorListener listenerError) {
    //noinspection unchecked
    mList.add(new MySQLRequest(mId, listenerSuccess, listenerError));
    mApp.sendBroadcastFromActivityToService(
        new MySQLBroadcastMessage(MySQLBroadcastType.SEND, MySQLHelper.buildPostRequestForExecute(mId,
            mApp.getProtocol(), mApp.getHOST(), mApp.getPort(), mApp.getPage(), mApp.getUsername(), mApp.getPassword(),
            MySQLAction.FIND, "\"title\": \"" + title + "\"")));
    mId++;
  }

  /**
   * Updates an entry in the database (asynchronously).
   *
   * @param context         The Android context.
   * @param entry           The entry to be updated.
   * @param listenerSuccess The listener called when the entry was deleted (data = null).
   * @param listenerError   The listener called when an error has occurred.
   */
  public void update(Context context, ShoppingCartEntry entry, MySQLSuccessListener listenerSuccess, MySQLErrorListener listenerError) {
    //noinspection unchecked
    mList.add(new MySQLRequest(mId, listenerSuccess, listenerError));
    mApp.sendBroadcastFromActivityToService(
        new MySQLBroadcastMessage(MySQLBroadcastType.SEND, MySQLHelper.buildPostRequestForExecute(mId,
            mApp.getProtocol(), mApp.getHOST(), mApp.getPort(), mApp.getPage(), mApp.getUsername(), mApp.getPassword(),
            MySQLAction.UPDATE_WITH_ID, entry.toJSON())));
    mId++;
  }

  /**
   * Inserts an entry in the database (asynchronously).
   *
   * @param context         The Android context.
   * @param entry           The entry to be inserted.
   * @param listenerSuccess The listener called when the entry was deleted (data = null).
   * @param listenerError   The listener called when an error has occurred.
   */
  public void insert(Context context, ShoppingCartEntry entry, MySQLSuccessListener listenerSuccess, MySQLErrorListener listenerError) {
    //noinspection unchecked
    mList.add(new MySQLRequest(mId, listenerSuccess, listenerError));
    mApp.sendBroadcastFromActivityToService(
        new MySQLBroadcastMessage(MySQLBroadcastType.SEND, MySQLHelper.buildPostRequestForExecute(mId,
            mApp.getProtocol(), mApp.getHOST(), mApp.getPort(), mApp.getPage(), mApp.getUsername(), mApp.getPassword(),
            MySQLAction.INSERT, entry.toJSON())));
    mId++;
  }

  /**
   * Deletes an entry from the database (asynchronously).
   *
   * @param context         The Android context.
   * @param entry           The entry to be deleted.
   * @param listenerSuccess The listener called when the entry was deleted (data = null).
   * @param listenerError   The listener called when an error has occurred.
   */
  public void delete(Context context, ShoppingCartEntry entry, MySQLSuccessListener listenerSuccess, MySQLErrorListener listenerError) {
    //noinspection unchecked
    mList.add(new MySQLRequest(mId, listenerSuccess, listenerError));
    mApp.sendBroadcastFromActivityToService(
        new MySQLBroadcastMessage(MySQLBroadcastType.SEND, MySQLHelper.buildPostRequestForExecute(mId,
            mApp.getProtocol(), mApp.getHOST(), mApp.getPort(), mApp.getPage(), mApp.getUsername(), mApp.getPassword(),
            MySQLAction.DELETE, entry.toJSON())));
    mId++;
  }
}
