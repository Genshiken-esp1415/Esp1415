package it.unipd.dei.esp1415;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Utilizzato per creare il database all'installazione dell'app o aggiornarlo
 * quando l'app viene modificata.
 * 
 */
public class DBOpenHelper extends SQLiteOpenHelper {
	// Variabili per la tabella SESSIONE
	public static final String TABLE_SESSION = "SESSION";
	public static final String COLUMN_TIMESTAMP_S = "timestamp_start";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_DURATION = "duration";
	public static final String COLUMN_ACTIVE = "active";
	public static final String COLUMN_ID_S = "ID";
	// Variabili per la tabella CADUTA
	public static final String TABLE_FALL = "FALL";
	public static final String COLUMN_NUMBER = "number";
	public static final String COLUMN_TIMESTAMP_F = "timestamp";
	public static final String COLUMN_NOTIFIED = "notified";
	public static final String COLUMN_LATITUDE = "latitude";
	public static final String COLUMN_LONGITUDE = "longitude";
	public static final String COLUMN_SESSION = "timestamp_start";
	public static final String COLUMN_ID_F = "ID";
	// Variabili per la tabella ACCELEROMETRO
	public static final String TABLE_ACCELEROMETER = "ACCELEROMETER";
	public static final String COLUMN_TIMESTAMP_A = "timestamp_accelerometer";
	public static final String COLUMN_X = "x";
	public static final String COLUMN_Y = "y";
	public static final String COLUMN_Z = "z";
	public static final String COLUMN_FALL = "timestamp";
	public static final String COLUMN_ID_A = "ID";

	private static final String DATABASE_NAME = "fallDetection.db";
	private static final int DATABASE_VERSION = 1;

	// Create table necessari
	private static final String DATABASE_CREATE_SESSION = "" + "CREATE TABLE "
			+ TABLE_SESSION + " ( " + COLUMN_TIMESTAMP_S
			+ " TEXT PRIMARY KEY DEFAULT(datetime('now','localtime')), "
			+ COLUMN_NAME + " TEXT NOT NULL, " + COLUMN_DURATION
			+ " INTEGER DEFAULT 0, " + COLUMN_ACTIVE + " INTEGER DEFAULT 0)";
	private static final String DATABASE_CREATE_FALL = "" + "CREATE TABLE "
			+ TABLE_FALL + " ( " + COLUMN_TIMESTAMP_F + " TEXT PRIMARY KEY , "
			+ COLUMN_NUMBER + " INTEGER, " + COLUMN_NOTIFIED + " INTEGER, "
			+ COLUMN_LATITUDE + " REAL, " + COLUMN_LONGITUDE + " REAL, "
			+ COLUMN_SESSION + " TEXT, " + "FOREIGN KEY(" + COLUMN_SESSION
			+ ") REFERENCES " + TABLE_SESSION + "(" + COLUMN_TIMESTAMP_S + "))";
	private static final String DATABASE_CREATE_ACCELEROMETER = ""
			+ "CREATE TABLE "
			+ TABLE_ACCELEROMETER
			+ " ( "
			+ COLUMN_TIMESTAMP_A
			+ " INTEGER, "
			+ COLUMN_X
			+ " REAL, "
			+ COLUMN_Y
			+ " REAL, "
			+ COLUMN_Z
			+ " REAL, "
			+ COLUMN_FALL
			+ " TEXT, "
			+ "PRIMARY KEY ("
			+ COLUMN_TIMESTAMP_A
			+ ","
			+ COLUMN_FALL
			+ "),"
			+ "FOREIGN KEY("
			+ COLUMN_FALL
			+ ") REFERENCES "
			+ TABLE_FALL
			+ "("
			+ COLUMN_TIMESTAMP_F + "))";

	public DBOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * Chiamato da android quando si fa per la prima volta l'accesso al db e
	 * questi non esiste.
	 */
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE_SESSION);
		database.execSQL(DATABASE_CREATE_FALL);
		database.execSQL(DATABASE_CREATE_ACCELEROMETER);
	}

	/**
	 * Chiamato da android quando il db viene aggiornato, cio� quando
	 * si installa l'app aggiornata e sono successe modifiche alla struttura del
	 * db.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DBOpenHelper.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCELEROMETER);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_FALL);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSION);
		onCreate(db);
	}

}
