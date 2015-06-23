package it.unipd.dei.esp1415;
/**
 * classe contenitore per i dati registrati dall'accelerometro.
 * @author Andrea
 *
 */
public class AccelerometerData {
	float x;
	float y;
	float z;
	long timestamp;
	
	public AccelerometerData(long timestamp, float x, float y, float z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
		this.timestamp = timestamp;
	}
	
	public float getX() {
		return x;
	}
	public void setX(float x) {
		this.x = x;
	}
	public float getY() {
		return y;
	}
	public void setY(float y) {
		this.y = y;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public float getZ() {
		return z;
	}
	public void setZ(float z) {
		this.z = z;
	}
	
}
