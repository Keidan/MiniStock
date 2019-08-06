package fr.ralala.ministock.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.UUID;

import fr.ralala.ministock.R;
import fr.ralala.ministock.ui.utils.UIHelper;

/**
 *******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Shopping cart entry
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class ShoppingCartEntry implements Serializable {
  private @NonNull
  String mId;
  private String mTitle;
  private String mImage;
  private int mCount = 0;
  private String mQrCodeId;
  private OnClickListener mClickListener;

  public interface OnClickListener {
    /**
     * onClick listener.
     *
     * @param e        The selected entry.
     * @param plus     true for plus false for subtract.
     * @param position The position in the adapter.
     */
    void onClick(ShoppingCartEntry e, boolean plus, int position);
  }
  /**
   * Called from the bdd.
   * @param c The android context used for the default image.
   */
  public ShoppingCartEntry(Context c) {
    mId = UUID.randomUUID().toString();
    initialize("", image2string(UIHelper.getBitmap(c, R.mipmap.shopping_cart_white)), 0, "", null);
  }

  /**
   * Called from the bdd.
   *
   * @param id        The SQL id.
   * @param title     The item title.
   * @param image     The image resource.
   * @param count     The item count.
   * @param qrCodeId  The QR code ID.
   */
  public ShoppingCartEntry(@NonNull String id, String title, String image, int count, String qrCodeId) {
    mId = id;
    initialize(title, image, count, qrCodeId, null);
  }

  /**
   * Constructs a new instance (SQLite).
   *
   * @param title         The item title.
   * @param image         The image resource.
   * @param count         The item count.
   * @param qrCodeId      The QR code ID.
   * @param clickListener View.OnClickListener.
   */
  public ShoppingCartEntry(String title, Bitmap image, int count, String qrCodeId, OnClickListener clickListener) {
    mId = UUID.randomUUID().toString();
    initialize(title, image2string(image), count, qrCodeId, clickListener);
  }

  /**
   * Constructs a new instance (SQLite).
   *
   * @param title         The item title.
   * @param image         The image resource.
   * @param count         The item count.
   * @param clickListener View.OnClickListener.
   */
  public ShoppingCartEntry(String title, Bitmap image, int count, OnClickListener clickListener) {
    mId = UUID.randomUUID().toString();
    initialize(title, image2string(image), count, "", clickListener);
  }

  private void initialize(String title, String image, int count, String qrCodeId, OnClickListener clickListener) {
    mTitle = title;
    mCount = count;
    mImage = image;
    mQrCodeId = qrCodeId;
    mClickListener = clickListener;
  }

  /**
   * Returns the object in JSON format (without braces).
   * @return String
   */
  public String toJSON() {
    return "\"id\": \"" + mId + "\", \"title\": \"" + mTitle + "\", \"count\": " +
        mCount + ", \"qrCodeId\": \"" + mQrCodeId + "\", \"image\": \"" + mImage + "\"";
  }

  /**
   * Returns the item title.
   * @return String
   */
  public String toString() {
    return mTitle;
  }

  /**
   * Returns the DB ID.
   * @return String
   */
  public String getID() {
    return mId;
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

  /**
   * Sets the listener for the buttons click.
   *
   * @param clickListener View.OnClickListener
   */
  public void setClickListener(OnClickListener clickListener) {
    mClickListener = clickListener;
  }

  /**
   * Returns the listener for the buttons click.
   *
   * @return View.OnClickListener
   */
  public OnClickListener getClickListener() {
    return mClickListener;
  }

  /**
   * Returns the item title.
   *
   * @return String
   */
  public String getTitle() {
    return mTitle;
  }

  /**
   * Sets the item title.
   *
   * @param title The item title.
   */
  public void setTitle(String title) {
    mTitle = title;
  }

  /**
   * Returns the image resource.
   *
   * @return Bitmap
   */
  public @NonNull Bitmap getImage() {
    return string2image(mImage);
  }

  /**
   * Sets the image resource.
   *
   * @param image The image to set.
   */
  public void setImage(Bitmap image) {
    mImage = image2string(image);
  }

  /**
   * Returns the item count.
   *
   * @return int
   */
  public int getCount() {
    return mCount;
  }

  /**
   * Sets the item count.
   *
   * @param count New count value.
   */
  public void setCount(int count) {
    mCount = count;
  }


  private Bitmap string2image(String encodedImage) {
    byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
    return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
  }

  private String image2string(Bitmap bmp) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
    return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
  }
}
