package fr.ralala.ministock.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Spinner;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import fr.ralala.ministock.ApplicationCtx;
import fr.ralala.ministock.R;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Settings activity.
 * </p>
 *
 * @author Keidan
 * ******************************************************************************
 */
public class SettingsActivity extends AppCompatActivity {
  private ApplicationCtx mApp;
  private Spinner mSpProtocols;
  private TextInputLayout mTilHost;
  private TextInputEditText mTietHost;
  private TextInputLayout mTilPort;
  private TextInputEditText mTietPort;
  private TextInputLayout mTilPage;
  private TextInputEditText mTietPage;
  private TextInputLayout mTilUsername;
  private TextInputEditText mTietUsername;
  private TextInputLayout mTilPassword;
  private TextInputEditText mTietPassword;
  private Spinner mSpCameraTimeout;
  private Spinner mSpScanTimeout;

  /**
   * Called when the activity is created.
   *
   * @param savedInstanceState The saved instance state.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    /* Rebuild activity toolbar */
    ActionBar actionBar = getDelegate().getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
    mApp = (ApplicationCtx) getApplication();
    mSpProtocols = findViewById(R.id.spProtocols);
    mTilHost = findViewById(R.id.tilHost);
    mTietHost = findViewById(R.id.tietHost);
    mTilPort = findViewById(R.id.tilPort);
    mTietPort = findViewById(R.id.tietPort);
    mTilPage = findViewById(R.id.tilPage);
    mTietPage = findViewById(R.id.tietPage);
    mTilUsername = findViewById(R.id.tilUsername);
    mTietUsername = findViewById(R.id.tietUsername);
    mTilPassword = findViewById(R.id.tilPassword);
    mTietPassword = findViewById(R.id.tietPassword);
    mSpCameraTimeout = findViewById(R.id.spCameraTimeout);
    mSpScanTimeout = findViewById(R.id.spScanTimeout);
  }


  private void fillDefault() {
    String[] array = getResources().getStringArray(R.array.protocols);
    for (int i = 0; i < array.length; i++) {
      if (array[i].equals(mApp.getProtocol())) {
        mSpProtocols.setSelection(i);
        break;
      }
    }
    mTietHost.setText(mApp.getHOST());
    mTietPort.setText(String.valueOf(mApp.getPort()));
    mTietPage.setText(mApp.getPage());
    mTietUsername.setText(mApp.getUsername());
    mTietPassword.setText(mApp.getPassword());
    mSpCameraTimeout.setSelection(mApp.getCameraTimeoutIndex() - 1);
    mSpScanTimeout.setSelection(mApp.getScanTimeoutIndex() - 1);
  }

  /**
   * Called when the fragment is resumed.
   */
  @Override
  public void onResume() {
    super.onResume();
    fillDefault();
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
    inflater.inflate(R.menu.settings, menu);
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
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    } else if (item.getItemId() == R.id.action_validate) {
      if (TextUtils.isEmpty(mTietHost.getText())) {
        mTilHost.setError(getString(R.string.error_invalid_host));
        return true;
      }
      if (TextUtils.isEmpty(mTietPort.getText())) {
        mTilPort.setError(getString(R.string.error_invalid_port));
        return true;
      }
      if (TextUtils.isEmpty(mTietPage.getText())) {
        mTilPage.setError(getString(R.string.error_invalid_page));
        return true;
      }
      if (TextUtils.isEmpty(mTietUsername.getText())) {
        mTilUsername.setError(getString(R.string.error_invalid_username));
        return true;
      }
      if (TextUtils.isEmpty(mTietPassword.getText())) {
        mTilPassword.setError(getString(R.string.error_invalid_password));
        return true;
      }

      SharedPreferences.Editor e = mApp.getSharedPreferences().edit();
      e.putString(ApplicationCtx.PREF_KEY_PROTOCOL, "" + mSpProtocols.getSelectedItem());
      e.putString(ApplicationCtx.PREF_KEY_USERNAME, mTietUsername.getText().toString());
      e.putString(ApplicationCtx.PREF_KEY_PASSWORD, mTietPassword.getText().toString());
      e.putString(ApplicationCtx.PREF_KEY_HOST, mTietHost.getText().toString());
      e.putInt(ApplicationCtx.PREF_KEY_PORT, Integer.parseInt(mTietPort.getText().toString()));
      e.putString(ApplicationCtx.PREF_KEY_PAGE, mTietPage.getText().toString());
      e.putInt(ApplicationCtx.PREF_KEY_CAMERA_TIMEOUT, Integer.parseInt("" + (mSpCameraTimeout.getSelectedItemPosition() + 1)));
      e.putInt(ApplicationCtx.PREF_KEY_SCAN_TIMEOUT, Integer.parseInt("" + (mSpScanTimeout.getSelectedItemPosition() + 1)));
      e.apply();
      setResult(RESULT_OK);
      finish();
      return true;
    } else if (item.getItemId() == R.id.action_clear) {
      fillDefault();
    }
    return super.onOptionsItemSelected(item);
  }

}
