package fr.ralala.ministock.ui.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import fr.ralala.ministock.R;
import fr.ralala.ministock.ui.fragments.SettingsFragment;

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

  /**
   * Called when the activity is created.
   *
   * @param savedInstanceState The saved instance state.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_settings);

    SettingsFragment prefs = new SettingsFragment();

    getSupportFragmentManager()
      .beginTransaction()
      .replace(R.id.settings_container, prefs)
      .commit();

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  /**
   * Called when the options item is clicked (home).
   *
   * @param item The selected menu.
   * @return boolean
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
