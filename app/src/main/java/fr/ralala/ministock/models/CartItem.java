package fr.ralala.ministock.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * cart item
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class CartItem implements Serializable {
  private static final String VERSION = "1.2";
  private final String mVersion;
  private String mDate;


  /**
   * Called from the bdd.
   *
   * @param json The json object.
   */
  public CartItem(JSONObject json) throws JSONException {
    mVersion = json.getString("version");
    mDate = json.getString("date").replace("\\", "/");
  }

  /**
   * Called from the bdd.
   *
   * @param date The date.
   */
  public CartItem(String date) {
    mVersion = VERSION;
    mDate = (date == null) ? getDateNow() : date;
  }

  /**
   * Returns the version.
   *
   * @return String
   */
  public String getVersion() {
    return mVersion;
  }

  /**
   * Returns the object in JSON format (without braces).
   *
   * @return String
   */
  public String toJSON() {
    return "{\"version\": \"" + mVersion + "\", \"date\": \"" + mDate + "\"}";
  }

  public static String getDateNow() {
    return getSimpleDateFormat().format(new Date());
  }

  public static SimpleDateFormat getSimpleDateFormat() {
    return new SimpleDateFormat("yyyy/MM/dd", Locale.US);
  }

  /**
   * Gets the date.
   *
   * @return String
   */
  public String getDate() {
    return mDate;
  }

  /**
   * Sets the date.
   *
   * @param date The creation date.
   */
  public void setDate(String date) {
    mDate = date;
  }

}
