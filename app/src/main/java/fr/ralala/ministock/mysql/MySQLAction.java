package fr.ralala.ministock.mysql;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * MySQL action type.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public enum MySQLAction {
  LIST,
  UPDATE_WITH_TITLE,
  UPDATE_WITH_ID,
  INSERT,
  DELETE,
  FIND;


  public static MySQLAction fromString(String s) {
    if (s.equals("" + MySQLAction.DELETE))
      return MySQLAction.DELETE;
    else if (s.equals("" + MySQLAction.INSERT))
      return MySQLAction.INSERT;
    else if (s.equals("" + MySQLAction.UPDATE_WITH_TITLE))
      return MySQLAction.UPDATE_WITH_TITLE;
    else if (s.equals("" + MySQLAction.UPDATE_WITH_ID))
      return MySQLAction.UPDATE_WITH_ID;
    else if (s.equals("" + MySQLAction.FIND))
      return MySQLAction.FIND;
    else
      return MySQLAction.LIST;
  }


  public static String toString(MySQLAction a) {
    return "" + a;
  }
}
