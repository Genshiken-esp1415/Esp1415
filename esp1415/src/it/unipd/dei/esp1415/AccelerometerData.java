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
	public AccelerometerData(float x, float y, float z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
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
	public float getZ() {
		return z;
	}
	public void setZ(float z) {
		this.z = z;
	}
	
}
