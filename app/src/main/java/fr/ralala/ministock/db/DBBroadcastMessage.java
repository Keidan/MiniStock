package fr.ralala.ministock.db;

import java.io.Serializable;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Broadcast message.
 * </p>
 *
 * @author Keidan
 * ******************************************************************************
 */
public class DBBroadcastMessage implements Serializable {
  private final DBBroadcastType mBroadcastType;
  private Throwable mThrowable;
  private String[] mData;

  /**
   * Generic message without parameters.
   *
   * @param broadcastType The message type.
   */
  public DBBroadcastMessage(DBBroadcastType broadcastType) {
    mBroadcastType = broadcastType;
  }

  /**
   * Error message.
   *
   * @param throwable The exception.
   */
  public DBBroadcastMessage(Throwable throwable) {
    this(DBBroadcastType.SOCKET_ERROR);
    mThrowable = throwable;
  }

  /**
   * RX/TX message.
   *
   * @param broadcastType Message type.
   * @param data          Message data.
   */
  public DBBroadcastMessage(DBBroadcastType broadcastType, String[] data) {
    this(broadcastType);
    mData = data;
  }


  /**
   * Returns the DBBroadcastType value.
   *
   * @return DBBroadcastType
   */
  public DBBroadcastType getBroadcastType() {
    return mBroadcastType;
  }

  /**
   * Returns the Throwable value (error only).
   *
   * @return Throwable
   */
  public Throwable getThrowable() {
    return mThrowable;
  }

  /**
   * Returns the data value.
   *
   * @return String[]
   */
  public String[] getData() {
    return mData;
  }

}
