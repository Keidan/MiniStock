package fr.ralala.ministock.ui.utils;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import java.io.InputStream;
import java.util.Calendar;

import fr.ralala.ministock.R;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * UI Helper functions.
 * </p>
 *
 * @author Keidan
 * ******************************************************************************
 */
public class UIHelper {
  private UIHelper() {
  }

  /**
   * Decodes a bitmap array.
   *
   * @param data Array
   * @return Bitmap
   */
  public static Bitmap decodeBitmap(byte[] data) {
    BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
    return BitmapFactory.decodeByteArray(data, 0, data.length, opts);
  }

  /**
   * Resizes a bitmap
   *
   * @param bm     Bitmap
   * @param width  Width
   * @param height Height
   * @return Bitmap
   */
  public static Bitmap resizeBitmap(Bitmap bm, int width, int height) {
    return Bitmap.createScaledBitmap(bm, width, height, false);
  }

  /**
   * Converts an ImageView to a Parcelable.
   *
   * @param iv ImageView
   * @return Parcelable
   */
  public static Parcelable imageViewToParcelable(ImageView iv) {
    iv.invalidate();
    BitmapDrawable drawable = (BitmapDrawable) iv.getDrawable();
    return drawable.getBitmap();
  }

  /**
   * Decodes a bitmap stream.
   *
   * @param stream InputStream
   * @return Bitmap
   */
  public static Bitmap decodeBitmap(InputStream stream) {
    BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
    return BitmapFactory.decodeStream(stream, null, opts);
  }

  /**
   * Creates Bitmap objects from resource.
   *
   * @param c   The Android context.
   * @param res The drawable resource.
   * @return Bitmap
   */
  public static Bitmap getBitmap(Context c, int res) {
    return BitmapFactory.decodeResource(c.getResources(), res);
  }

  /**
   * Converts a Drawable object to a Bitmap object.
   *
   * @param drawable The Drawable object to convert.
   * @return The Bitmap object.
   */
  public static Bitmap drawableToBitmap(Drawable drawable) {
    Bitmap bitmap;

    if (drawable instanceof BitmapDrawable) {
      BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
      if (bitmapDrawable.getBitmap() != null) {
        return bitmapDrawable.getBitmap();
      }
    }

    if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
      bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
    } else {
      bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
    }

    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);
    return bitmap;
  }

  /**
   * Displays a confirm dialog.
   *
   * @param c       The Android context.
   * @param message The dialog message.
   * @param yes     Listener used when the 'yes' button is clicked.
   */
  public static void showConfirmDialog(final Context c,
                                       String message, final android.view.View.OnClickListener yes) {
    new AlertDialog.Builder(c)
      .setCancelable(false)
      .setMessage(message)
      .setPositiveButton(R.string.yes, (dialog, whichButton) -> {
        if (yes != null) yes.onClick(null);
      })
      .setNegativeButton(R.string.no, (dialog, whichButton) -> {
      }).show();
  }

  /**
   * Displays an alert dialog.
   *
   * @param c       The Android context.
   * @param title   The alert dialog title.
   * @param message The alert dialog message.
   * @return AlertDialog
   */
  public static AlertDialog showAlertDialog(final Context c, final int title, final String message) {
    return showAlertDialog(c, title, message, null);
  }

  /**
   * Displays an alert dialog.
   *
   * @param c       The Android context.
   * @param title   The alert dialog title.
   * @param message The alert dialog message.
   * @param click   Click on the ok button (The callback parameter is always null).
   */
  public static AlertDialog showAlertDialog(final Context c, final int title, final String message, final View.OnClickListener click) {
    AlertDialog alertDialog = new AlertDialog.Builder(c).create();
    alertDialog.setTitle(c.getResources().getString(title));
    alertDialog.setMessage(message);
    alertDialog.setCancelable(click == null);
    alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
      c.getResources().getString(R.string.ok), (dialog, which) -> {
        if (click != null)
          click.onClick(null);
        dialog.dismiss();
      });
    alertDialog.show();
    return alertDialog;
  }

  /**
   * Displays a toast.
   *
   * @param c       The Android context.
   * @param message The toast message.
   */
  public static void toast(final Context c, final @StringRes int message) {
    /* Create a toast with the launcher icon */
    Toast toast = Toast.makeText(c, message, Toast.LENGTH_LONG);
    toast.show();
  }

  /**
   * Displays a toast.
   *
   * @param c       The Android context.
   * @param message The toast message.
   */
  public static void toastShort(final Context c, final @StringRes int message) {
    /* Create a toast with the launcher icon */
    Toast toast = Toast.makeText(c, message, Toast.LENGTH_SHORT);
    toast.show();
  }

  /**
   * Opens a date picker dialog.
   *
   * @param c       The Android context.
   * @param current The current date.
   * @param li      The listener used when the date is selected.
   */
  public static void openDatePicker(final Context c, final Calendar current, DatePickerDialog.OnDateSetListener li) {
    DatePickerDialog dpd = new DatePickerDialog(c, li, current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH));
    dpd.show();
  }

  /**
   * Hides the keyboard.
   * @param activity The displayed activity.
   */
  public static void hideKeyboard(final Activity activity) {
    /* hide keyboard */
    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
    if (imm.isAcceptingText()) {
      View view = activity.getCurrentFocus();
      if (view == null) {
        view = new View(activity);
      }
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }
}
