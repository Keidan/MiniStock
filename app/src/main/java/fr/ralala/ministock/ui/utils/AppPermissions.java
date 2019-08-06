package fr.ralala.ministock.ui.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import fr.ralala.ministock.R;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Manages the application permissions.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class AppPermissions {

  public static final int PERMISSIONS_REQUEST = 1;

  /**
   * Callback for the result from requesting permissions.
   *
   * @param a            The owner activity.
   * @param permissions  The requested permissions. Never null.
   * @param grantResults The grant results for the corresponding permissions which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
   * @return False on error, true else.
   */
  public static boolean onRequestPermissionsResult(Activity a, @NonNull String permissions[], @NonNull int[] grantResults) {
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
  public static boolean checkPermissions(Activity a) {
    return ContextCompat.checkSelfPermission(a,
        Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(a,
            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(a,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(a,
            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
  }

  /**
   * Requests the required permissions.
   */
  private static void requestPermissions(Activity a) {
    ActivityCompat.requestPermissions(a, new String[]{
        Manifest.permission.INTERNET,
        Manifest.permission.VIBRATE,
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
    }, PERMISSIONS_REQUEST);
  }

  /**
   * If a message needs to be displayed to request permissions,
   * it will be displayed after this call and a new authorization request will be made.
   */
  public static void shouldShowRequest(Activity a) {
    if (ActivityCompat.shouldShowRequestPermissionRationale(a, Manifest.permission.BLUETOOTH) ||
        ActivityCompat.shouldShowRequestPermissionRationale(a, Manifest.permission.BLUETOOTH_ADMIN) ||
        ActivityCompat.shouldShowRequestPermissionRationale(a, Manifest.permission.ACCESS_COARSE_LOCATION) ||
        ActivityCompat.shouldShowRequestPermissionRationale(a, Manifest.permission.ACCESS_FINE_LOCATION)) {
      UIHelper.showAlertDialog(a,
          R.string.permissions_title,
          a.getString(R.string.permissions_required),
          (unused) -> requestPermissions(a));
    } else
      requestPermissions(a);
  }
}
