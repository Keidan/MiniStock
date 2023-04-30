package fr.ralala.ministock.db;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Broadcast message type.
 * </p>
 *
 * @author Keidan
 * ******************************************************************************
 */
public enum DBBroadcastType {
  EXIT, /* Internal service */
  SEND,  /* A2S */
  SOCKET_ERROR, /* S2A */
  READ, /* S2A */
}
