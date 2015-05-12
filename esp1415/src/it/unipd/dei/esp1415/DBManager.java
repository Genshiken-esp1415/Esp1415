package it.unipd.dei.esp1415;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Comment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Questo metodo incapsula l'interazione col database in modo da non dover
 * sporcare di opendb, rawquery, execsql, eccetera le activity. Conterrà metodi
 * per l'inserimento di dati e il reperimento di dati in forma di array,
 * tabelle, eccetera.
 * 
 * @author Andrea
 *
 */
public class DBManager {
	  // Database fields
	  private SQLiteDatabase database;
	  private DBOpenHelper dbHelper;
	  private String[] SessionColumns = {	DBOpenHelper.COLUMN_TIMESTAMP_S,
			  								DBOpenHelper.COLUMN_NAME,
			  								DBOpenHelper.COLUMN_DURATA,
			  								DBOpenHelper.COLUMN_ATTIVA};
	  private String[] FallColumns = {	DBOpenHelper.COLUMN_TIMESTAMP_F,
										DBOpenHelper.COLUMN_NUMBER,
										DBOpenHelper.COLUMN_NOTIFIED,
										DBOpenHelper.COLUMN_LATITUDE,
										DBOpenHelper.COLUMN_LONGITUDE,
										DBOpenHelper.COLUMN_SESSION};	 
	  private String[] AccelerometerColumns = {	DBOpenHelper.COLUMN_SAMPLENUMBER,
												DBOpenHelper.COLUMN_X,
												DBOpenHelper.COLUMN_Y,
												DBOpenHelper.COLUMN_Z,
												DBOpenHelper.COLUMN_FALL};  
	  

	  public DBManager(Context context) {
	    dbHelper = new DBOpenHelper(context);
	  }

	  public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	  }

	  public void close() {
	    dbHelper.close();
	  }

	  /**
	   * Utilizzato quando si crea una nuova sessione. La sessione viene creata, inserita nel db e restituita al chiamante.
	   * @param sessionName Il nome della sessione che si vuole creare.
	   * @return
	   */
	  public Session createSession(String sessionName) {
	    ContentValues values = new ContentValues();
	    values.put(DBOpenHelper.COLUMN_NAME, sessionName);
	    Long insertId = database.insert(DBOpenHelper.TABLE_SESSION, null, values);
	    Cursor cursor = database.query(DBOpenHelper.TABLE_SESSION,
	        SessionColumns, null, null,
	        null, null, DBOpenHelper.COLUMN_TIMESTAMP_S + " DESC");
	    cursor.moveToFirst();
	    Session newSession = cursorToSession(cursor);
	    cursor.close();
	    return newSession;
	  }
	  
	  /**
	   * chiamare questo metodo per rinominare la sessione.
	   * @param session la sessione che si vuole rinominare
	   * @return niente, viene modificata la sessione passata come parametro
	   */
	  public Session renameSession(Session session) {
	    ContentValues values = new ContentValues();
	    values.put(DBOpenHelper.COLUMN_NAME, session.getName());
	    String whereClause = DBOpenHelper.COLUMN_TIMESTAMP_S +" = ?";
	    String[] whereArgs = new String[1];
	    whereArgs[0] =  dateToSqlDate(session.getSessionBegin());
	    int updateId = database.update(DBOpenHelper.TABLE_SESSION, values, whereClause, whereArgs);
	    Cursor cursor = database.query(DBOpenHelper.TABLE_SESSION,
		        SessionColumns, whereClause, whereArgs,
		        null, null, null);
	    cursor.moveToFirst();
	    session = cursorToSession(cursor);
	    cursor.close();
	    return session;
	  }

	  /** 
	   * Cancella una sessione presente nel db, data la sessione da cancellare
	   * @param session
	   */
	  public void deleteSession(Session session) {
	    Date timestamp = session.getSessionBegin();
	    System.out.println("Comment deleted with timestamp: " + timestamp);
	    database.delete(DBOpenHelper.TABLE_SESSION, DBOpenHelper.COLUMN_TIMESTAMP_S
	        + " = " + dateToSqlDate(timestamp), null);
	  }

	  /**
	   * restituisce una lista contenente tutte le sessioni presenti nella tabella SESSIONE
	   * @return
	   */
	  public List<Session> getAllSessions() {
	    List<Session> sessions = new ArrayList<Session>();

	    Cursor cursor = database.query(DBOpenHelper.TABLE_SESSION,
	        FallColumns, null, null, null, null, null);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      Session session = cursorToSession(cursor);
	      sessions.add(session);
	      cursor.moveToNext();
	    }
	    // make sure to close the cursor
	    cursor.close();
	    return sessions;
	  }

	  /**
	   * Restituisce una sessione Session a partire da un cursor contenente dati di una riga di una query sql
	   * @param cursor
	   * @return
	   */
	  private Session cursorToSession(Cursor cursor) {
	    Session session = new Session.SessionBuilder(sqlDateToDate(cursor.getString(0)))
	    								.name(cursor.getString(1))
	    								.duration(cursor.getInt(2))
	    								.active(cursor.getInt(3)==0)
	    								.build();	    							
	    return session;
	  }
	  
	  /**
	   * Usato per convertire una stringa sql contenente una data in una data Date
	   * @param sqlDate
	   * @return
	   */
	  private Date sqlDateToDate(String sqlDate){
		  SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		  try {
			  Date date = dateFormat.parse(sqlDate);
			  return date;
		  }
		  catch (ParseException e) {		  
		  }
		  return null;
	  }
	  
	  /**
	   * Usato per convertire una data Date in una stringa contenente una data riconoscibile da sql
	   * @param date
	   * @return
	   */
	  private String dateToSqlDate(Date date){
		  SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		  String sqlDate = sqlDateFormat.format(date);
		  return sqlDate;
	  }
}
