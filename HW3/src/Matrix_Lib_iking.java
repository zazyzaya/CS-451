
/**
 * A static class with helper methods for matrix manipulation
 * @author The Jabberwock
 *
 */
public class Matrix_Lib_iking {
	/* Multiplication */
	
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
	
	public static float[] vecMult(float[] m, float[] v) {
		float[] product = new float[4];
		
		// Adjust for 2d coords
		if (v.length == 3) {
			v = new float[] { v[0], v[1], v[2], 1.0f };
		}
		
		for (int i=0; i<4; i++) {
			float val = 0;
			
			for (int j=0; j<4; j++) {
				val += v[i] * m[j*4 + i];
			}
			
			product[i] = val;
		}
		
		return product;
	}
	
	// For ease of typing
	public static float[] mult(float[] m1, float[] m2) {
		return matMult4x4(m1, m2);
	}
	
	
	/* Transformations */
	
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
		return mult(mat, rotMat);
	}
	
	public static float[] translate(float[] mat, float tx, float ty, float tz) {
		float[] tMat = getTransMatrix(tx, ty, tz);
		return mult(mat, tMat);
	}
	
	public static float[] scale(float[] mat, float sx, float sy, float sz) {
		float[] sMat = getScaleMatrix(sx, sy, sz);
		return mult(mat, sMat);
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
	
	/* Projections */
	public static float[] getOrtho(
			float left, float right, 
			float top, float bottom, 
			float near, float far) {
		
		float[] ret = new float[] {
				2/(right-left), 0, 0, (right+left)/(right-left),
				0, 2/(top-bottom), 0, (top+bottom)/(top-bottom),
				0, 0, -2/(far-near), (far+near)/(near-far),
				0, 0, 0, 1
		};
		
		return ret;
	}
	
	public static float[] getFrustum(
			float left, float right,
			float top, float bottom,
			float near, float far) {
		
		float[] ret = new float[] {
				near, 0, 0, 0, 
				0, near, 0, 0, 
				0, 0, far+near, far*near,
				0, 0, -1, 0
		};
		
		return mult(getOrtho(left, right, top, bottom, near, far), ret);
	}
	
}
