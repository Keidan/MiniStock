package fr.ralala.ministock.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.util.List;

import fr.ralala.ministock.MainApplication;
import fr.ralala.ministock.R;
import fr.ralala.ministock.models.ShoppingCartEntry;
import fr.ralala.ministock.mysql.MySQLService;
import fr.ralala.ministock.ui.adapters.AdapterCartItems;
import fr.ralala.ministock.ui.utils.AppPermissions;
import fr.ralala.ministock.ui.utils.SwipeEditDeleteRecyclerViewItem;
import fr.ralala.ministock.ui.utils.UIHelper;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Main activity.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class MainActivity extends AppCompatActivity implements SwipeEditDeleteRecyclerViewItem.SwipeEditDeleteRecyclerViewItemListener {
  private static final int BACK_TIME_DELAY = 2000;
  private static long mLastBackPressed = -1;
  private AlertDialog mProgress;
  private MainApplication mApp;
  private AdapterCartItems mAdapter;
  private boolean listInProgress = false;
  private RecyclerView mRecyclerView;
  private SwipeRefreshLayout mListViewContainer;
  private SwipeRefreshLayout mEmptyViewContainer;

  /**
   * Called when the activity is created.
   *
   * @param savedInstanceState The saved instance state.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mApp = (MainApplication) getApplication();
    mApp.getDb().setActivity(this);

    mProgress = UIHelper.showCircularProgressDialog(this);

    /* permissions */
    if (!AppPermissions.checkPermissions(this)) {
      AppPermissions.shouldShowRequest(this);
    }

    // SwipeRefreshLayout
    mListViewContainer = findViewById(R.id.swipeRefreshLayout_listView);
    mEmptyViewContainer = findViewById(R.id.swipeRefreshLayout_emptyView);
    // Configure SwipeRefreshLayout
    onCreateSwipeToRefresh(mListViewContainer);
    onCreateSwipeToRefresh(mEmptyViewContainer);

    mRecyclerView = findViewById(R.id.list);
    mRecyclerView.setHasFixedSize(true);
    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
    mRecyclerView.setLayoutManager(layoutManager);
    mRecyclerView.getRecycledViewPool().clear();
    new SwipeEditDeleteRecyclerViewItem(this, mRecyclerView, this);
  }

  private void onCreateSwipeToRefresh(SwipeRefreshLayout refreshLayout) {
    refreshLayout.setOnRefreshListener(this::refresh);
    refreshLayout.setColorSchemeResources(
        android.R.color.holo_blue_light,
        android.R.color.holo_orange_light,
        android.R.color.holo_green_light,
        android.R.color.holo_red_light);
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
    inflater.inflate(R.menu.activity_main, menu);
    return true;
  }

  /**
   * Callback for the result from requesting permissions.
   *
   * @param requestCode  The request code passed in requestPermissions(android.app.Activity, String[], int).
   * @param permissions  The requested permissions. Never null.
   * @param grantResults The grant results for the corresponding permissions which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
    switch (requestCode) {
      case AppPermissions.PERMISSIONS_REQUEST: {
        if (!AppPermissions.onRequestPermissionsResult(this, permissions, grantResults)) {
          UIHelper.showAlertDialog(this, R.string.error, getString(R.string.error_permissions));
          finish();
        }
        break;
      }
    }
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
    /*if(requestCode == CartItemActivity.REQUEST_START_ACTIVITY)
      refresh();
    else*/
    // the event must be propagated to fragments
    super.onActivityResult(requestCode, resultCode, data);
  }


  /**
   * Called when a ViewHolder is swiped from left to right by the user.
   *
   * @param adapterPosition The position in the adapter.
   */
  @Override
  public void onClickEdit(int adapterPosition) {
    ShoppingCartEntry entry = mAdapter.getItem(adapterPosition);
    if (entry == null) return;
    CartItemActivity.startActivity(this, entry);
  }

  /**
   * Called when a ViewHolder is swiped from right to left by the user.
   *
   * @param adapterPosition The position in the adapter.
   */
  @Override
  public void onClickDelete(int adapterPosition) {
    final ShoppingCartEntry entry = mAdapter.getItem(adapterPosition);
    if (entry == null) return;
    UIHelper.showConfirmDialog(this,
        (getString(R.string.confirm_delete_entry) + " '" + entry.getTitle() + "'" + getString(R.string.help)),
        (v) ->
            mApp.getDb().delete(this, entry, (requestId, data) -> {
              mAdapter.removeItem(entry);
              refresh();
            } , (action, code, description) ->
                    UIHelper.showAlertDialog(this, R.string.error,
                        getString(R.string.error_delete_entry) + " : (" + code + ") " + description)
            )
    );

  }

  /**
   * Called when the activity is destroyed.
   */
  @Override
  public void onDestroy() {
    super.onDestroy();
    progressHide();
  }

  /**
   * Hide the progress dialog.
   */
  public void progressHide() {
    if (mProgress.isShowing())
      mProgress.dismiss();
  }

  /**
   * Show the progress dialog.
   */
  public void progressShow() {
    if (!listInProgress && !mProgress.isShowing()) {
      mProgress.show();
      Window window = mProgress.getWindow();
      if (window != null) {
        window.setLayout(350, 350);
        View v = window.getDecorView();
        v.setBackgroundResource(R.drawable.rounded_border);
      }
    }
  }

  /**
   * Called when the fragment is resumed.
   */
  @Override
  public void onResume() {
    startService(new Intent(this, MySQLService.class));
    MainApplication.registerActivityBroadcast(this, mApp.getDb());
    super.onResume();
    new Handler().postDelayed(() -> {
      if (mAdapter != null && mAdapter.getItemCount() != 0) {
        mListViewContainer.setRefreshing(true);
      } else
        mEmptyViewContainer.setRefreshing(true);
      refresh();
    }, 500);
  }

  /**
   * Functions called to refresh the list.
   */
  private void refresh() {
    if (!listInProgress) {
      listInProgress = true;
      mApp.getDb().list(this, (requestId, data) -> {
        mListViewContainer.setRefreshing(false);
        mEmptyViewContainer.setRefreshing(false);
        @SuppressWarnings("unchecked")
        List<ShoppingCartEntry> li = (List<ShoppingCartEntry>) data;
        mAdapter = new AdapterCartItems(mRecyclerView, li);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.safeNotifyDataSetChanged();
        listInProgress = false;
        if (mAdapter.getItemCount() != 0)
          mEmptyViewContainer.setVisibility(View.GONE);
        else
          mEmptyViewContainer.setVisibility(View.VISIBLE);
      }, (action, code, description) -> {
        mListViewContainer.setRefreshing(false);
        mEmptyViewContainer.setRefreshing(false);
        UIHelper.showAlertDialog(this, R.string.error,
            getString(R.string.error_list_entries) + " : (" + code + ") " + description);
        listInProgress = false;
        if (mAdapter.getItemCount() != 0)
          mEmptyViewContainer.setVisibility(View.GONE);
        else
          mEmptyViewContainer.setVisibility(View.VISIBLE);
      });
    }
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
   * Called to handle the click on the back button.
   */
  @Override
  public void onBackPressed() {
    if (mLastBackPressed + BACK_TIME_DELAY > System.currentTimeMillis()) {
      super.onBackPressed();
      return;
    } else {
      Toast.makeText(this, R.string.on_double_back_exit_text, Toast.LENGTH_SHORT).show();
    }
    mLastBackPressed = System.currentTimeMillis();
  }

  /**
   * Called when a menu is selected.
   *
   * @param item The selected item.
   * @return boolean
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_add:
        CartItemActivity.startActivity(this, null);
        break;
      case R.id.action_settings:
        startActivity(new Intent(this, SettingsActivity.class));
        break;
    }
    return true;
  }
}
