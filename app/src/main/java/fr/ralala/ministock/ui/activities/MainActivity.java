package fr.ralala.ministock.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.ralala.ministock.ApplicationCtx;
import fr.ralala.ministock.R;
import fr.ralala.ministock.db.DBService;
import fr.ralala.ministock.models.CartEntry;
import fr.ralala.ministock.ui.adapters.AdapterCartEntries;
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
 * ******************************************************************************
 */
public class MainActivity extends AppCompatActivity implements SwipeEditDeleteRecyclerViewItem.SwipeEditDeleteRecyclerViewItemListener {
  private static final int BACK_TIME_DELAY = 2000;
  private long mLastBackPressed = -1;
  private ApplicationCtx mApp;
  private AdapterCartEntries mAdapter;
  private final AtomicBoolean mListInProgress = new AtomicBoolean(false);
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
    mApp = (ApplicationCtx) getApplication();
    mApp.getDb().setActivity(this);

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
   * Called when a ViewHolder is swiped from left to right by the user.
   *
   * @param adapterPosition The position in the adapter.
   */
  @Override
  public void onClickEdit(int adapterPosition) {
    if (adapterPosition < 0 || adapterPosition >= mAdapter.getItemCount())
      return;
    CartEntry entry = mAdapter.getItem(adapterPosition);
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
    final CartEntry entry = mAdapter.getItem(adapterPosition);
    if (entry == null) return;
    UIHelper.showConfirmDialog(this,
      (getString(R.string.confirm_delete_entry) + " '" + entry.getTitle() + "'" + getString(R.string.help)),
      v ->
        mApp.getDb().delete(this, entry, (requestId, data) -> {
            mAdapter.removeItem(entry);
            refresh();
          }, (action, code, description) ->
            UIHelper.showAlertDialog(this, R.string.error,
              getString(R.string.error_delete_entry) + " : (" + code + ") " + description)
        )
    );

  }

  /**
   * Called when the fragment is resumed.
   */
  @Override
  public void onResume() {
    startService(new Intent(this, DBService.class));
    ApplicationCtx.registerActivityBroadcast(this, mApp.getDb());
    super.onResume();
    new Handler(Looper.getMainLooper()).postDelayed(() -> {
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
    mListInProgress.set(true);
    mApp.getDb().list(this, (requestId, data) -> {
      mListViewContainer.setRefreshing(false);
      mEmptyViewContainer.setRefreshing(false);
      @SuppressWarnings("unchecked")
      List<CartEntry> li = (List<CartEntry>) data;
      mAdapter = new AdapterCartEntries(mRecyclerView, li);
      mRecyclerView.setAdapter(mAdapter);
      mAdapter.safeNotifyDataSetChanged();
      mListInProgress.set(false);
      if (mAdapter.getItemCount() != 0)
        mEmptyViewContainer.setVisibility(View.GONE);
      else
        mEmptyViewContainer.setVisibility(View.VISIBLE);
    }, (action, code, description) -> {
      mListViewContainer.setRefreshing(false);
      mEmptyViewContainer.setRefreshing(false);
      UIHelper.showAlertDialog(this, R.string.error,
        getString(R.string.error_list_entries) + " : (" + code + ") " + description);
      mListInProgress.set(false);
      if (mAdapter != null && mAdapter.getItemCount() != 0)
        mEmptyViewContainer.setVisibility(View.GONE);
      else
        mEmptyViewContainer.setVisibility(View.VISIBLE);
    });
  }

  /**
   * Called when the fragment is paused.
   */
  @Override
  public void onPause() {
    super.onPause();
    mListViewContainer.setRefreshing(false);
    mEmptyViewContainer.setRefreshing(false);
    stopService(new Intent(this, DBService.class));
    unregisterReceiver(mApp.getDb());
  }

  /**
   * Called to handle the click on the back button.
   *
   * @deprecated For the moment I continue with this.
   */
  @Deprecated
  @Override
  @SuppressWarnings("squid:S1133")
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
    if (item.getItemId() == R.id.action_add) {
      CartItemActivity.startActivity(this, null);
      return true;
    } else if (item.getItemId() == R.id.action_settings) {
      startActivity(new Intent(this, SettingsActivity.class));
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
