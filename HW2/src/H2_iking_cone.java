import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.*;

import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.common.nio.Buffers;
import java.nio.FloatBuffer;
import java.util.Random;

public class H2_iking_cone extends JOGL1_3_VertexArray {
	protected int vao[ ] = new int[1];
	protected int vbo[ ] = new int[1];	// Position
	protected static int POSITION=0;
	protected static Random rnd = new Random();
	protected static float xth = rnd.nextFloat()*2f;
	protected static float yth = rnd.nextFloat()*2f;
	protected static float zth = rnd.nextFloat()*2f + 1f;
	protected static float scale = 0.5f;
	protected boolean isShrinking = true;
	protected double num_ticks = 0;
	
	// Change these for more pleasing animation
	protected static boolean OVERLAY_WIRES = true;	// Mostly used to test faces are going where they should
													// Looks pretty bad at higher poly counts
	
	// Speeds of rotation and scaling
	protected static float x_delta = 0.006f;
	protected static float y_delta = 0.0061f;
	protected static float z_delta = 0.0011f; 
	protected static float scale_delta = 0.001f; 
	
	// When to toggle shrink/grow 
	protected static float MAX_SCALE = 0.5f;
	protected static float MIN_SCALE = 0.25f;
	
	protected static int INC_TIMING = 70; 	// How often to iterate
	protected static int NUM_ITERS = 10;	// When to stop adding vectors
	protected static int RESET_AT = 12;		// When to reset the shape back to low-poly
	protected float HEIGHT = 1f;	// How tall the shape is
	
	
	/**
	 * Performs multiplication on two 4x4 matrices
	 */
	protected float[] matMult4x4(float[] m1, float[] m2) {
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
	
	protected float[] getScaleMatrix(float sFactor) {
		return new float[] {
				sFactor, 0f, 0f, 0f,
				0f, sFactor, 0f, 0f,
				0f, 0f, sFactor, 0f,
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
	protected float[] getRotMatrix(float thetaX, float thetaY, float thetaZ){
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
	 * Returns the starting points for the various primitive 3D shapes in this assignment
	 * @param s
	 * @return
	 */
	protected float[] getStartPoints() {
		return new float[] { 
			  0f, 1f, 0f,
			  -1f, 0f, 0f,
			  0f, 0f, HEIGHT,
			  
			  -1f, 0f, 0f,
			  0f, -1f, 0f, 
			  0f, 0f, HEIGHT,
			  
			  0f, -1f, 0f, 
			  1f, 0f, 0f,
			  0f, 0f, HEIGHT,
			  
			  1f, 0f, 0f,
			  0f, 1f, 0f,
			  0f, 0f, HEIGHT,
		};
	}
	
	/**
	 * Normalizes a vector input
	 * @param vec
	 * @return
	 */
	protected float[] norm(float[] vec) {
		float magnitude = 0;
		for (int i=0; i<vec.length; i++) {
			magnitude += vec[i] * vec[i];
		}
		
		magnitude = (float) Math.sqrt(magnitude);
		
		float ret[] = new float[vec.length];
		for (int i=0; i<vec.length; i++) {
			ret[i] = vec[i] / magnitude;
		}
		
		return ret;
	}
	
	/**
	 * Returns the sum of two vectors
	 * @param v1
	 * @param v2
	 * @return
	 */
	protected float[] vectorAdd(float[] v1, float[] v2) {
		float ret[] = new float[v1.length];
		
		for (int i=0; i<v1.length; i++) {
			ret[i] = v1[i] + v2[i];
		}
		
		return ret;
	}
	
	protected float[] incrimentPoints(float[] pts){
		float[] newVectors = new float[pts.length * 2];
		float[] v1 = new float[3], v2 = new float[3], newV = new float[3];
		float[] ctr = { 0.0f, 0.0f, HEIGHT };
		
		// Having a list like this lets us avoid a thousand arrcopy calls, so I can just
		// do it in the below loop
		float[][] copyOrder = {v1, newV, ctr, newV, v2, ctr};
		
		// Act on each group of 9 points, each polygon at the same time
		for(int i=0; i<pts.length/9; i++) {

			// First copy original vectors out  
			System.arraycopy(pts, i*9, v1, 0, 3);
			System.arraycopy(pts, i*9+3, v2, 0, 3);
			
			// Then calculate the new vector in the middle
			System.arraycopy(norm(vectorAdd(v1, v2)), 0, newV, 0, 3);
			
			// Then add everything to the new vector list in order
			for (int j=0; j<6; j++) {
				System.arraycopy(copyOrder[j], 0, newVectors, i*18 + j*3, 3);
			}
		}
		
		return newVectors;
	}
	
	public static void main(String[] args) {
		 new H2_iking_cone();
	}
	
	
	public void display(GLAutoDrawable drawable) {	
		num_ticks += 1;
		gl = (GL4) drawable.getGL();
		
		// Clear depth and color buffers
		gl.glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

		// Increment number of points
		if (num_ticks % INC_TIMING == 0 && num_ticks/60 < NUM_ITERS) {
			vPoints = incrimentPoints(vPoints);
		}
		// Restart the incriment process
		if (num_ticks/60 == RESET_AT) {
			num_ticks = 0;
			vPoints = getStartPoints();
		}
		
		// Update location of cone
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[POSITION]); // use handle 0 		
		FloatBuffer vBuf = Buffers.newDirectFloatBuffer(vPoints);
		gl.glBufferData(GL_ARRAY_BUFFER, vBuf.limit()*Float.BYTES, vBuf, GL_STATIC_DRAW); 
		gl.glVertexAttribPointer(POSITION, 3, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer
		
		xth += x_delta;
		yth += y_delta;
		zth += z_delta;
		
		if (scale >= MAX_SCALE) {
			isShrinking = true;
		}
		else if (scale <= MIN_SCALE) {
			isShrinking = false;
		}
		scale = isShrinking ? scale-scale_delta : scale+scale_delta;
		
		float[] rotMatrix = getRotMatrix(xth, yth, zth);
		rotMatrix = matMult4x4(rotMatrix, getScaleMatrix(scale));
		
		// Update uniforms as needed
		int lPtr = gl.glGetUniformLocation(vfPrograms, "drawLines");
		int rmPtr = gl.glGetUniformLocation(vfPrograms, "rotMatrix");
		gl.glProgramUniformMatrix4fv(vfPrograms, rmPtr, 1, true, rotMatrix, 0);
		gl.glProgramUniform1i(vfPrograms, lPtr, 0);
		
		gl.glPointSize(6f);
		gl.glDrawArrays(GL_TRIANGLES, 0, vPoints.length / 3);
		
		if (OVERLAY_WIRES) {
			gl.glProgramUniform1i(vfPrograms, lPtr, 1);
			gl.glDrawArrays(GL_LINES, 0, vPoints.length / 3);
		}
}
	
	
	public void init(GLAutoDrawable drawable) {
		gl = (GL4) drawable.getGL();
		String vShaderSource[], fShaderSource[] ;
		
		// Set up initial points for cone
		vPoints = getStartPoints();
		
		// Initialize shaders
		vShaderSource = readShaderSource("src/H2_iking_V.shader"); // read vertex shader
		fShaderSource = readShaderSource("src/H2_iking_F.shader"); // read fragment shader
		vfPrograms = initShaders(vShaderSource, fShaderSource);		
		
		// Generate vertex arrays indexed by vao
		gl.glGenVertexArrays(vao.length, vao, 0); // vao stores the handles, starting position 0
		System.out.println(vao.length); // we only use one vao
		gl.glBindVertexArray(vao[0]); // use handle 0
		
		// Generate vertex buffers indexed by vbo: here vertices and colors
		gl.glGenBuffers(vbo.length, vbo, 0);
		System.out.println(vbo.length); 
		
		// Enable VAO with loaded VBO data
		gl.glEnableVertexAttribArray(POSITION); // enable the 0th vertex attribute: position
		
		// So 3D clips correctly
		gl.glEnable(GL_DEPTH_TEST);
	}
}
