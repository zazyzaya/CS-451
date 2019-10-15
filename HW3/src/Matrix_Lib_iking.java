
/**
 * A static class with helper methods for matrix manipulation
 * @author The Jabberwock
 *
 */
public class Matrix_Lib_iking {
	/**
	 * Performs multiplication on two 4x4 matrices
	 */
	private static float[] matMult4x4(float[] m1, float[] m2) {
		float[] product = new float[16];
		
		for (int i=0; i<16; i++) {
			float val=0;
			
			int row = i/4;
			int col = i%4;
			
			for (int j=0; j<4; j++) {
				val += m1[row*4 + j]*m2[col+j*4];
			}
			
			product[i] = val;
		}
		
		return product;
	}
	
	// For ease of typing
	public static float[] mult(float[] m1, float[] m2) {
		return matMult4x4(m1, m2);
	}
	
	/**
	 *  Generates a scale matrix for a given scaling factor
	 */
	public static float[] getScaleMatrix(float sx, float sy, float sz) {
		return new float[] {
				sx, 0f, 0f, 0f,
				0f, sy, 0f, 0f,
				0f, 0f, sz, 0f,
				0f, 0f, 0f, 1f
		};
	}
	/**
	 * Builds a rotation matrix given 3 angles in 3D space, then multiplies them together
	 * to get a 4x4 matrix used for transforms in the vert shader
	 * 
	 * @param thetaX
	 * @param thetaY
	 * @param thetaZ
	 * @return
	 */
	public static float[] getRotMatrix(float thetaX, float thetaY, float thetaZ){
		// Row major rot matrix
		float[] xrot = {
				1f, 0f, 0f, 0f,
				0f, (float) Math.cos(thetaX), (float) -Math.sin(thetaX), 0f,
				0f, (float) Math.sin(thetaX), (float) Math.cos(thetaX), 0f,
				0f, 0f, 0f, 1f
		};
		
		float[] yrot = {
				(float) Math.cos(thetaY), 0f, (float) -Math.sin(thetaY), 0f,
				0f, 1f, 0f, 0f, 
				(float) Math.sin(thetaY), 0f, (float) Math.cos(thetaY), 0f,
				0f, 0f, 0f, 1f
		};
		
		float[] zrot = {
				(float) Math.cos(thetaZ), (float) -Math.sin(thetaZ), 0f, 0f,
				(float) Math.sin(thetaZ), (float) Math.cos(thetaZ), 0f, 0f,
				0f, 0f, 1f, 0f,
				0f, 0f, 0f, 1f
		};
		
		return matMult4x4(matMult4x4(xrot, yrot), zrot);
	}
	
	/**
	 * Builds a translation matrix 
	 * 
	 * @param tx
	 * @param ty
	 * @param tz
	 * @return
	 */
	public static float[] getTransMatrix(float tx, float ty, float tz) {
		float[] ret = {
				1.0f, 0, 0, tx,
				0, 1.0f, 0, ty,
				0, 0, 1.0f, tz,
				0, 0, 0, 1.0f
		};
		
		return ret;
	}
	
	/**
	 * Helper methods to quickly apply tranformations
	 * 
	 */
	public static float[] rotate(float[] mat, float rx, float ry, float rz) {
		float[] rotMat = getRotMatrix(rx, ry, rz);
		return mult(rotMat, mat);
	}
	
	public static float[] translate(float[] mat, float tx, float ty, float tz) {
		float[] tMat = getTransMatrix(tx, ty, tz);
		return mult(tMat, mat);
	}
	
	public static float[] scale(float[] mat, float sx, float sy, float sz) {
		float[] sMat = getScaleMatrix(sx, sy, sz);
		return mult(sMat, mat);
	}
	
	public static float[] getIdentity() {
		float[] ret = new float[] {
			1.0f, 0, 0, 0,
			0, 1.0f, 0, 0,
			0, 0, 1.0f, 0, 
			0, 0, 0, 1.0f
		};
		
		return ret;
	}
	
}
