import java.util.ArrayList;

/**
 * Helper class to impliment a stack datastructure for matricies
 * @author Isaiah King
 *
 */
public class MatrixStack {
	private ArrayList<float[]> stack;
	private int size;
	
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
	public void rotate(float rx, float ry, float rz) {
		stack.set(size, Matrix_Lib_iking.rotate(this.peek(), rx, ry, rz));
	}
	
	public void translate(float tx, float ty, float tz) {
		stack.set(size, Matrix_Lib_iking.translate(this.peek(), tx, ty, tz));
	}
	
	public void scale(float sx, float sy, float sz) {
		stack.set(size, Matrix_Lib_iking.scale(this.peek(), sx, sy, sz));
	}
	
	// Overloaded for 2d transforms
	public void translate(float tx, float ty) { this.translate(tx, ty, 0); }
	public void scale(float sx, float sy) { this.scale(sx, sy, 1); }
	public void scale(float sx) { this.scale(sx, sx, 0); }
	public void rotate(float rz) { this.rotate(0, 0, rz); }
	
	// Overloaded for inputting vectors as transform coords
	public void translate(float[] t) { this.translate(t[0], t[1], t[2]); }
	public void scale(float[] s) { this.scale(s[0], s[1], s[2]); }
	public void rotate(float[] r) { this.rotate(r[0], r[1], r[2]); }
	
	// Projections
	public void pushOrtho(
			float left, float right, 
			float top, float bottom, 
			float near, float far) {
		
		this.push(Matrix_Lib_iking.getOrtho(left, right, top, bottom, near, far));
	}
	
	public void pushFrustum(
			float left, float right, 
			float top, float bottom, 
			float near, float far) {
		
		this.push(Matrix_Lib_iking.getFrustum(left, right, top, bottom, near, far));
	}
}
