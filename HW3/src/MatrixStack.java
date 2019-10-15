import java.util.ArrayList;

/**
 * Helper class to impliment a stack datastructure for matricies
 * @author Isaiah King
 *
 */
public class MatrixStack {
	private ArrayList<float[]> stack;
	private int size;
	private static Matrix_Lib_iking matOps;
	
	// Builds an array stack with identity matrix by default
	public MatrixStack() {
		stack = new ArrayList<float[]>();
		stack.add(matOps.getIdentity());
		size = 0;
	}
	
	public void push(float[] m) {
		stack.add(m);
		size++;
	}
	
	/**
	 * Pushes a copy of the current matrix to the top 
	 */
	public void pushDupe() {
		this.push(this.peek().clone());
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
		stack.set(size, matOps.mult(this.peek(), matOps.rotate(this.peek(), rx, ry, rz)));
	}
	
	public void translate(float tx, float ty, float tz) {
		stack.set(size, matOps.mult(this.peek(), matOps.translate(this.peek(), tx, ty, tz)));
	}
	
	public void scale(float sx, float sy, float sz) {
		stack.set(size, matOps.mult(this.peek(), matOps.scale(this.peek(), sx, sy, sz)));
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
	
	
}
