package it.unipd.dei.esp1415;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
/**
 * Utilizzato per creare il database all'installazione dell'app o aggiornarlo
 * quando l'app viene modificata.
 * 
 * @author Andrea
 *
 */
public class DbOpenHelper extends  SQLiteOpenHelper {

	// variabili per la tabella SESSIONE
	public static final String TABLE_SESSION = "SESSIONE";
	public static final String COLUMN_TIMESTAMP_S = "timestamp_inizio";
	public static final String COLUMN_NAME = "nome";
	public static final String COLUMN_DURATA = "durata";
	public static final String COLUMN_ATTIVA = "attiva";
	//variabili per la tabella CADUTA
	public static final String TABLE_FALL = "CADUTA";
	public static final String COLUMN_NUMBER = "numero";
	public static final String COLUMN_TIMESTAMP_F = "timestamp";
	public static final String COLUMN_NOTIFIED = "notificato";
	public static final String COLUMN_LATITUDE = "latitudine";
	public static final String COLUMN_LONGITUDE = "longitudine";
	public static final String COLUMN_SESSION = "timestamp_inizio";
	//variabili per la tabella ACCELEROMETRO
	public static final String TABLE_ACCELEROMETER = "ACCELEROMETRO";
	public static final String COLUMN_SAMPLENUMBER = "numero_campione";
	public static final String COLUMN_X = "x";
	public static final String COLUMN_Y = "y";
	public static final String COLUMN_Z = "z";
	public static final String COLUMN_FALL = "timestamp";

	private static final String DATABASE_NAME = "commments.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE_SESSION = ""
			+ "CREATE TABLE " + TABLE_SESSION 
			+ " ( " + COLUMN_TIMESTAMP_S + " TEXT PRIMARY KEY, "
			+ COLUMN_NAME + " TEXT, "
			+ COLUMN_DURATA + " INTEGER, "
			+ COLUMN_ATTIVA + " INTEGER)";
	private static final String DATABASE_CREATE_FALL = ""
			+ "CREATE TABLE " + TABLE_FALL 
			+ " ( " + COLUMN_TIMESTAMP_F + " TEXT PRIMARY KEY, "
			+ COLUMN_NUMBER + " INTEGER, "
			+ COLUMN_NOTIFIED + " INTEGER, "
			+ COLUMN_LATITUDE + " REAL, "
			+ COLUMN_LONGITUDE + " REAL, "
			+ COLUMN_SESSION + " TEXT, "
			+ "FOREIGN KEY(" + COLUMN_SESSION + ") REFERENCES " + TABLE_SESSION + "(" + COLUMN_TIMESTAMP_S +"))";
	private static final String DATABASE_CREATE_ACCELEROMETER = ""
			+ "CREATE TABLE " + TABLE_ACCELEROMETER 
			+ " ( " + COLUMN_SAMPLENUMBER + " INTEGER, "
			+ COLUMN_X + " REAL, "
			+ COLUMN_Y + " REAL, "
			+ COLUMN_Z + " REAL, "
			+ COLUMN_LONGITUDE + " REAL, "
			+ COLUMN_FALL + " TEXT, "
			+ "PRIMARY KEY (" + COLUMN_SAMPLENUMBER + "," + COLUMN_FALL + "),"
			+ "FOREIGN KEY(" + COLUMN_FALL + ") REFERENCES " + TABLE_FALL + "(" + COLUMN_TIMESTAMP_F +"))";
			

	public DbOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE_SESSION);
		database.execSQL(DATABASE_CREATE_FALL);
		database.execSQL(DATABASE_CREATE_ACCELEROMETER);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DbOpenHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCELEROMETER);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_FALL);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSION);
		onCreate(db);
	}

} 



