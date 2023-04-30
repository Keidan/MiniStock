package fr.ralala.ministock.update;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import fr.ralala.ministock.R;
import fr.ralala.ministock.task.TaskRunner;
import fr.ralala.ministock.ui.utils.AppPermissions;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Downloads the APK.
 * </p>
 *
 * @author Keidan
 * ******************************************************************************
 */
public class DownloadAPK extends TaskRunner<Context, UpdaterFile, Double, UpdaterFile> {
  private static final int PROGRESS_MAX = 100;
  private static final String CHANNEL_ID = "ch_download_id";
  public static final String NOTIFY_CHANNEL = "ch_download_nfy";
  private static final int NFY_ID = 111;
  private static final String EXCEPTION = "Exception: ";
  private final Context mContext;
  private final DownloadManager mDownloadManager;
  private final NotificationManagerCompat mNotificationManager;
  private final NotificationCompat.Builder mBuilder;
  private int mPrevProgress = 0;

  public DownloadAPK(Context context) {
    mContext = context;
    mDownloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
    mNotificationManager = NotificationManagerCompat.from(mContext);
    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Channel",
      NotificationManager.IMPORTANCE_LOW);
    channel.setShowBadge(true);
    channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
    mNotificationManager.deleteNotificationChannel(CHANNEL_ID);
    mNotificationManager.createNotificationChannel(channel);
    mBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID);
    mBuilder.setContentTitle(mContext.getString(R.string.update_nfy_title))
      .setContentText(mContext.getString(R.string.update_nfy_in_progress))
      .setSmallIcon(R.mipmap.ic_launcher)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .setAutoCancel(true);
  }

  /**
   * Called before the execution of the task.
   *
   * @return The Config.
   */
  @Override
  public Context onPreExecute() {
    updateNfy(0, PROGRESS_MAX, 0);
    return mContext;
  }

  /**
   * Runs on the UI thread.
   *
   * @param value The value indicating progress.
   */
  @Override
  public void onProgressUpdate(Double value) {
    int progress = (int) value.doubleValue();
    if (progress != 0 && (progress % 10) == 0 && mPrevProgress != progress) {
      updateNfy(0, PROGRESS_MAX, progress);
      mPrevProgress = progress;
    }
  }

  private void copyStreams(InputStream is, FileOutputStream fos, long size) throws IOException {
    byte[] buffer = new byte[1024];
    int length;
    long total = 0;
    while ((length = is.read(buffer)) > 0) {
      fos.write(buffer, 0, length);
      total += length;
      publishProgress(((total * 100.0) / size));
    }
  }

  private void adjustFilename(Context ctx, UpdaterFile uf) {
    File rootDirectory = new File(ctx.getExternalFilesDir(null).getAbsoluteFile().toString());
    if (!rootDirectory.exists() && !rootDirectory.mkdirs()) {
      Log.i(getClass().getSimpleName(), "rootDirectory error");
    }
    uf.setDestFile(rootDirectory + "/" + uf.getFilename());
  }

  /**
   * Performs a computation on a background thread.
   *
   * @param ctx Android context.
   * @param uf  The updater file.
   * @return A result, defined by the subclass of this task.
   */
  @Nullable
  @Override
  public UpdaterFile doInBackground(@Nullable Context ctx, @Nullable UpdaterFile uf) {
    if (ctx != null && uf != null) {
      adjustFilename(ctx, uf);
      InputStream is = null;
      try (FileOutputStream fos = new FileOutputStream(uf.getDestFile())) {
        URL url = new URL(uf.getUrl());
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
        is = connection.getInputStream();
        copyStreams(is, fos, uf.getSize());
      } catch (IOException e) {
        Log.e(getClass().getSimpleName(), EXCEPTION + e.getMessage(), e);
        uf.setError(true);
      } finally {
        final String tag = getClass().getSimpleName();
        if (is != null)
          try {
            is.close();
          } catch (IOException e) {
            Log.e(tag, EXCEPTION + e.getMessage(), e);
          }
      }
      return uf;
    }
    return null;
  }


  /**
   * Called after the execution of the task.
   *
   * @param result The result.
   */
  @Override
  public void onPostExecute(final UpdaterFile result) {
    if (mDownloadManager != null && result != null && !result.isError()) {
      Intent intent = new Intent(Intent.ACTION_VIEW);
      Uri uri = FileProvider.getUriForFile(mContext,
        mContext.getApplicationContext().getPackageName() + ".provider", new File(result.getDestFile()));
      intent.setDataAndType(uri, "application/vnd.android.package-archive");
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

      updateNfy(R.string.update_nfy_done, 0, 0);
      updateNfy(intent);
      mContext.startActivity(intent);
    } else
      updateNfy(R.string.update_nfy_error, 0, 0);
  }

  private void updateNfy(int text, int max, int progress) {
    if (text != 0)
      mBuilder.setContentText(mContext.getString(text));
    mBuilder.setProgress(max, progress, false);
    if(AppPermissions.checkSelfPermissionPostNotifications(mContext))
      mNotificationManager.notify(NFY_ID, mBuilder.build());
  }

  private void updateNfy(Intent intent) {
    PendingIntent notifyPendingIntent = PendingIntent.getActivity(
      mContext, 0, intent,
      PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    mBuilder.setContentIntent(notifyPendingIntent);
    if(AppPermissions.checkSelfPermissionPostNotifications(mContext))
      mNotificationManager.notify(NFY_ID, mBuilder.build());
  }
}
