package fr.ralala.ministock.update;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import fr.ralala.ministock.BuildConfig;
import fr.ralala.ministock.R;
import fr.ralala.ministock.task.TaskRunner;
import fr.ralala.ministock.ui.utils.UIHelper;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Checks APK updates.
 * </p>
 *
 * @author Keidan
 * ******************************************************************************
 */
public class CheckUpdate extends TaskRunner<Void, Void, Void, UpdaterFile> {
  private static final String EXCEPTION = "Exception: ";
  private static final String URL = "https://api.github.com/repos/keidan/MiniStock/releases/latest";
  private static final String REST_VERSION = "2022-11-28";
  private static final String TAG_TAG_NAME = "tag_name";
  private static final String TAG_ASSETS = "assets";
  private static final String TAG_CONTENT_TYPE = "content_type";
  private static final String TAG_BROWSER_URL = "browser_download_url";
  private static final String TAG_CONTENT_TYPE_APK = "application/vnd.android.package-archive";
  private static final String TAG_SIZE = "size";
  private final Context mContext;
  private final boolean mFromStart;

  public CheckUpdate(Context ctx, boolean fromStart) {
    mContext = ctx;
    mFromStart = fromStart;
  }

  /**
   * Called before the execution of the task.
   *
   * @return The Config.
   */
  @Override
  public Void onPreExecute() {
    if (!mFromStart)
      UIHelper.toastShort(mContext, R.string.update_search);
    return null;
  }

  /**
   * Performs a computation on a background thread.
   *
   * @param config Unused.
   * @param param  Unused.
   * @return A result, defined by the subclass of this task.
   */
  @Nullable
  @Override
  public UpdaterFile doInBackground(@Nullable Void config, @Nullable Void param) {
    UpdaterFile uf = null;
    try {
      URL url = new URL(URL);
      HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
      connection.setRequestProperty("Accept", "application/vnd.github+json");
      connection.setRequestProperty("X-GitHub-Api-Version", REST_VERSION);
      connection.setRequestMethod("GET");
      connection.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
      StringBuilder resp = new StringBuilder();
      try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
        String responseLine;
        while ((responseLine = br.readLine()) != null) {
          resp.append(responseLine.trim());
        }
      }
      JSONObject obj = new JSONObject(resp.toString());
      AtomicInteger major = new AtomicInteger(0);
      AtomicInteger minor = new AtomicInteger(0);
      if (obj.has(TAG_TAG_NAME)) {
        extractVersion(obj.getString(TAG_TAG_NAME), major, minor);
      }
      if (obj.has(TAG_ASSETS)) {
        JSONArray arr = obj.getJSONArray(TAG_ASSETS);
        for (int i = 0; i < arr.length(); i++) {
          JSONObject o = arr.getJSONObject(i);

          if (o.has(TAG_CONTENT_TYPE) && o.has(TAG_BROWSER_URL) && o.has(TAG_SIZE) && o.getString(TAG_CONTENT_TYPE).equals(TAG_CONTENT_TYPE_APK)) {
            uf = new UpdaterFile();
            uf.setMajor(major.get());
            uf.setMinor(minor.get());
            uf.setUrl(o.getString(TAG_BROWSER_URL));
            uf.setSize(o.getLong(TAG_SIZE));
            uf.setFilename(uf.getUrl().substring(uf.getUrl().lastIndexOf('/') + 1));
          }
        }
      }
    } catch (IOException | JSONException e) {
      Log.e(getClass().getSimpleName(), EXCEPTION + e.getMessage(), e);
    }
    return uf;
  }

  private void extractVersion(@NonNull String tagName, @NonNull AtomicInteger major, @NonNull AtomicInteger minor) {
    Pattern p = Pattern.compile("v(\\d+)\\.(\\d+)");
    Matcher m = p.matcher(tagName);
    major.set(0);
    minor.set(0);
    if (m.find()) {
      MatchResult mr = m.toMatchResult();
      try {
        major.set(Integer.parseInt(mr.group(1)));
        minor.set(Integer.parseInt(mr.group(2)));
      } catch (NumberFormatException e) {
        Log.e(getClass().getSimpleName(), EXCEPTION + e.getMessage(), e);
      }
    }
  }

  /**
   * Called after the execution of the task.
   *
   * @param result The result.
   */
  @Override
  public void onPostExecute(final UpdaterFile result) {
    if (result != null) {
      int localVersionCode = BuildConfig.VERSION_CODE;
      int remoteVersionCode = result.getMajor() * 100 + result.getMinor();
      if (remoteVersionCode > localVersionCode) {
        UIHelper.showConfirmDialog(mContext, mContext.getString(R.string.update_confirm),
          v -> new DownloadAPK(mContext).execute(result));
      } else if (!mFromStart)
        UIHelper.toastShort(mContext, R.string.no_update);
    }
  }
}
