package fr.ralala.ministock.ui.launchers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import fr.ralala.ministock.ui.activities.ScanActivity;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Permissions requests.
 * </p>
 *
 * @author Keidan
 * ******************************************************************************
 */
public class LauncherCameraPermission {
  private final ScanActivity mActivity;
  private ActivityResultLauncher<String> mActivityResultLauncher;

  public LauncherCameraPermission(ScanActivity activity) {
    mActivity = activity;
    register();
  }

  /**
   * Registers result launcher for the activity for opening a file.
   */
  private void register() {
    mActivityResultLauncher = mActivity.registerForActivityResult(
      new ActivityResultContracts.RequestPermission(),
      isGranted -> {
        if (Boolean.TRUE.equals(isGranted)) {
          mActivity.loadCamera();
        }
      });
  }

  /**
   * Starts this launcher.
   */
  public void start() {
    mActivityResultLauncher.launch(Manifest.permission.CAMERA);
  }

  /**
   * Checks if the camera can be used.
   *
   * @param ctx The context.
   * @return boolean
   */
  public static boolean checkCameraPermission(Context ctx) {
    return ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
  }
}

