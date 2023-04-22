package fr.ralala.ministock.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fr.ralala.ministock.ApplicationCtx;
import fr.ralala.ministock.R;
import fr.ralala.ministock.db.DBService;
import fr.ralala.ministock.db.models.CartEntry;
import fr.ralala.ministock.db.models.CartItem;
import fr.ralala.ministock.ui.adapters.AdapterCartItems;
import fr.ralala.ministock.ui.launchers.LauncherPickPhoto;
import fr.ralala.ministock.ui.launchers.LauncherScanActivity;
import fr.ralala.ministock.ui.utils.UIHelper;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Cart item activity.
 * </p>
 *
 * @author Keidan
 * ******************************************************************************
 */
public class CartItemActivity extends AppCompatActivity implements AdapterCartItems.OnClick {
  public static final int LOGO_WIDTH = 320;
  public static final int LOGO_HEIGHT = 320;
  private static final String ITEM = "item";
  public static final String EXTRA_ENTRY = "exEntry";
  private CartEntry mEntry;
  private ImageButton mIbLogo;
  private TextInputEditText mTietTitle;
  private TextInputLayout mTilTitle;
  private ApplicationCtx mApp;
  private LauncherScanActivity mLauncherScanActivity;
  private AdapterCartItems mItemsAdapter;

  /**
   * Starts an activity.
   *
   * @param appActivityCompat The Android AppCompatActivity.
   * @param entry             The entry.
   */
  public static void startActivity(final AppCompatActivity appActivityCompat, final CartEntry entry) {
    Intent intent = new Intent(appActivityCompat, CartItemActivity.class);
    intent.putExtra(EXTRA_ENTRY, entry);
    appActivityCompat.startActivity(intent);
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
    ActionBar actionBar = getDelegate().getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
    mApp = (ApplicationCtx) getApplication();
    mLauncherScanActivity = new LauncherScanActivity(this);
    LauncherPickPhoto launcherPickPhoto = new LauncherPickPhoto(this);

    mIbLogo = findViewById(R.id.ibLogo);
    mTietTitle = findViewById(R.id.tietTitle);
    AppCompatTextView tvItemsCount = findViewById(R.id.tvItemsCount);
    mTilTitle = findViewById(R.id.tilTitle);
    AppCompatImageView ivAdd = findViewById(R.id.ivAdd);
    ListView lv = findViewById(R.id.lvItems);

    mItemsAdapter = new AdapterCartItems(this, new ArrayList<>(), this);
    lv.setOnItemLongClickListener((parent, view, position, id) -> {
      UIHelper.showConfirmDialog(this, getString(R.string.confirm_delete_item),
        v -> {
          mItemsAdapter.remove(position);
          tvItemsCount.setText(String.valueOf(mItemsAdapter.getCount()));
        });
      return true;
    });
    lv.setAdapter(mItemsAdapter);
    ivAdd.setOnClickListener(v -> {
      mItemsAdapter.add(new CartItem("", CartItem.getDate()));
      tvItemsCount.setText(String.valueOf(mItemsAdapter.getCount()));
    });

    if (getIntent().getAction() == null && getIntent().getExtras() != null) {
      Bundle extras = getIntent().getExtras();
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
        mEntry = extras.getSerializable(EXTRA_ENTRY, CartEntry.class);
      else
        mEntry = (CartEntry) extras.getSerializable(EXTRA_ENTRY);
      if (mEntry != null) {
        mIbLogo.setImageBitmap(mEntry.getImage());
        mTietTitle.setText(mEntry.getTitle());
        mItemsAdapter.addAll(mEntry.getItems());
      }
    } else
      mEntry = null;

    mIbLogo.setOnClickListener(v -> launcherPickPhoto.start());

    if (savedInstanceState != null) {
      CartEntry ce;
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
        ce = savedInstanceState.getSerializable(ITEM, CartEntry.class);
      else
        ce = (CartEntry) savedInstanceState.getSerializable(ITEM);
      mTietTitle.setText(ce.getTitle());
      mIbLogo.setImageBitmap(ce.getImage());
      mItemsAdapter.addAll(ce.getItems());
    }
    tvItemsCount.setText(String.valueOf(mItemsAdapter.getCount()));
    setResult(RESULT_CANCELED);
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
    super.onSaveInstanceState(savedInstanceState);
    CartEntry ce = new CartEntry(this);
    ce.setImage((Bitmap) UIHelper.imageViewToParcelable(mIbLogo));
    ce.setTitle(Objects.requireNonNull(mTietTitle.getText()).toString());
    ce.getItems().addAll(mItemsAdapter.getItems());
    savedInstanceState.putSerializable(ITEM, ce);
  }

  /**
   * Called when the fragment is resumed.
   */
  @Override
  public void onResume() {
    startService(new Intent(this, DBService.class));
    ApplicationCtx.registerActivityBroadcast(this, mApp.getDb());
    super.onResume();
  }

  /**
   * Called when the fragment is paused.
   */
  @Override
  public void onPause() {
    super.onPause();
    stopService(new Intent(this, DBService.class));
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

  public void setImage(Bitmap bm) {
    mIbLogo.setImageBitmap(bm);
  }

  /**
   * Sets the Qr code data.
   *
   * @param data Data
   */
  public void setQrCode(CartItem ci, int position, String data) {
    ci.setQrCodeId(data);
    mItemsAdapter.getItems().get(position).setQrCodeId(data);
    mItemsAdapter.notifyDataSetChanged();
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
      if (TextUtils.isEmpty(mTietTitle.getText())) {
        mTilTitle.setError(getString(R.string.error_invalid_title));
        return true;
      }
      String title = mTietTitle.getText().toString();
      mApp.getDb().find(this, title, (requestId, data) -> {
          @SuppressWarnings("unchecked")
          List<CartEntry> li = (List<CartEntry>) data;
          if (li.isEmpty() || (mEntry != null && li.get(0).getID().equals(mEntry.getID()))) {
            insert(title,
              UIHelper.drawableToBitmap(mIbLogo.getDrawable()),
              mItemsAdapter.getItems());
          } else
            UIHelper.showAlertDialog(this, R.string.error,
              getString(R.string.error_item_already_exists));
        }, (action, code, description) ->
          UIHelper.showAlertDialog(this, R.string.error,
            getString(R.string.db_error) + " : (" + code + ") " + description)
      );
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void insert(final String title, final Bitmap image, List<CartItem> items) {
    CartEntry entry = mEntry;
    if (entry == null)
      entry = new CartEntry(this);
    entry.setTitle(title);
    entry.setImage(image);
    entry.getItems().clear();
    entry.getItems().addAll(items);
    if (mEntry == null) {
      mApp.getDb().insert(this, entry, (requestId, data) -> {
          setResult(RESULT_OK);
          finish();
        }, (action, code, description) ->
          UIHelper.showAlertDialog(this, R.string.error,
            getString(R.string.db_error) + " : (" + code + ") " + description)
      );
    } else {
      mApp.getDb().update(this, entry, (requestId, data) -> {
          setResult(RESULT_OK);
          finish();
        }, (action, code, description) ->
          UIHelper.showAlertDialog(this, R.string.error,
            getString(R.string.db_error) + " : (" + code + ") " + description)
      );
    }
  }

  @Override
  public void onClick(CartItem ci, int position) {
    mLauncherScanActivity.start(ci, position);
  }

}
