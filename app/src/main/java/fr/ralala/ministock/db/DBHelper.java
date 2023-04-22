package fr.ralala.ministock.db;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * DB helper functions.
 * </p>
 *
 * @author Keidan
 * ******************************************************************************
 */
public class DBHelper {
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

  public static String[] buildPostRequestForExecute(DBItem item, final DBAction action) {
    String[] array = new String[IDX_REQ_DATA + 1];
    array[IDX_REQ_ID] = "" + item.getId();
    array[IDX_REQ_PROTOCOL] = item.getProtocol();
    array[IDX_REQ_HOST] = item.getHost();
    array[IDX_REQ_PORT] = "" + item.getPort();
    array[IDX_REQ_PAGE] = item.getPage();
    array[IDX_REQ_ACTION] = "" + action;
    array[IDX_REQ_DATA] = "{\"user\": \"" + item.getUser() + "\", \"pwd\": \"" + md5(item.getPwd()) +
      "\",\"action\": \"" + action + "\", \"table\": \"ministock\"";
    if (item.getData() != null) {
      array[IDX_REQ_DATA] += ", " + item.getData();
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
      hash = MessageDigest.getInstance("MD5").digest(s.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException e) {
      hash = new byte[32];
      Arrays.fill(hash, (byte) 0);
    }
    StringBuilder hex = new StringBuilder(hash.length * 2);
    for (byte b : hash) {
      hex.append(String.format("%02x", b & 0xFF));
    }
    return hex.toString();
  }
}
