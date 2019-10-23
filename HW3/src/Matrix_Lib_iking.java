
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
	
	// For ease of typing
	public static float[] mult(float[] m1, float[] m2) {
		return matMult4x4(m1, m2);
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
				val += m[j+4*i] * v[j];
			}
			
			product[i] = val;
		}
		
		return product;
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
	 * Clone of glRotate
	 * Note: Undefined when !(isX ^ isY ^ isZ)
	 * 
	 * @param theta angle to rotate by 
	 * @param isX set to 1.0 for Xrot
	 * @param isY set to 1.0 for Yrot
	 * @param isZ set to 1.0 for Zrot
	 * @return
	 */
	public static float[] getRotMatrix(float theta, float isX, float isY, float isZ){
		float[] ret = null;
		
		// Row major rot matrix
		if (isX == 1.0) {
			float[] xrot = {
				1f, 0f, 0f, 0f,
				0f, (float) Math.cos(theta), (float) -Math.sin(theta), 0f,
				0f, (float) Math.sin(theta), (float) Math.cos(theta), 0f,
				0f, 0f, 0f, 1f
			};
			
			ret = xrot;
		}
		
		else if (isY == 1.0) {
			float[] yrot = {
					(float) Math.cos(theta), 0f, (float) -Math.sin(theta), 0f,
					0f, 1f, 0f, 0f, 
					(float) Math.sin(theta), 0f, (float) Math.cos(theta), 0f,
					0f, 0f, 0f, 1f
			};
			
			ret = yrot;
		}
		
		else if (isZ == 1.0) {
			float[] zrot = {
				(float) Math.cos(theta), (float) -Math.sin(theta), 0f, 0f,
				(float) Math.sin(theta), (float) Math.cos(theta), 0f, 0f,
				0f, 0f, 1f, 0f,
				0f, 0f, 0f, 1f
			};
			
			ret = zrot;
		}
		
		if (isX != 1.0 && isY != 1.0 && isZ != 1.0){
			throw new IllegalArgumentException("Please provide 1 angle, and set exactly 1 argument to 1.0");
		}
		
		return ret;
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
	public static float[] rotate(float[] mat, float angle, float rx, float ry, float rz) {
		float[] rotMat = getRotMatrix(angle, rx, ry, rz);
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
			float bottom, float top, 
			float near, float far) {
		
		float[] ret = new float[] {
				2/(right-left), 0, 0, -(right+left)/(right-left),
				0, 2/(top-bottom), 0, -(top+bottom)/(top-bottom),
				0, 0, -2/(far-near), -(far+near)/(far-near),
				0, 0, 0, 1
		};
		
		return ret;
	}
	
	public static float[] getFrustum(
			float left, float right,
			float bottom, float top,
			float near, float far) {
		
		// OpenGl specs say near is always supposed to be positive
		// But it only really matters for the x and y vectors that near
		// is always positive, and if it's negative, we don't need to negate
		// C and D
		float n = Math.abs(near);
		float neg = -1.0f;
		if (near < 0) {
			neg = 1.0f;
		}
		
		float 	A=(right+left)/(right-left), 
				B=(top+bottom)/(top-bottom),
				C=(far+near)/(far-near),
				D=(2*far*near)/(far-near);
				
		float[] ret = new float[] {
				(2*n)/(right-left), 0, A, 0, 
				0, (2*n)/(top-bottom), B, 0, 
				0, 0, C*neg, D*neg,
				0, 0, -1f, 0
		};
		
		return ret;
	}
	
}
