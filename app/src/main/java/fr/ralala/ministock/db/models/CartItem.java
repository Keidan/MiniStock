package fr.ralala.ministock.db.models;

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
  private static final String VERSION = "1.1";
  private final String mVersion;
  private String mQrCodeId;
  private String mCreationDate;


  /**
   * Called from the bdd.
   *
   * @param json The json object.
   */
  public CartItem(JSONObject json) throws JSONException {
    mVersion = json.getString("version");
    mQrCodeId = json.getString("qrCodeId");
    mCreationDate = json.getString("creationDate").replace("\\", "/");
  }

  /**
   * Called from the bdd.
   *
   * @param qrCodeId     The QR code ID.
   * @param creationDate The creation date.
   */
  public CartItem(String qrCodeId, String creationDate) {
    mVersion = VERSION;
    mQrCodeId = qrCodeId;
    mCreationDate = (creationDate == null) ? getDate() : creationDate;
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
    return "{\"version\": \"" + mVersion + "\", \"qrCodeId\": \"" + mQrCodeId + "\", \"creationDate\": \"" + mCreationDate + "\"}";
  }

  public static String getDate() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
    return sdf.format(new Date());
  }

  /**
   * Gets the creation date.
   *
   * @return String
   */
  public String getCreationDate() {
    return mCreationDate;
  }

  /**
   * Sets the creation date.
   *
   * @param creationDate The creation date.
   */
  public void setCreationDate(String creationDate) {
    mCreationDate = creationDate;
  }

  /**
   * Returns the QR code ID.
   *
   * @return String
   */
  public String getQrCodeId() {
    return mQrCodeId;
  }

  /**
   * Sets the QR code ID.
   *
   * @param qrCodeId The QR code ID.
   */
  public void setQrCodeId(String qrCodeId) {
    mQrCodeId = qrCodeId;
  }
}
