package fr.ralala.ministock.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import fr.ralala.ministock.MainApplication;
import fr.ralala.ministock.R;


/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Scan (QRCode+Barcode) activity.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class ScanActivity extends AppCompatActivity implements SurfaceHolder.Callback, Detector.Processor<Barcode> {
  protected static final String KEY_QRCODE = "qrcode";
  public static final int REQUEST_START_ACTIVITY = 20;
  private static final int REQUEST_CAMERA_PERMISSION_ID = 1001;
  private MainApplication mApp;
  private SurfaceView mCameraPreview;
  private TextView mTvCode;
  private BarcodeDetector mBarcodeDetector;
  private CameraSource mCameraSource;
  private boolean mStopped = true;
  private boolean mManaged = false;
  private long mDelay = 0L;
  private Timer mTimer;
  private Vibrator mVibrator;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    mApp = (MainApplication) getApplication();
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_scan);

    /* Rebuild activity toolbar */
    android.support.v7.app.ActionBar actionBar = getDelegate().getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /* gets the reference to the order list */
    mCameraPreview = findViewById(R.id.cameraPreview);
    //mCameraPreview.setZOrderOnTop(true);
    mCameraPreview.getHolder().setFormat(PixelFormat.TRANSLUCENT);
    mTvCode = findViewById(R.id.tvCode);
    mCameraPreview.setVisibility(View.INVISIBLE);
    mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

    mTvCode.setOnClickListener((v) -> {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(R.string.qrcode_hint);
      // Set up the input
      final EditText input = new EditText(this);
      // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
      input.setInputType(InputType.TYPE_CLASS_TEXT);
      builder.setView(input);
      // Set up the buttons
      builder.setPositiveButton(R.string.ok, (dialog, which) -> mTvCode.setText(input.getText().toString()));
      builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
      builder.show();
    });
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
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
   * Called to handle the click on the back button.
   */
  @Override
  public void onBackPressed() {
    setResult(RESULT_CANCELED);
    super.onBackPressed();
  }

  /**
   * Called on permission result.
   *
   * @param requestCode  The request code.
   * @param permissions  The permissions.
   * @param grantResults The grant results.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    switch (requestCode) {
      case REQUEST_CAMERA_PERMISSION_ID: {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
          }
          try {
            mCameraSource.start(mCameraPreview.getHolder());
          } catch (IOException e) {
            Toast.makeText(this, R.string.error_camera, Toast.LENGTH_LONG).show();
            e.printStackTrace();
          }
        }
      }
      break;
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
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;
      case R.id.action_onoff:
        if (mStopped) {
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
        break;
      case R.id.action_done:
        Intent resultIntent = new Intent();
        resultIntent.putExtra(KEY_QRCODE, mTvCode.getText());
        setResult(RESULT_OK, resultIntent);
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Called when the barcode receiver is released.
   */
  @Override
  public void release() {
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
    mTvCode.setText(text);
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
    if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
      //Request permission
      ActivityCompat.requestPermissions(this,
          new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_ID);
      return;
    }
    try {
      mCameraSource.start(mCameraPreview.getHolder());
    } catch (IOException e) {
      Toast.makeText(this, R.string.error_camera, Toast.LENGTH_LONG).show();
      e.printStackTrace();
    }
    mStopped = false;
  }

  /**
   * Stop the camera preview.
   */
  private void stopCameraPreview() {
    if (!mStopped) {
      mBarcodeDetector.release();
      mCameraPreview.getHolder().removeCallback(this);
      mCameraSource.stop();
      mCameraSource.release();
      mStopped = true;
    }
    stopTimer();
  }


  /**
   * Called when the surfaceview is created.
   *
   * @param surfaceHolder The surface holder.
   */
  @Override
  public void surfaceCreated(SurfaceHolder surfaceHolder) {
  }


  /**
   * Called when the surfaceview is changed.
   *
   * @param surfaceHolder The surface holder.
   * @param i             Unused.
   * @param i1            Unused.
   * @param i2            Unused.
   */
  @Override
  public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

  }

  /**
   * Called when the surfaceview is destroyed.
   *
   * @param surfaceHolder The surface holder.
   */
  @Override
  public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    if (mCameraSource != null)
      mCameraSource.stop();
  }
}
