package fr.ralala.ministock.ui.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import fr.ralala.ministock.ApplicationCtx;
import fr.ralala.ministock.R;
import fr.ralala.ministock.models.SettingsKeys;
import fr.ralala.ministock.ui.utils.UIHelper;
import fr.ralala.ministock.update.CheckUpdate;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Settings fragments
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
  private EditTextPreference mPrefHost;
  private EditTextPreference mPrefPort;
  private EditTextPreference mPrefPage;
  private EditTextPreference mPrefUsername;
  private EditTextPreference mPrefPassword;

  /**
   * Called during onCreate(Bundle) to supply the preferences for this fragment.
   *
   * @param savedInstanceState If the fragment is being re-created from a previous saved state,
   *                           this is the state.
   * @param rootKey            If non-null, this preference fragment should be rooted at the
   *                           PreferenceScreen with this key.
   */
  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.preferences, rootKey);

    Preference update = findPreference(SettingsKeys.CFG_CHECK_UPDATE);
    ListPreference protocol = findPreference(SettingsKeys.CFG_PROTOCOL);
    mPrefHost = findPreference(SettingsKeys.CFG_HOST);
    mPrefPort = findPreference(SettingsKeys.CFG_PORT);
    mPrefPage = findPreference(SettingsKeys.CFG_PAGE);
    mPrefUsername = findPreference(SettingsKeys.CFG_USERNAME);
    mPrefPassword = findPreference(SettingsKeys.CFG_PASSWORD);

    mPrefHost.setOnPreferenceChangeListener(this);
    mPrefPort.setOnPreferenceChangeListener(this);
    mPrefPage.setOnPreferenceChangeListener(this);
    mPrefUsername.setOnPreferenceChangeListener(this);
    mPrefPassword.setOnPreferenceChangeListener(this);

    updateInputType(mPrefHost, InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
    updateInputType(mPrefPage, InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
    updateInputType(mPrefPort, InputType.TYPE_CLASS_NUMBER);
    updateInputType(mPrefUsername, InputType.TYPE_CLASS_TEXT);
    updateInputType(mPrefPassword, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

    ApplicationCtx app = (ApplicationCtx) requireActivity().getApplication();
    if (null != protocol)
      protocol.setDefaultValue(app.getProtocol());
    if (update != null)
      update.setOnPreferenceClickListener(p -> {
        new CheckUpdate(getContext(), false).execute(null);
        return true;
      });
  }

  private void updateInputType(EditTextPreference etp, int clazz) {
    etp.setOnBindEditTextListener(editText -> {
      editText.setInputType(clazz);
      editText.setSingleLine(true);
      if ((InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD) == clazz) {
        editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
      }
      editText.selectAll();
    });
  }

  /**
   * Called when a preference has been changed by the user.
   *
   * @param preference The changed preference.
   * @param newValue   The new value of the preference
   * @return true to update the state of the preference with the new value.
   */
  @Override
  public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
    String value = newValue.toString();
    int id = -1;
    if (preference.equals(mPrefHost)) {
      id = R.string.error_invalid_host;
    } else if (preference.equals(mPrefPort)) {
      id = R.string.error_invalid_port;
    } else if (preference.equals(mPrefPage)) {
      id = R.string.error_invalid_page;
    } else if (preference.equals(mPrefUsername)) {
      id = R.string.error_invalid_username;
    } else if (preference.equals(mPrefPassword)) {
      id = R.string.error_invalid_password;
    }

    if (-1 != id) {
      if (TextUtils.isEmpty(value)) {
        UIHelper.toast(preference.getContext(), id);
        return false;
      }
      return true;
    } else
      return false;
  }

}
