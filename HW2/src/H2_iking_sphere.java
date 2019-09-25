import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LINES;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;

import java.nio.FloatBuffer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;

public class H2_iking_sphere extends H2_iking_cone {
	public static void main(String[] args) {
		 new H2_iking_sphere();
	}
	
	protected float[] getStartPoints() {
		return new float[] {
				1f, 0f, 0f,
				0f, 1f, 0f,
				0f, 0f, 1f,
				
				1f, 0f, 0f,
				0f, 1f, 0f,
				0f, 0f, -1f,
				
				1f, 0f, 0f,
				0f, -1f, 0f,
				0f, 0f, 1f,
				
				1f, 0f, 0f,
				0f, -1f, 0f,
				0f, 0f, -1f,
				
				-1f, 0f, 0f,
				0f, 1f, 0f,
				0f, 0f, 1f,
				
				-1f, 0f, 0f,
				0f, 1f, 0f,
				0f, 0f, -1f,
				
				-1f, 0f, 0f,
				0f, -1f, 0f,
				0f, 0f, 1f,
				
				-1f, 0f, 0f,
				0f, -1f, 0f,
				0f, 0f, -1f,
		};
	}
	
	protected float[] incrimentPoints(float[] pts){
		float[] newVectors = new float[pts.length * 4];
		float[] v1 = new float[3], v2 = new float[3], v3 = new float[3];
		float[] v12 = new float[3], v23 = new float[3], v13 = new float[3];
		
		// The following are the polygons that can be generated from the new vectors
		float[][] copyOrder = {
			v1, v12, v13, 
			v2, v23, v12,
			v3, v13, v23,
			v12, v23, v13
		};
		
		// Act on each group of 3 points (9 coords), each polygon at the same time
		for(int i=0; i<pts.length/9; i++) {

			// First copy original vectors out  
			System.arraycopy(pts, i*9, v1, 0, 3);
			System.arraycopy(pts, i*9+3, v2, 0, 3);
			System.arraycopy(pts, i*9+6, v3, 0, 3);
			
			// Then calculate the new normal vectors
			System.arraycopy(norm(vectorAdd(v1, v2)), 0, v12, 0, 3);
			System.arraycopy(norm(vectorAdd(v1, v3)), 0, v13, 0, 3);
			System.arraycopy(norm(vectorAdd(v2, v3)), 0, v23, 0, 3);
			
			// Then add everything to the new vector list in order
			for (int j=0; j<copyOrder.length; j++) {
				System.arraycopy(copyOrder[j], 0, newVectors, j*3+i*copyOrder.length*3, 3);
			}
		}
		
		return newVectors;
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
		super.init(drawable);
		
		// Load initial points
		vPoints = getStartPoints();
		
		// Any higher and my computer becomes very sad
		NUM_ITERS = 6;
	}
}
