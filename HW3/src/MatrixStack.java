import java.util.ArrayList;

/**
 * Helper class to impliment a stack datastructure for matricies
 * @author Isaiah King
 *
 */
public class MatrixStack {
	private ArrayList<float[]> stack;
	private int size;
	private static float PI = (float) Math.PI; 
	
	// Builds an array stack with identity matrix by default
	public MatrixStack() {
		stack = new ArrayList<float[]>();
		stack.add(Matrix_Lib_iking.getIdentity());
		size = 0;
	}
	
	public void push(float[] m) {
		stack.add(m);
		size++;
	}
	
	public void push() {
		this.push(this.peek().clone());
	}
	
	public void pushIdentity() {
		this.push(Matrix_Lib_iking.getIdentity());
	}
	
	public float[] pop() {
		return stack.remove(size--);
	}
	
	public float[] peek() {
		return stack.get(size);
	}
	
	
	/**
	 * The following methods perform matrix transforms on the top-most element in the stack
	 *
	 */
	public void rotate(float angle, float rx, float ry, float rz) {
		stack.set(size, Matrix_Lib_iking.rotate(this.peek(), angle, rx, ry, rz));
	}
	
	public void translate(float tx, float ty, float tz) {
		stack.set(size, Matrix_Lib_iking.translate(this.peek(), tx, ty, tz));
	}
	
	public void scale(float sx, float sy, float sz) {
		stack.set(size, Matrix_Lib_iking.scale(this.peek(), sx, sy, sz));
	}
	
	public void multiply(float[] m) {
		stack.set(size,  Matrix_Lib_iking.mult(this.peek(), m));
	}
	
	// Overloaded for 2d transforms
	public void translate(float tx, float ty) { this.translate(tx, ty, 0); }
	public void scale(float sx, float sy) { this.scale(sx, sy, 1); }
	public void scale(float sx) { this.scale(sx, sx, sx); }
	public void rotate(float rz) { this.rotate(rz, 0, 0, 1.0f); } // Shorthand rotates around Z
	
	// Overloaded for inputting vectors as transform coords
	public void translate(float[] t) { this.translate(t[0], t[1], t[2]); }
	public void scale(float[] s) { this.scale(s[0], s[1], s[2]); }
	
	// Overloaded to automatically cast
	public void multiply(double[] m) {
		float[] converted = new float[m.length];
		
		for (int i=0; i<converted.length; i++) {
			converted[i] = (float) m[i];
		}
		
		this.multiply(converted);
	}
	
	// Specialized
	/**
	 * Shorthand to rotate all axes the same amount. Uses rotX*rotY*rotZ order
	 * @param theta angle to rotate
	 */
	public void rotateAllAxes(float theta) {
		this.rotate(theta, 1.0f, 0, 0);
		this.rotate(theta, 0, 1.0f, 0);
		this.rotate(theta, 0, 0, 1.0f);
	}
	
	/**
	 * Converts degree input to radians for rotations
	 * @param theta
	 * @param isX
	 * @param isY
	 * @param isZ
	 */
	public void rotateDegrees(float theta, float isX, float isY, float isZ) {
		this.rotate((theta/360)*2*PI, isX, isY, isZ);
	}

	
	
	// Informational operations 
	/**
	 * Returns the origin relative to the current matrix
	 */
	public float[] getOrigin() {
		float[] m = this.peek();
		return new float[] {
			m[3], m[7], m[11], m[15]
		};
	}
	
	
	// Projections
	public void pushOrtho(
			float left, float right, 
			float bottom, float top,  
			float near, float far) {
		
		this.push(Matrix_Lib_iking.getOrtho(left, right, bottom, top, near, far));
	}
	
	public void pushFrustum(
			float left, float right, 
			float bottom, float top, 
			float near, float far) {
		
		this.push(Matrix_Lib_iking.getFrustum(left, right, bottom, top, near, far));
	}
}
