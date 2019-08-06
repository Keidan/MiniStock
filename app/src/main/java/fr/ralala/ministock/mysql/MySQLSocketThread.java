package fr.ralala.ministock.mysql;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import fr.ralala.ministock.MainApplication;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Service thread.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class MySQLSocketThread extends Thread {
  public static final String CANCEL_MSG = "Task cancelled";
  private static final String METHOD_POST = "POST";
  private static final int READ_TIMEOUT = 95000;
  private static final int CONNECT_TIMEOUT = 95000;
  private static final int CONNECTION_TIMEOUT = 3000;
  private static final int ERROR_HOST = 503;
  private static final int ERROR_SERVER = 500;
  private static final int ERROR_NOT_FOUND = 404;
  private BlockingQueue<MySQLBroadcastMessage> mQueue;
  private MainApplication mApp;

  MySQLSocketThread(BlockingQueue<MySQLBroadcastMessage> queue, MainApplication app) {
    mQueue = queue;
    mApp = app;
  }


  void kill() {
    mQueue.clear();
    try {
      mQueue.put(new MySQLBroadcastMessage(MySQLBroadcastType.EXIT));
    } catch (Exception e) {
      mApp.sendBroadcastFromServiceToActivity(
          new MySQLBroadcastMessage(e));
    }
    while (isAlive()) {
      try {
        Thread.sleep(10);
      } catch (Exception e) {
        Log.e(getClass().getSimpleName(), "Kill exception: " + e.getMessage(), e);
        return;
      }
    }
    Log.e(getClass().getSimpleName(), "Socket thread killed");
  }

  public void run() {
    MySQLBroadcastMessage bm;
    boolean aborted = false;
    while (true) {

      try {
        bm = mQueue.take();
      } catch (Exception e) {
        mApp.sendBroadcastFromServiceToActivity(
            new MySQLBroadcastMessage(e));
        return;
      }
      switch (bm.getBroadcastType()) {
        case EXIT:
          Log.i(getClass().getSimpleName(), "Exit message received.");
          return;
        case SEND:
          mApp.sendBroadcastFromServiceToActivity(
              new MySQLBroadcastMessage(MySQLBroadcastType.SHOW_PROGRESS));
          URL url;
          HttpURLConnection connection = null;
          String[] request = bm.getData();
          String[] response = new String[MySQLHelper.IDX_RESP_DATA + 1];

          response[MySQLHelper.IDX_RESP_ID] = request[MySQLHelper.IDX_REQ_ID];
          response[MySQLHelper.IDX_RESP_ACTION] = request[MySQLHelper.IDX_REQ_ACTION];

          MySQLAction action = MySQLAction.fromString(request[MySQLHelper.IDX_REQ_ACTION]);
          try {
            String host = request[MySQLHelper.IDX_REQ_HOST];
            int port = Integer.parseInt(request[MySQLHelper.IDX_REQ_PORT]);
            if (request[MySQLHelper.IDX_REQ_PROTOCOL].compareToIgnoreCase("https") == 0)
              connection = getHttpsConnection(host, port, request[MySQLHelper.IDX_REQ_PAGE]);
            else
              connection = getHttpConnection(host, port, request[MySQLHelper.IDX_REQ_PAGE]);
            //connection.connect();
            //Send request ...
            try(OutputStream os = connection.getOutputStream()) {
              byte[] input = request[MySQLHelper.IDX_REQ_DATA].getBytes("utf-8");
              os.write(input, 0, input.length);
            }
            response[MySQLHelper.IDX_RESP_CODE] = "" + connection.getResponseCode();

            try(BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))){
              StringBuilder resp = new StringBuilder();
              String responseLine;
              while ((responseLine = br.readLine()) != null) {
                resp.append(responseLine.trim());
              }
              response[MySQLHelper.IDX_RESP_DATA] = resp.toString();
            }
          } catch (Exception e) {
            String message = e.getMessage();
            Log.e(getClass().getSimpleName(), "Exception: " + message, e);
            if (FileNotFoundException.class.isInstance(e))
              response[MySQLHelper.IDX_RESP_CODE] = "" + ERROR_NOT_FOUND;
            else if (message.startsWith("Unable to resolve host"))
              response[MySQLHelper.IDX_RESP_CODE] = "" + ERROR_HOST;
            else
              response[MySQLHelper.IDX_RESP_CODE] = "" + ERROR_SERVER;
            response[MySQLHelper.IDX_RESP_DATA] = "{\"result\": \"error\", \"description\": \"" + e.getMessage() + "\"}";
          } finally {
            if (connection != null) {
              connection.disconnect();
            }
          }
          mApp.sendBroadcastFromServiceToActivity(
              new MySQLBroadcastMessage(MySQLBroadcastType.READ, response));
          break;
        default:
          Log.i(getClass().getSimpleName(), "Unsupported message received (" + bm.getBroadcastType() + ").");
          break;
      }
    }
  }

  private void fillDefaultConnection(HttpURLConnection connection, String method) throws ProtocolException {
    connection.setRequestMethod(method);
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
    fillDefaultConnection(connection, METHOD_POST);
    StringBuilder sbHost = new StringBuilder();
    if (host.startsWith("www."))
      sbHost.append(host.substring("www.".length()));
    else
      sbHost.append(host);
    connection.setHostnameVerifier((hostname, session) ->
        HttpsURLConnection.getDefaultHostnameVerifier().verify(sbHost.toString(), session));
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
    fillDefaultConnection(connection, METHOD_POST);
    return connection;
  }
}
