package fr.ralala.ministock.update;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Updater file.
 * </p>
 *
 * @author Keidan
 * ******************************************************************************
 */
public class UpdaterFile {
  private int mMajor = 0;
  private int mMinor = 0;
  private String mUrl = "";
  private String mFilename = "";
  private boolean mError = false;
  private String mDestFile = "";
  private long mSize = 0;

  public long getSize() {
    return mSize;
  }

  public void setSize(long size) {
    mSize = size;
  }

  public String getDestFile() {
    return mDestFile;
  }

  public void setDestFile(String destFile) {
    mDestFile = destFile;
  }

  public boolean isError() {
    return mError;
  }

  public void setError(boolean error) {
    mError = error;
  }

  public int getMajor() {
    return mMajor;
  }

  public void setMajor(int major) {
    mMajor = major;
  }

  public int getMinor() {
    return mMinor;
  }

  public void setMinor(int minor) {
    mMinor = minor;
  }

  public String getUrl() {
    return mUrl;
  }

  public void setUrl(String url) {
    mUrl = url;
  }

  public String getFilename() {
    return mFilename;
  }

  public void setFilename(String filename) {
    mFilename = filename;
  }
}
