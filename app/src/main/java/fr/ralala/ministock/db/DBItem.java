package fr.ralala.ministock.db;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * DB item
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class DBItem {
  private int mId;
  private String mProtocol;
  private String mHost;
  private int mPort;
  private String mPage;
  private String mUser;
  private String mPwd;
  private String mData;

  public int getId() {
    return mId;
  }

  public void setId(int id) {
    mId = id;
  }

  public String getProtocol() {
    return mProtocol;
  }

  public void setProtocol(String protocol) {
    mProtocol = protocol;
  }

  public String getHost() {
    return mHost;
  }

  public void setHost(String host) {
    mHost = host;
  }

  public int getPort() {
    return mPort;
  }

  public void setPort(int port) {
    mPort = port;
  }

  public String getPage() {
    return mPage;
  }

  public void setPage(String page) {
    mPage = page;
  }

  public String getUser() {
    return mUser;
  }

  public void setUser(String user) {
    mUser = user;
  }

  public String getPwd() {
    return mPwd;
  }

  public void setPwd(String pwd) {
    mPwd = pwd;
  }

  public String getData() {
    return mData;
  }

  public void setData(String data) {
    mData = data;
  }
}
