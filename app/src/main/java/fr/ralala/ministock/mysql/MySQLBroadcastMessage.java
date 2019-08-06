package fr.ralala.ministock.mysql;

import java.io.Serializable;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Broadcast message.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class MySQLBroadcastMessage implements Serializable {
  private MySQLBroadcastType mBroadcastType;
  private Throwable mThrowable;
  private String[] mData;

  /**
   * Generic message without parameters.
   *
   * @param broadcastType The message type.
   */
  public MySQLBroadcastMessage(MySQLBroadcastType broadcastType) {
    mBroadcastType = broadcastType;
  }

  /**
   * Error message.
   *
   * @param throwable The exception.
   */
  public MySQLBroadcastMessage(Throwable throwable) {
    this(MySQLBroadcastType.SOCKET_ERROR);
    mThrowable = throwable;
  }

  /**
   * RX/TX message.
   *
   * @param broadcastType Message type.
   * @param data          Message data.
   */
  public MySQLBroadcastMessage(MySQLBroadcastType broadcastType, String[] data) {
    this(broadcastType);
    mData = data;
  }


  /**
   * Returns the MySQLBroadcastType value.
   *
   * @return MySQLBroadcastType
   */
  public MySQLBroadcastType getBroadcastType() {
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
