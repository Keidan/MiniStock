package fr.ralala.ministock.ui.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.text.Collator;
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
import fr.ralala.ministock.update.CheckUpdate;

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
  private Collator mCollator;
  private MenuItem mSearchMenu = null;
  private SearchView mSearchView = null;
  private String mPrevSearch = "";

  /**
   * Called when the activity is created.
   *
   * @param savedInstanceState The saved instance state.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        back();
      }
    });
    mCollator = Collator.getInstance();
    mCollator.setStrength(Collator.PRIMARY);
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

    if (mApp.isCheckUpdateStartup())
      new CheckUpdate(this, true).execute(null);
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
    mSearchMenu = menu.findItem(R.id.action_search);
    setSearchView(mSearchMenu);
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
    hideSearch();
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
    UIHelper.hideKeyboard(this);
    mApp.getDb().list(this, (requestId, data) -> {
      mListViewContainer.setRefreshing(false);
      mEmptyViewContainer.setRefreshing(false);
      @SuppressWarnings("unchecked")
      List<CartEntry> li = (List<CartEntry>) data;
      /* sort */
      li.sort((a, b) -> mCollator.compare(a.getTitle(), b.getTitle()));
      mAdapter = new AdapterCartEntries(mRecyclerView, li);
      mAdapter.setFilteredList(mAdapter.apply(mPrevSearch));
      mRecyclerView.setAdapter(mAdapter);
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
   */
  public void back() {
    hideSearch();
    if (mLastBackPressed + BACK_TIME_DELAY > System.currentTimeMillis()) {
      finish();
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
      hideSearch();
      CartItemActivity.startActivity(this, null);
      return true;
    } else if (item.getItemId() == R.id.action_settings) {
      hideSearch();
      startActivity(new Intent(this, SettingsActivity.class));
      return true;
    }
    return super.onOptionsItemSelected(item);
  }


  /* ---------------------------- */
  /* Search */
  /* ---------------------------- */
  private void doSearch(String str) {
    mPrevSearch = str;
    mAdapter.getFilter().filter(str);
  }

  private void hideSearch() {
    if (mSearchView != null && mSearchMenu != null) {
      if (!mSearchView.isIconified()) {
        doSearch("");
        mSearchView.onActionViewCollapsed();
      }
      mSearchView.clearFocus();
      UIHelper.hideKeyboard(this);
    }
  }

  private void setSearchView(MenuItem si) {
    // Searchable configuration
    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
    if (searchManager != null) {
      mSearchView = (SearchView) si.getActionView();
      mSearchView.setSearchableInfo(searchManager
        .getSearchableInfo(getComponentName()));
      mSearchView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
      mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
          return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
          doSearch(s);
          return true;
        }
      });
    }
  }
}
