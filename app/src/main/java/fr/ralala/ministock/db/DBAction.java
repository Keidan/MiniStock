package fr.ralala.ministock.db;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * DB action type.
 * </p>
 *
 * @author Keidan
 * ******************************************************************************
 */
public enum DBAction {
  LIST,
  UPDATE_WITH_TITLE,
  UPDATE_WITH_ID,
  INSERT,
  DELETE,
  FIND;


  public static DBAction fromString(String s) {
    if (s.equals("" + DBAction.DELETE))
      return DBAction.DELETE;
    else if (s.equals("" + DBAction.INSERT))
      return DBAction.INSERT;
    else if (s.equals("" + DBAction.UPDATE_WITH_TITLE))
      return DBAction.UPDATE_WITH_TITLE;
    else if (s.equals("" + DBAction.UPDATE_WITH_ID))
      return DBAction.UPDATE_WITH_ID;
    else if (s.equals("" + DBAction.FIND))
      return DBAction.FIND;
    else
      return DBAction.LIST;
  }


  public static String toString(DBAction a) {
    return "" + a;
  }
}
