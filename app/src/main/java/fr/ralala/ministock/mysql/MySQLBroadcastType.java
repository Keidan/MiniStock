package fr.ralala.ministock.mysql;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Broadcast message type.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public enum MySQLBroadcastType {
  EXIT, /* Internal service */
  SHOW_PROGRESS,  /* S2A */
  SEND,  /* A2S */
  SOCKET_ERROR, /* S2A */
  READ, /* S2A */
}
