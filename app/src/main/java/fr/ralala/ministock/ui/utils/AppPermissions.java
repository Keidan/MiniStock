package fr.ralala.ministock.ui.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import fr.ralala.ministock.R;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Manages the application permissions.
 * </p>
 *
 * @author Keidan
 * ******************************************************************************
 */
public class AppPermissions {

  public static final int PERMISSIONS_REQUEST = 1;

  private AppPermissions() {

  }

  /**
   * Callback for the result from requesting permissions.
   *
   * @param a            The owner activity.
   * @param permissions  The requested permissions. Never null.
   * @param grantResults The grant results for the corresponding permissions which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
   * @return False on error, true else.
   */
  public static boolean onRequestPermissionsResult(Activity a, @NonNull String[] permissions, @NonNull int[] grantResults) {
    for (int i = 0; i < grantResults.length; i++) {
      int n = grantResults[i];
      if (!permissions[i].equals(Manifest.permission.VIBRATE) &&
        n != PackageManager.PERMISSION_GRANTED) {
        shouldShowRequest(a);
        return false;
      }
    }
    return true;
  }

  /**
   * Tests if the required permissions are granted.
   *
   * @return boolean
   */
  public static boolean checkPermissions(Context ctx) {
    return ContextCompat.checkSelfPermission(ctx,
      Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(ctx,
        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(ctx,
        Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(ctx,
        Manifest.permission.REQUEST_INSTALL_PACKAGES) == PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(ctx,
        Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED &&
      (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(ctx,
        Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED);
  }


  public static boolean checkSelfPermissionPostNotifications(Context ctx) {
    if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
      return ContextCompat.checkSelfPermission(ctx,
        Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    return true;
  }

  /**
   * Requests the required permissions.
   */
  private static void requestPermissions(Activity a) {
    List<String> list = new ArrayList<>();
    list.add(Manifest.permission.INTERNET);
    list.add(Manifest.permission.VIBRATE);
    list.add(Manifest.permission.READ_EXTERNAL_STORAGE);
    list.add(Manifest.permission.REQUEST_INSTALL_PACKAGES);
    list.add(Manifest.permission.ACCESS_NETWORK_STATE);
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
      list.add(Manifest.permission.POST_NOTIFICATIONS);
    ActivityCompat.requestPermissions(a, list.toArray(new String[]{}), PERMISSIONS_REQUEST);
  }

  /**
   * If a message needs to be displayed to request permissions,
   * it will be displayed after this call and a new authorization request will be made.
   */
  public static void shouldShowRequest(Activity a) {
    if (ActivityCompat.shouldShowRequestPermissionRationale(a, Manifest.permission.INTERNET) ||
      ActivityCompat.shouldShowRequestPermissionRationale(a, Manifest.permission.VIBRATE) ||
      ActivityCompat.shouldShowRequestPermissionRationale(a, Manifest.permission.REQUEST_INSTALL_PACKAGES) ||
      ActivityCompat.shouldShowRequestPermissionRationale(a, Manifest.permission.READ_EXTERNAL_STORAGE) ||
      ActivityCompat.shouldShowRequestPermissionRationale(a, Manifest.permission.ACCESS_NETWORK_STATE) ||
      (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
        ActivityCompat.shouldShowRequestPermissionRationale(a, Manifest.permission.POST_NOTIFICATIONS))) {
      UIHelper.showAlertDialog(a,
        R.string.permissions_title,
        a.getString(R.string.permissions_required),
        unused -> requestPermissions(a));
    } else
      requestPermissions(a);
  }
}
