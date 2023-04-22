package fr.ralala.ministock.ui.launchers;

import android.app.Activity;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import fr.ralala.ministock.db.models.CartItem;
import fr.ralala.ministock.ui.activities.CartItemActivity;
import fr.ralala.ministock.ui.activities.ScanActivity;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Starts the scan activity.
 * </p>
 *
 * @author Keidan
 * ******************************************************************************
 */
public class LauncherScanActivity {
  private final CartItemActivity mActivity;
  private ActivityResultLauncher<Intent> activityResult;
  private CartItem mCartItem;
  private int mPosition;

  public LauncherScanActivity(CartItemActivity activity) {
    mActivity = activity;
    register();
  }

  /**
   * Registers result launcher for the activity for opening a file.
   */
  private void register() {
    activityResult = mActivity.registerForActivityResult(
      new ActivityResultContracts.StartActivityForResult(),
      result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
          Intent data = result.getData();
          if (data != null) {
            mActivity.setQrCode(mCartItem, mPosition, data.getStringExtra(ScanActivity.KEY_QRCODE));
          }
        }
      });
  }

  /**
   * Starts the activity
   */
  public void start(CartItem ci, int position) {
    mCartItem = ci;
    mPosition = position;
    activityResult.launch(new Intent(mActivity, ScanActivity.class));
  }
}
