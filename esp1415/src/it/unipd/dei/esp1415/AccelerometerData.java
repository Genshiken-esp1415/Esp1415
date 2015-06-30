package it.unipd.dei.esp1415;


/**
 * classe contenitore per i dati registrati dall'accelerometro.
 */
public class AccelerometerData {
	private float mX;
	private float mY;
	private float mZ;
	private long mTimestamp;

	public AccelerometerData(long timestamp, float x, float y, float z) {
		super();
		this.mX = x;
		this.mY = y;
		this.mZ = z;
		this.mTimestamp = timestamp;
	}

	public float getX() {
		return mX;
	}

	public void setX(float x) {
		this.mX = x;
	}

	public float getY() {
		return mY;
	}

	public void setY(float y) {
		this.mY = y;
	}

	public long getTimestamp() {
		return mTimestamp;
	}

	public void setTimestamp(long timestamp) {
		this.mTimestamp = timestamp;
	}

	public float getZ() {
		return mZ;
	}

	public void setZ(float z) {
		this.mZ = z;
	}

}
