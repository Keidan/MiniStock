package fr.ralala.ministock.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import fr.ralala.ministock.R;
import fr.ralala.ministock.ui.utils.UIHelper;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * cart entry
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class CartEntry implements Serializable {
  private static final String VERSION = "1.1";
  private final String mVersion;
  private final String mId;
  private String mTitle;
  private String mImage;
  private final List<CartItem> mItems = new ArrayList<>();

  /**
   * Called from the bdd.
   *
   * @param c The android context used for the default image.
   */
  public CartEntry(Context c) {
    mId = UUID.randomUUID().toString();
    mVersion = VERSION;
    mTitle = "";
    mImage = image2string(UIHelper.getBitmap(c, R.mipmap.shopping_cart_white));
  }

  /**
   * Called from the bdd.
   *
   * @param json The json object.
   */
  public CartEntry(JSONObject json) throws JSONException {
    mId = json.getString("id");
    mVersion = json.getString("version");
    mTitle = json.getString("title");
    mImage = json.getString("image");
    JSONArray items = json.getJSONArray("items");
    for (int i = 0; i < items.length(); i++) {
      JSONObject o = items.getJSONObject(i);
      mItems.add(new CartItem(o));
    }
    sortItems();
  }

  public void sortItems() {
    mItems.sort(Comparator.comparing(CartItem::getDate));
  }

  /**
   * Returns the object in JSON format (without braces).
   *
   * @return String
   */
  public String toJSON() {
    StringBuilder sb = new StringBuilder();
    sb.append("\"version\": \"").append(mVersion).append("\", \"id\": \"")
      .append(mId).append("\", \"title\": \"").append(mTitle).append("\", \"items\": [");
    for (int i = 0; i < mItems.size(); i++) {
      CartItem item = mItems.get(i);
      sb.append(item.toJSON());
      if (i < mItems.size() - 1)
        sb.append(",");
    }
    sb.append("], \"image\": \"").append(mImage).append("\"");
    return sb.toString();
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
   * Returns the item title.
   *
   * @return String
   */
  @Override
  public @NonNull String toString() {
    return mTitle;
  }

  /**
   * Returns the DB ID.
   *
   * @return String
   */
  public String getID() {
    return mId;
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
   * Returns the items.
   *
   * @return List<CartItem>
   */
  public List<CartItem> getItems() {
    return mItems;
  }

  private Bitmap string2image(String encodedImage) {
    byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
    return UIHelper.decodeBitmap(decodedString);
  }

  private String image2string(Bitmap bmp) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
    return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
  }
}
