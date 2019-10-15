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
	
	public MatrixStack() {
		stack = new ArrayList<float[]>();
		size = -1;
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
	public void scale(float sx, float sy) { this.scale(sx, sy, 0); }
	public void rotate(float rz) { this.rotate(0, 0, rz); }
	
	
}
