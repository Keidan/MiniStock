package fr.ralala.ministock.db;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import fr.ralala.ministock.ApplicationCtx;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Service thread.
 * </p>
 *
 * @author Keidan
 * ******************************************************************************
 */
public class DBSocketThread extends Thread {
  public static final String CANCEL_MSG = "Task cancelled";
  private static final String METHOD_POST = "POST";
  private static final int READ_TIMEOUT = 95000;
  private static final int CONNECT_TIMEOUT = 95000;
  private static final int ERROR_HOST = 503;
  private static final int ERROR_SERVER = 500;
  private static final int ERROR_NOT_FOUND = 404;
  private final BlockingQueue<DBBroadcastMessage> mQueue;
  private final ApplicationCtx mApp;

  DBSocketThread(BlockingQueue<DBBroadcastMessage> queue, ApplicationCtx app) {
    mQueue = queue;
    mApp = app;
  }


  void kill() {
    mQueue.clear();
    try {
      mQueue.put(new DBBroadcastMessage(DBBroadcastType.EXIT));
    } catch (InterruptedException e) {
      mApp.sendBroadcastFromServiceToActivity(
        new DBBroadcastMessage(e));
      Thread.currentThread().interrupt();
    }
    while (isAlive()) {
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        Log.e(getClass().getSimpleName(), "Kill exception: " + e.getMessage(), e);
        Thread.currentThread().interrupt();
        return;
      }
    }
    Log.e(getClass().getSimpleName(), "Socket thread killed");
  }

  @Override
  public void run() {
    DBBroadcastMessage bm;

    while (true) {

      try {
        bm = mQueue.take();
      } catch (InterruptedException e) {
        mApp.sendBroadcastFromServiceToActivity(
          new DBBroadcastMessage(e));
        Thread.currentThread().interrupt();
        return;
      }
      switch (bm.getBroadcastType()) {
        case EXIT:
          Log.i(getClass().getSimpleName(), "Exit message received.");
          return;
        case SEND:
          processSend(bm);
          break;
        default:
          Log.i(getClass().getSimpleName(), "Unsupported message received (" + bm.getBroadcastType() + ").");
          break;
      }
    }
  }

  private void processSend(DBBroadcastMessage bm) {
    HttpURLConnection connection = null;
    String[] request = bm.getData();
    String[] response = new String[DBHelper.IDX_RESP_DATA + 1];

    response[DBHelper.IDX_RESP_ID] = request[DBHelper.IDX_REQ_ID];
    response[DBHelper.IDX_RESP_ACTION] = request[DBHelper.IDX_REQ_ACTION];

    try {
      String host = request[DBHelper.IDX_REQ_HOST];
      int port = Integer.parseInt(request[DBHelper.IDX_REQ_PORT]);
      if (request[DBHelper.IDX_REQ_PROTOCOL].compareToIgnoreCase("https") == 0)
        connection = getHttpsConnection(host, port, request[DBHelper.IDX_REQ_PAGE]);
      else
        connection = getHttpConnection(host, port, request[DBHelper.IDX_REQ_PAGE]);
      //Send request ...
      try (OutputStream os = connection.getOutputStream()) {
        byte[] input = request[DBHelper.IDX_REQ_DATA].getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
      }
      response[DBHelper.IDX_RESP_CODE] = "" + connection.getResponseCode();

      try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
        StringBuilder resp = new StringBuilder();
        String responseLine;
        while ((responseLine = br.readLine()) != null) {
          resp.append(responseLine.trim());
        }
        response[DBHelper.IDX_RESP_DATA] = resp.toString();
      }
    } catch (Exception e) {
      String message = e.getMessage();
      Log.e(getClass().getSimpleName(), "Exception: " + message, e);
      if (e instanceof FileNotFoundException)
        response[DBHelper.IDX_RESP_CODE] = "" + ERROR_NOT_FOUND;
      else if (message != null && message.startsWith("Unable to resolve host"))
        response[DBHelper.IDX_RESP_CODE] = "" + ERROR_HOST;
      else
        response[DBHelper.IDX_RESP_CODE] = "" + ERROR_SERVER;
      response[DBHelper.IDX_RESP_DATA] = "{\"result\": \"error\", \"description\": \"" + e.getMessage() + "\"}";
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
    if(response[DBHelper.IDX_RESP_CODE].equals("200") &&
      DBAction.fromString(response[DBHelper.IDX_RESP_ACTION]) == DBAction.LIST) {
      mApp.setItemsData(response[DBHelper.IDX_RESP_DATA]);
      response[DBHelper.IDX_RESP_DATA] = null;
    }
    mApp.sendBroadcastFromServiceToActivity(
      new DBBroadcastMessage(DBBroadcastType.READ, response));
  }

  private void fillDefaultConnection(HttpURLConnection connection) throws ProtocolException {
    connection.setRequestMethod(DBSocketThread.METHOD_POST);
    connection.setReadTimeout(READ_TIMEOUT);
    connection.setConnectTimeout(CONNECT_TIMEOUT);
    connection.setRequestProperty("Accept", "application/json");
    connection.setRequestProperty("Content-Type", "application/json; utf-8");
    connection.setRequestProperty("X-Environment", "android");
    connection.setDoOutput(true);
  }

  /**
   * Gets the HttpsURLConnection.
   *
   * @param host The remote host address.
   * @param port The remote port number.
   * @param page The remote page.
   * @return HttpsURLConnection
   * @throws IOException If an exception occurs.
   */
  private HttpURLConnection getHttpsConnection(String host, int port, String page) throws IOException {
    URL url = new URL("https://" + host + ":" + port + page);
    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
    fillDefaultConnection(connection);
    connection.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
    return connection;
  }

  /**
   * Gets the HttpURLConnection.
   *
   * @param host The remote host address.
   * @param port The remote port number.
   * @param page The remote page.
   * @return HttpURLConnection
   * @throws IOException If an exception occurs.
   */
  private HttpURLConnection getHttpConnection(String host, int port, String page) throws IOException {
    URL url = new URL("http://" + host + ":" + port + page);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    fillDefaultConnection(connection);
    return connection;
  }
}
