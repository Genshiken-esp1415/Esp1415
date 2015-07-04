package it.unipd.dei.esp1415;

import java.util.ArrayList;
import java.util.Date;

/**
 * Classe contenitore per i dati relativi ad una caduta. Presente un builder
 * per sostituire il costruttore default.
 */
public class Fall {

	private Date mFallTimestamp;
	private int mFallNumber; // Campo ridondante ma necessario per migliorare le
								// prestazioni generali
	private Date mSession;
	private boolean mNotified;

	private double mLatitude;
	private double mLongitude;
	private ArrayList<AccelerometerData> mFallData; // Lista contenente i dati
													// dell'accelerometro
													// relativi a 500 ms prima e
													// dopo

	// la caduta

	private Fall(FallBuilder builder) {
		this.mFallTimestamp = builder.mFallTimestamp;
		this.mFallNumber = builder.mFallNumber;
		this.mNotified = builder.mNotified;
		this.mSession = builder.mSession;
		this.mFallData = builder.mFallData;
		this.mLatitude = builder.mLatitude;
		this.mLongitude = builder.mLongitude;
	}

	public Date getFallTimestamp() {
		return mFallTimestamp;
	}

	public void setFallTimestamp(Date fallTimestamp) {
		this.mFallTimestamp = fallTimestamp;
	}

	public int getFallNumber() {
		return mFallNumber;
	}

	public void setFallNumber(int fallNumber) {
		this.mFallNumber = fallNumber;
	}

	public Date getSession() {
		return mSession;
	}

	public void setSession(Date session) {
		this.mSession = session;
	}

	public boolean isNotified() {
		return mNotified;
	}

	public void setNotified() {
		this.mNotified = true;

	}

	public double getLatitude() {
		return mLatitude;
	}

	public void setLatitude(double latitude) {
		this.mLatitude = latitude;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public void setLongitude(double longitude) {
		this.mLongitude = longitude;
	}

	public ArrayList<AccelerometerData> getFallData() {
		return mFallData;
	}

	public void setFallData(ArrayList<AccelerometerData> fallData) {
		this.mFallData = fallData;
	}

	public static class FallBuilder {
		private Date mFallTimestamp;
		private int mFallNumber;
		private Date mSession;
		private boolean mNotified;
		private double mLatitude;
		private double mLongitude;
		private ArrayList<AccelerometerData> mFallData;

		public FallBuilder(Date fallTimestamp) {
			this.mFallTimestamp = fallTimestamp;
			// Inizializza i campi facoltativi, nel senso che si potrebbe non
			// avere da subito la lista di dati dell'accelerometro o avere una
			// caduta non ancora notificata
			mFallData = new ArrayList<AccelerometerData>();
			mNotified = false;
		}

		public FallBuilder fallNumber(int fallNumber) {
			this.mFallNumber = fallNumber;
			return this;
		}

		public FallBuilder session(Date session) {
			this.mSession = session;
			return this;
		}

		public FallBuilder notified(Boolean notified) {
			this.mNotified = notified;
			return this;
		}

		public FallBuilder latitude(Double latitude) {
			this.mLatitude = latitude;
			return this;
		}

		public FallBuilder longitude(Double longitude) {
			this.mLongitude = longitude;
			return this;
		}

		public FallBuilder fallData(ArrayList<AccelerometerData> fallData) {
			this.mFallData = fallData;
			return this;
		}

		public Fall build() {
			return new Fall(this);
		}

	}
}
