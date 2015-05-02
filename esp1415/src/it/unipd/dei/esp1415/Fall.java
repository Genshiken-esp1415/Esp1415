package it.unipd.dei.esp1415;

import java.util.ArrayList;
import java.util.Date;

/**
 * Classe contenitore per i dati relativi ad una caduta
 */
public class Fall {
	
	private Date fallTimestamp;
	// campo ridondante, mi basta sapere la data e a quale sessione appartiene,
	// la query mi da già le cadute dalla piu vecchia alla piu nuova...
	private int fallNumber;
	//forse non servirà, intanto teniamo un riferimento alla sessione per sicurezza
	private Session session;
	private boolean notified;
	//lista contenente i dati dell'accelerometro relativi a 500 ms prima e dopo la caduta
	private ArrayList<AccelerometerData> fallData;	
	
	private Fall(FallBuilder builder){
		this.fallTimestamp = builder.fallTimestamp;
		this.fallNumber = builder.fallNumber;
		this.notified = builder.notified;
		this.session = builder.session;
		this.fallData = builder.fallData;
	}

	public Date getFallTimestamp() {
		return fallTimestamp;
	}

	public void setFallTimestamp(Date fallTimestamp) {
		this.fallTimestamp = fallTimestamp;
	}

	public int getFallNumber() {
		return fallNumber;
	}

	public void setFallNumber(int fallNumber) {
		this.fallNumber = fallNumber;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public boolean isNotified() {
		return notified;
	}

	public void setNotified(boolean notified) {
		this.notified = notified;
	}

	public ArrayList<AccelerometerData> getFallData() {
		return fallData;
	}

	public void setFallData(ArrayList<AccelerometerData> fallData) {
		this.fallData = fallData;
	}
	
	public static class FallBuilder {
		private Date fallTimestamp;
		private int fallNumber;
		private Session session;
		private boolean notified;
		private ArrayList<AccelerometerData> fallData;

		public FallBuilder(Date fallTimestamp) {
			this.fallTimestamp = fallTimestamp;
			// Inizializzo campi facoltativi, nel senso che posso non avere da
			// subito la lista di dati dell'accelerometro o avere una caduta non
			// ancora notificata.
			fallData = new ArrayList<AccelerometerData>();
			notified = false;
		}
		
		public FallBuilder fallNumber(int fallNumber) {
			this.fallNumber = fallNumber;
			return this;
		}
		
		public FallBuilder session(Session session) {
			this.session = session;
			return this;
		}
		
		public FallBuilder notified(Boolean notified) {
			this.notified = notified;
			return this;
		}
		
		public FallBuilder fallData(ArrayList<AccelerometerData> fallData) {
			this.fallData = fallData;
			return this;
		}
		
		public Fall build() {
			return new Fall(this);
		}
		
	}
}
