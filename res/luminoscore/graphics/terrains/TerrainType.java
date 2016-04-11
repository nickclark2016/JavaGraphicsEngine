package luminoscore.graphics.terrains;

/**
 * 
 * @author Nick Clark
 * @version 1.0
 * 
 * Terrain class holding a terrain type
 *
 */

public class TerrainType {
	
	/**
	 * Enumeration of Terrain Types
	 */
	public static enum Type {
		FLAT,
		PLAINS,
		HILLS,
		MOUNTAINS
	}
	
	/**
	 * @param type		Type of terrain	
	 * @return float[]	Data on terrain type
	 * 
	 * Gets the data from the terrain type
	 */
	public static float[] getData(Type type) {
		float data[] = new float[3];
		switch(type) {
		case FLAT:
			data[0] = 0;
			data[1] = 1;
			data[2] = 0;
			break;
		case PLAINS:
			data[0] = 25;
			data[1] = 5;
			data[2] = 0.05f;
			break;
		case HILLS:
			data[0] = 150;
			data[1] = 6;
			data[2] = .05f;
			break;
		case MOUNTAINS:
			data[0] = 100;
			data[1] = 2;
			data[2] = .15f;
		}
		
		return data;
			
	}
	
}