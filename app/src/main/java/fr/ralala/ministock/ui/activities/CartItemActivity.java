package fr.ralala.ministock.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import java.util.List;

import fr.ralala.ministock.MainApplication;
import fr.ralala.ministock.R;
import fr.ralala.ministock.models.ShoppingCartEntry;
import fr.ralala.ministock.mysql.MySQLService;
import fr.ralala.ministock.ui.utils.ImagePicker;
import fr.ralala.ministock.ui.utils.UIHelper;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Cart item activity.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class CartItemActivity extends AppCompatActivity implements View.OnClickListener {
  public static final int REQUEST_START_ACTIVITY = 200;
  private static final int PICK_IMAGE_ID = 234; // the number doesn't matter
  public static final String EXTRA_ENTRY = "exEntry";
  private ShoppingCartEntry mEntry;
  private ImageButton mIbLogo;
  private TextInputEditText mTietTitle;
  private TextInputEditText mTietCount;
  private TextInputEditText mTietQRCode;
  private AppCompatImageView mActvQRCode;
  private TextInputLayout mTilTitle;
  private MainApplication mApp;

  /**
   * Starts an activity.
   *
   * @param appActivityCompat The Android AppCompatActivity.
   * @param entry             The entry.
   */
  public static void startActivity(final AppCompatActivity appActivityCompat, final ShoppingCartEntry entry) {
    Intent intent = new Intent(appActivityCompat, CartItemActivity.class);
    intent.putExtra(EXTRA_ENTRY, entry);
    appActivityCompat.startActivityForResult(intent, REQUEST_START_ACTIVITY);
  }

  /**
   * Called when the activity is created.
   *
   * @param savedInstanceState The saved instance state.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_cart_item);
    android.support.v7.app.ActionBar actionBar = getDelegate().getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
    mApp = (MainApplication) getApplication();

    mIbLogo = findViewById(R.id.ibLogo);
    mTietTitle = findViewById(R.id.tietTitle);
    mTietCount = findViewById(R.id.tietCount);
    mTietQRCode = findViewById(R.id.tietQRCode);
    mActvQRCode = findViewById(R.id.actvQRCode);
    mTilTitle = findViewById(R.id.tilTitle);

    if (getIntent().getAction() == null && getIntent().getExtras() != null) {
      Bundle extras = getIntent().getExtras();
      mEntry = (ShoppingCartEntry) extras.getSerializable(EXTRA_ENTRY);
      if (mEntry != null) {
        mIbLogo.setImageBitmap(mEntry.getImage());
        mTietTitle.setText(mEntry.getTitle());
        mTietCount.setText(String.valueOf(mEntry.getCount()));
        mTietQRCode.setText(mEntry.getQrCodeId());
      }
    } else
      mEntry = null;

    mIbLogo.setOnClickListener(this);
    mActvQRCode.setOnClickListener(this);
  }

  /**
   * Called when the fragment is resumed.
   */
  @Override
  public void onResume() {
    startService(new Intent(this, MySQLService.class));
    MainApplication.registerActivityBroadcast(this, mApp.getDb());
    super.onResume();
  }

  /**
   * Called when the fragment is paused.
   */
  @Override
  public void onPause() {
    super.onPause();
    stopService(new Intent(this, MySQLService.class));
    unregisterReceiver(mApp.getDb());
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
    inflater.inflate(R.menu.cart_item, menu);
    return true;
  }

  /**
   * Called when the file chooser is disposed with a result.
   *
   * @param requestCode The request code.
   * @param resultCode  The result code.
   * @param data        The Intent data.
   */
  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    if (requestCode == ScanActivity.REQUEST_START_ACTIVITY) {
      if (resultCode == RESULT_OK) {
        mTietQRCode.setText(data.getStringExtra(ScanActivity.KEY_QRCODE));
      }
    } else if (requestCode == PICK_IMAGE_ID) {
      mIbLogo.setImageBitmap(ImagePicker.getImageFromResult(this, resultCode, data));
    } else
      super.onActivityResult(requestCode, resultCode, data);
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
      case R.id.action_validate:
        if (TextUtils.isEmpty(mTietCount.getText()))
          mTietCount.setText("0");
        if (TextUtils.isEmpty(mTietQRCode.getText()))
          mTietQRCode.setText("");
        if (TextUtils.isEmpty(mTietTitle.getText())) {
          mTilTitle.setError(getString(R.string.error_invalid_title));
          return true;
        }
        String title = mTietTitle.getText().toString();
        mApp.getDb().find(this, title, (requestId, data) -> {
              @SuppressWarnings("unchecked")
              List<ShoppingCartEntry> li = (List<ShoppingCartEntry>) data;
              if ((!li.isEmpty() && mEntry != null && li.get(0).getID().equals(mEntry.getID())) || li.isEmpty()) {
                insert(title,
                    mTietCount.getText().toString(), mTietQRCode.getText().toString(),
                    UIHelper.drawableToBitmap(mIbLogo.getDrawable()));
              } else
                UIHelper.showAlertDialog(this, R.string.error,
                    getString(R.string.error_item_already_exists));
            }, (action, code, description) ->
                UIHelper.showAlertDialog(this, R.string.error,
                    getString(R.string.sql_error) + " : (" + code + ") " + description)
        );
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void insert(final String title, final String count, final String qrcode, final Bitmap image) {
    ShoppingCartEntry entry = mEntry;
    if (entry == null)
      entry = new ShoppingCartEntry(this);
    entry.setCount(Integer.parseInt(count));
    entry.setQrCodeId(qrcode);
    entry.setTitle(title);
    entry.setImage(image);
    if (mEntry == null) {
      entry.setCreationDate(ShoppingCartEntry.getDate());
      entry.setModificationDate(ShoppingCartEntry.getDate());
      mApp.getDb().insert(this, entry, (requestId, data) -> {
            setResult(RESULT_OK);
            finish();
          }, (action, code, description) ->
              UIHelper.showAlertDialog(this, R.string.error,
                  getString(R.string.sql_error) + " : (" + code + ") " + description)
      );
    } else {
      entry.setModificationDate(ShoppingCartEntry.getDate());
      mApp.getDb().update(this, entry, (requestId, data) -> {
            setResult(RESULT_OK);
            finish();
          }, (action, code, description) ->
              UIHelper.showAlertDialog(this, R.string.error,
                  getString(R.string.sql_error) + " : (" + code + ") " + description)
      );
    }
  }

  /**
   * Called to handle the click on the back button.
   */
  @Override
  public void onBackPressed() {
    setResult(RESULT_CANCELED);
    super.onBackPressed();
  }

  @Override
  public void onClick(View v) {
    if (v.equals(mActvQRCode)) {
      startActivityForResult(new Intent(this, ScanActivity.class), ScanActivity.REQUEST_START_ACTIVITY);
    } else if (v.equals(mIbLogo)) {
      startActivityForResult(ImagePicker.getPickImageIntent(this), PICK_IMAGE_ID);
    }
  }


}
