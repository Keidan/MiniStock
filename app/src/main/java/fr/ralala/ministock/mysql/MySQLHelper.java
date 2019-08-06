package fr.ralala.ministock.mysql;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * MySQL helper functions.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class MySQLHelper {
  public static final int IDX_REQ_ID = 0;
  public static final int IDX_REQ_PROTOCOL = 1;
  public static final int IDX_REQ_HOST = 2;
  public static final int IDX_REQ_PORT = 3;
  public static final int IDX_REQ_PAGE = 4;
  public static final int IDX_REQ_ACTION = 5;
  public static final int IDX_REQ_DATA = 6;
  public static final int IDX_RESP_ID = 0;
  public static final int IDX_RESP_ACTION = 1;
  public static final int IDX_RESP_CODE = 2;
  public static final int IDX_RESP_DATA = 3;

  public enum ActionJSON {
    LIST,
    UPDATE,
    INSERT,
    DELETE
  }

  public static String[] buildPostRequestForExecute(final int id, final String protocol, final String host, final int port, final String page, final String user, final String pwd, final MySQLAction action, String data) {
    String[] array = new String[IDX_REQ_DATA + 1];
    array[IDX_REQ_ID] = "" + id;
    array[IDX_REQ_PROTOCOL] = protocol;
    array[IDX_REQ_HOST] = host;
    array[IDX_REQ_PORT] = "" + port;
    array[IDX_REQ_PAGE] = page;
    array[IDX_REQ_ACTION] = "" + action;
    array[IDX_REQ_DATA] = "{\"user\": \"" + user + "\", \"pwd\": \"" + md5(pwd) + "\",\"action\": \"" + action + "\", \"table\": \"ministock\"";
    if (data != null) {
      array[IDX_REQ_DATA] += ", " + data;
    }
    array[IDX_REQ_DATA] += "}";
    return array;
  }

  /**
   * Evaluates the MD5 sum of a string (php like).
   *
   * @param s The string to be evaluated.
   * @return The MD5 sum.
   */
  private static String md5(final String s) {
    byte[] hash;
    try {
      hash = MessageDigest.getInstance("MD5").digest(s.getBytes("UTF-8"));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Huh, MD5 should be supported?", e);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Huh, UTF-8 should be supported?", e);
    }
    StringBuilder hex = new StringBuilder(hash.length * 2);
    for (byte b : hash) {
      if ((b & 0xFF) < 0x10) {
        hex.append("0");
      }
      hex.append(Integer.toHexString(b & 0xFF));
    }
    return hex.toString();
  }
}
