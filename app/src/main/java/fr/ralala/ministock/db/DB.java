package fr.ralala.ministock.db;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.ralala.ministock.ApplicationCtx;
import fr.ralala.ministock.R;
import fr.ralala.ministock.db.models.CartEntry;
import fr.ralala.ministock.ui.activities.MainActivity;
import fr.ralala.ministock.ui.utils.UIHelper;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * DB module.
 * </p>
 *
 * @author Keidan
 * ******************************************************************************
 */
public class DB extends BroadcastReceiver {
  private static final String EXCEPTION = "Exception: ";
  private static DB mInstance;
  private final ApplicationCtx mApp;
  private final List<DBRequest> mList;
  private int mId = 0;
  private MainActivity mActivity;
  private AlertDialog mAlertDialogError;

  private static class DBRequest {
    int id;
    DBSuccessListener success;
    DBErrorListener error;

    DBRequest(int id, DBSuccessListener success, DBErrorListener error) {
      this.id = id;
      this.success = success;
      this.error = error;
    }
  }

  public interface DBSuccessListener {
    void onDBSuccess(DBAction action, Object data);
  }

  public interface DBErrorListener {
    void onDBError(DBAction action, int code, String description);
  }

  public static DB getInstance(ApplicationCtx app) {
    if (mInstance == null) {
      mInstance = new DB(app);
    }
    return mInstance;
  }

  private DB(ApplicationCtx app) {
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
    if (action != null && action.equals(ApplicationCtx.ACTION_STRING_S2A)) {
      final String name = DBBroadcastMessage.class.getSimpleName();
      if (intent.hasExtra(name)) {
        DBBroadcastMessage bm = (DBBroadcastMessage) intent.getSerializableExtra(name);
        if (bm.getBroadcastType() == DBBroadcastType.SHOW_PROGRESS)
          mActivity.runOnUiThread(mActivity::progressShow);
        else if (bm.getBroadcastType() == DBBroadcastType.READ)
          processRead(bm);
        else if (bm.getBroadcastType() == DBBroadcastType.SOCKET_ERROR)
          processSocketError(bm);
      }
    }
  }

  /**
   * Manages read.
   *
   * @param bm DBBroadcastMessage
   */
  private void processRead(DBBroadcastMessage bm) {
    final String[] response = bm.getData();
    int requestId = Integer.parseInt(response[DBHelper.IDX_RESP_ID]);
    int code = Integer.parseInt(response[DBHelper.IDX_RESP_CODE]);
    String data = response[DBHelper.IDX_RESP_DATA];
    DBAction act = DBAction.fromString(response[DBHelper.IDX_RESP_ACTION]);
    List<DBRequest> list = new ArrayList<>(mList);
    for (DBRequest req : list) {
      if (req != null && req.id == requestId) {
        if (data == null)
          mActivity.runOnUiThread(() -> req.error.onDBError(act, code, "HTTP(S) error"));
        else {
          processJSON(code, data, req, act);
        }
        mList.add(req);
      }
    }
    mActivity.runOnUiThread(mActivity::progressHide);
  }

  /**
   * Manages the JSON decoding.
   *
   * @param code The HTTP code.
   * @param data The raw data.
   * @param req  The DB request.
   * @param act  The associated action.
   */
  private void processJSON(int code, String data, DBRequest req, DBAction act) {
    try {
      JSONObject json = new JSONObject(data);
      if (json.has("result")) {
        String result = json.getString("result");
        if (result.equals("success")) {
          if (act == DBAction.LIST || act == DBAction.FIND) {
            int idx = 0;
            List<CartEntry> list = new ArrayList<>();
            while (json.has("item" + idx)) {
              JSONObject obj = json.getJSONObject("item" + idx);
              list.add(new CartEntry(obj));
              idx++;
            }
            mActivity.runOnUiThread(() -> req.success.onDBSuccess(act, list));
          } else
            mActivity.runOnUiThread(() -> req.success.onDBSuccess(act, null));
        } else {
          decodeError(req, act, json, code, data);
        }
      } else {
        decodeError(req, act, json, code, data);
      }
    } catch (Exception e) {
      Log.e(getClass().getSimpleName(), EXCEPTION + e.getMessage(), e);
      mActivity.runOnUiThread(() -> req.error.onDBError(act, code, EXCEPTION + e.getMessage()));
    }
  }

  /**
   * Manages socket error.
   *
   * @param bm DBBroadcastMessage
   */
  private void processSocketError(DBBroadcastMessage bm) {
    mActivity.runOnUiThread(() ->
    {
      if (mAlertDialogError == null || !mAlertDialogError.isShowing()) {
        Throwable t = bm.getThrowable();
        String message = mActivity.getString(R.string.db_error);
        message += "\n";
        message += t.getMessage();
        Log.e(getClass().getName(), EXCEPTION + t.getMessage(), t);
        mAlertDialogError = UIHelper.showAlertDialog(mActivity, R.string.error, message);
        mActivity.progressHide();
      }
    });
  }

  /**
   * Decodes the error code.
   *
   * @param req  The DB request.
   * @param act  The associated action.
   * @param json The JSON response object.
   * @param code The HTTP code.
   * @param data The raw data.
   * @throws Exception If an error has occurred.
   */
  private void decodeError(DBRequest req, DBAction act, JSONObject json, int code, String data) throws Exception {
    int c;
    if (json.has("code"))
      c = json.getInt("code");
    else c = code;
    String desc;
    if (json.has("description"))
      desc = json.getString("description");
    else
      desc = data;
    mActivity.runOnUiThread(() -> req.error.onDBError(act, c, desc));
  }

  DBItem getItem(String data) {
    DBItem item = new DBItem();
    item.setId(mId);
    item.setProtocol(mApp.getProtocol());
    item.setHost(mApp.getHOST());
    item.setPort(mApp.getPort());
    item.setPage(mApp.getPage());
    item.setUser(mApp.getUsername());
    item.setPwd(mApp.getPassword());
    item.setData(data);
    return item;
  }

  /**
   * Lists all entries in the database (asynchronous).
   *
   * @param context         The Android context.
   * @param listenerSuccess The listener called when the entry was deleted (data = List<ShoppingCartEntry>).
   * @param listenerError   The listener called when an error has occurred.
   */
  public void list(Context context, DBSuccessListener listenerSuccess, DBErrorListener listenerError) {
    //noinspection unchecked
    mList.add(new DBRequest(mId, listenerSuccess, listenerError));
    mApp.sendBroadcastFromActivityToService(
      new DBBroadcastMessage(DBBroadcastType.SEND, DBHelper.buildPostRequestForExecute(getItem(null), DBAction.LIST)));
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
  public void find(Context context, String title, DBSuccessListener listenerSuccess, DBErrorListener listenerError) {
    //noinspection unchecked
    mList.add(new DBRequest(mId, listenerSuccess, listenerError));
    mApp.sendBroadcastFromActivityToService(
      new DBBroadcastMessage(DBBroadcastType.SEND, DBHelper.buildPostRequestForExecute(getItem("\"title\": \"" + title + "\""),
        DBAction.FIND)));
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
  public void update(Context context, CartEntry entry, DBSuccessListener listenerSuccess, DBErrorListener listenerError) {
    //noinspection unchecked
    mList.add(new DBRequest(mId, listenerSuccess, listenerError));
    mApp.sendBroadcastFromActivityToService(
      new DBBroadcastMessage(DBBroadcastType.SEND, DBHelper.buildPostRequestForExecute(getItem(entry.toJSON()),
        DBAction.UPDATE_WITH_ID)));
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
  public void insert(Context context, CartEntry entry, DBSuccessListener listenerSuccess, DBErrorListener listenerError) {
    //noinspection unchecked
    mList.add(new DBRequest(mId, listenerSuccess, listenerError));
    mApp.sendBroadcastFromActivityToService(
      new DBBroadcastMessage(DBBroadcastType.SEND, DBHelper.buildPostRequestForExecute(getItem(entry.toJSON()),
        DBAction.INSERT)));
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
  public void delete(Context context, CartEntry entry, DBSuccessListener listenerSuccess, DBErrorListener listenerError) {
    //noinspection unchecked
    mList.add(new DBRequest(mId, listenerSuccess, listenerError));
    mApp.sendBroadcastFromActivityToService(
      new DBBroadcastMessage(DBBroadcastType.SEND, DBHelper.buildPostRequestForExecute(getItem(entry.toJSON()),
        DBAction.DELETE)));
    mId++;
  }
}
