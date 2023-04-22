package fr.ralala.ministock.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import fr.ralala.ministock.ApplicationCtx;
import fr.ralala.ministock.R;
import fr.ralala.ministock.ui.launchers.LauncherCameraPermission;


/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Scan (QRCode+Barcode) activity.
 * </p>
 *
 * @author Keidan
 * ******************************************************************************
 */
public class ScanActivity extends AppCompatActivity implements SurfaceHolder.Callback, Detector.Processor<Barcode> {
  public static final String KEY_QRCODE = "qrcode";
  private ApplicationCtx mApp;
  private SurfaceView mCameraPreview;
  private TextView mTvCode;
  private BarcodeDetector mBarcodeDetector;
  private CameraSource mCameraSource;
  private boolean mIsStopped = true;
  private boolean mManaged = false;
  private long mDelay = 0L;
  private Timer mTimer;
  private Vibrator mVibrator;
  private LauncherCameraPermission mLauncherCameraPermission;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    mApp = (ApplicationCtx) getApplication();
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_scan);
    mLauncherCameraPermission = new LauncherCameraPermission(this);
    /* Rebuild activity toolbar */
    ActionBar actionBar = getDelegate().getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /* gets the reference to the order list */
    mCameraPreview = findViewById(R.id.cameraPreview);
    mCameraPreview.getHolder().setFormat(PixelFormat.TRANSLUCENT);
    mTvCode = findViewById(R.id.tvCode);
    mCameraPreview.setVisibility(View.INVISIBLE);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      VibratorManager vm = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
      mVibrator = vm.getDefaultVibrator();
    } else {
      @SuppressWarnings("deprecation")
      Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
      mVibrator = vibrator;
    }

    mTvCode.setOnClickListener(v -> {
      LayoutInflater inflater = getLayoutInflater();
      final ViewGroup nullParent = null;
      View alertLayout = inflater.inflate(R.layout.dialog_qrcode, nullParent);
      final TextInputEditText input = alertLayout.findViewById(R.id.tietText);
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setView(alertLayout);
      // Set up the buttons
      builder.setPositiveButton(R.string.ok, (dialog, which) ->
      {
        String text = Objects.requireNonNull(input.getText()).toString();
        if (!text.trim().isEmpty())
          mTvCode.setText(text);
      });
      builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
      builder.show();
    });
    setResult(RESULT_CANCELED);
  }

  /**
   * Stops the camera timer.
   */
  private void stopTimer() {
    if (mTimer != null) {
      mTimer.cancel();
      mTimer.purge();
    }
  }

  /**
   * Starts/Restarts the camera timer.
   */
  private void restartTimer() {
    stopTimer();
    long delay = mApp.getCameraTimeout();
    if (delay == 0L)
      return;
    mTimer = new Timer();
    TimerTask tt = new TimerTask() {
      @Override
      public void run() {
        runOnUiThread(() -> {
          stopCameraPreview();
          mCameraPreview.setVisibility(View.INVISIBLE);
        });
      }
    };
    mTimer.schedule(tt, delay, delay); // execute in every 15sec
  }

  /**
   * Called when the activity is resumed.
   */
  @Override
  public void onResume() {
    super.onResume();
    /* force refresh */
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
  }

  /**
   * Called when the activity is paused.
   */
  @Override
  public void onPause() {
    super.onPause();
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
  }

  /**
   * Loads the camera object
   */
  public void loadCamera() {
    if (!LauncherCameraPermission.checkCameraPermission(this)) {
      Toast.makeText(this, R.string.error_camera, Toast.LENGTH_LONG).show();
      return;
    }
    try {
      mCameraSource.start(mCameraPreview.getHolder());
      mIsStopped = false;
    } catch (IOException e) {
      Toast.makeText(this, R.string.error_camera, Toast.LENGTH_LONG).show();
      e.printStackTrace();
    }
  }

  /**
   * Called when the activity is destroyed.
   */
  @Override
  protected void onDestroy() {
    stopCameraPreview();
    super.onDestroy();
  }

  /**
   * Called when the options menu is clicked.
   *
   * @param menu The selected menu.
   * @return boolean
   */
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.scan, menu);
    return true;
  }

  /**
   * Called when the options item is clicked (home and cancel).
   *
   * @param item The selected menu.
   * @return boolean
   */
  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    if (item.getItemId() == android.R.id.home)
      onBackPressed();
    else if (item.getItemId() == R.id.action_onoff) {
      if (mIsStopped) {
        item.setIcon(R.drawable.button_scan_on_white);
        mCameraPreview.setVisibility(View.VISIBLE);
        createCameraPreview();
        restartTimer();
      } else {
        item.setIcon(R.drawable.button_scan_off_white);
        stopCameraPreview();
        mCameraPreview.setVisibility(View.INVISIBLE);
      }
      mTvCode.setText("");
    } else if (item.getItemId() == R.id.action_done) {
      Intent resultIntent = new Intent();
      String qrcode = mTvCode.getText().toString();
      if (getString(R.string.qrcode_hint).equals(qrcode))
        qrcode = "";
      resultIntent.putExtra(KEY_QRCODE, qrcode);
      setResult(RESULT_OK, resultIntent);
      finish();
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Called when the barcode receiver is released.
   */
  @Override
  public void release() {
    /* nothing */
  }

  /**
   * Called when a barcode is detected.
   *
   * @param detections The detections.
   */
  @Override
  public void receiveDetections(Detector.Detections<Barcode> detections) {
    final SparseArray<Barcode> codes = detections.getDetectedItems();
    if (codes.size() != 0 && !mManaged) {
      mTvCode.post(() -> applyScanEntry(codes.valueAt(0).displayValue));
    }
  }


  /**
   * Applies a scan entry.
   *
   * @param text The text to validate.
   */
  private void applyScanEntry(String text) {
    if (text.isEmpty()) {
      return;
    }
    if ((System.currentTimeMillis() - mDelay) < mApp.getScanTimeout()) {
      return;
    } else
      mDelay = System.currentTimeMillis();
    restartTimer();
    mManaged = true;
    //Create vibrate
    if (mVibrator != null)
      mVibrator.vibrate(VibrationEffect.createOneShot(150, 10));
    mTvCode.setText(text.replaceAll("\\p{C}", ""));
  }

  /**
   * Creates the camera preview.
   */
  private void createCameraPreview() {
    mBarcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build();
    // Creates and starts the camera.  Note that this uses a higher resolution in comparison
    // to other detection examples to enable the barcode detector to detect small barcodes
    // at long distances.
    mCameraSource = new CameraSource.Builder(this, mBarcodeDetector)
      .setFacing(CameraSource.CAMERA_FACING_BACK)
      .setRequestedPreviewSize(1600, 900)
      /*.setRequestedPreviewSize(800, 600)
      .setRequestedFps(15.0f)*/
      .setAutoFocusEnabled(true)
      .build();

    //Add Event
    mCameraPreview.getHolder().addCallback(this);
    mBarcodeDetector.setProcessor(this);
    if (LauncherCameraPermission.checkCameraPermission(this)) {
      mLauncherCameraPermission.start();
    } else {
      loadCamera();
    }
  }

  /**
   * Stop the camera preview.
   */
  private void stopCameraPreview() {
    if (!mIsStopped) {
      mBarcodeDetector.release();
      mCameraPreview.getHolder().removeCallback(this);
      mCameraSource.stop();
      mCameraSource.release();
      mDelay = 0;
      mIsStopped = true;
    }
    stopTimer();
  }


  /**
   * Called when the surface view is created.
   *
   * @param surfaceHolder The surface holder.
   */
  @Override
  public void surfaceCreated(SurfaceHolder surfaceHolder) {
    /* nothing */
  }


  /**
   * Called when the surface view is changed.
   *
   * @param surfaceHolder The surface holder.
   * @param i             Unused.
   * @param i1            Unused.
   * @param i2            Unused.
   */
  @Override
  public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    /* nothing */
  }

  /**
   * Called when the surface view is destroyed.
   *
   * @param surfaceHolder The surface holder.
   */
  @Override
  public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    if (mCameraSource != null)
      mCameraSource.stop();
  }
}
