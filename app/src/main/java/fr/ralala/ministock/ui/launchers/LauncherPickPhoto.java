package fr.ralala.ministock.ui.launchers;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import java.io.FileNotFoundException;
import java.io.InputStream;

import fr.ralala.ministock.R;
import fr.ralala.ministock.ui.activities.CartItemActivity;
import fr.ralala.ministock.ui.utils.UIHelper;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Pick a photo.
 * </p>
 *
 * @author Keidan
 * ******************************************************************************
 */
public class LauncherPickPhoto {
  private final CartItemActivity mActivity;
  private ActivityResultLauncher<PickVisualMediaRequest> activityResult;

  public LauncherPickPhoto(CartItemActivity activity) {
    mActivity = activity;
    register();
  }

  /**
   * Registers result launcher for the activity for opening a file.
   */
  private void register() {
    activityResult = mActivity.registerForActivityResult(
      new ActivityResultContracts.PickVisualMedia(),
      result -> {
        if (result != null) {
          try {
            ContentResolver cr = mActivity.getContentResolver();
            cr.takePersistableUriPermission(result, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            InputStream inputStream = cr.openInputStream(result);
            Bitmap bm = UIHelper.decodeBitmap(inputStream);
            mActivity.setImage(UIHelper.resizeBitmap(bm, CartItemActivity.LOGO_WIDTH, CartItemActivity.LOGO_HEIGHT));
          } catch (FileNotFoundException ex) {
            Toast.makeText(mActivity, R.string.error_file_not_found, Toast.LENGTH_LONG).show();
            Log.e(getClass().getSimpleName(), "FileNotFoundException: " + ex.getMessage(), ex);
          }
        }
      });
  }

  /**
   * Starts the activity
   */
  public void start() {
    PickVisualMediaRequest request = new PickVisualMediaRequest.Builder()
      .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
      .build();
    activityResult.launch(request);
  }
}