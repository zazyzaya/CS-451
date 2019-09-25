import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LINES;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL4.*;

import java.nio.FloatBuffer;

import com.jogamp.opengl.*;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;

public class H2_iking_cylinder extends H2_iking_cone {
	private float[] connectors, top, bottom;
	
	/**
	 * Method to combine top and bottom into a single array. Usually one should supply
	 * vPoints as the dst argument
	 * @param top
	 * @param bottom
	 * @param dst
	 * @return
	 */
	private void combineTopAndBottom(float[] top, float[] bottom, float[] dst) {
		System.arraycopy(top, 0, dst, 0, top.length);
		System.arraycopy(bottom, 0, dst, top.length, bottom.length);
	}
	
	// Create quad coordinates for top and bottom
	private float[] connectTopAndBottom(float[] top, float[] bottom) {
		float[] ret = new float[top.length * 4];
		
		for (int i=0; i<top.length/3; i++) {
			int offset = i*3;
			int retOffset = i*12;
			
			// For each top/bottom pair, generate a group of 4 corresponding points
			// For ease of coding we copy full points (3 indexes of the array) at a time
			System.arraycopy(top, offset, ret, retOffset, 3);
			System.arraycopy(bottom, offset, ret, retOffset+3, 3);
			System.arraycopy(bottom, (offset+3)%bottom.length, ret, retOffset+6, 3);
			System.arraycopy(top, (offset+3)%top.length, ret, retOffset+9, 3);
		}
		
		return ret;
	}
	
	/**
	 * Returns the starting points for the various primitive 3D shapes in this assignment
	 * @param s
	 * @return
	 */
	protected float[][] getStartTips() {
		return new float[][] { 
			//Top
			new float[] {
				0f, 1f, 0f,
				-1f, 0f, 0f,
				0f, 0f, 0f,
				  
				-1f, 0f, 0f,
				0f, -1f, 0f, 
				0f, 0f, 0f,
				  
				0f, -1f, 0f, 
				1f, 0f, 0f,
				0f, 0f, 0f,
				  
				1f, 0f, 0f,
				0f, 1f, 0f,
				0f, 0f, 0f
			},
			
			// Bottom  
			new float[] {
				0f, 1f, HEIGHT,
				-1f, 0f, HEIGHT,
				0f, 0f, HEIGHT,
					
				-1f, 0f, HEIGHT,
				0f, -1f, HEIGHT, 
				0f, 0f, HEIGHT,
				  
				0f, -1f, HEIGHT,
				1f, 0f, HEIGHT,
				0f, 0f, HEIGHT,
				  
				1f, 0f, HEIGHT,
				0f, 1f, HEIGHT,
				0f, 0f, HEIGHT
			}
		};
	}
	
	protected float[] getStartPoints() {
		float[][] st = getStartTips();
		float[] ret = new float[st[0].length * 2];
		combineTopAndBottom(st[0], st[1], ret);
		
		// Initialize globals while we're here
		top = st[0];
		bottom = st[1];
		connectors = connectTopAndBottom(top, bottom);
		
		return ret;
	}
	
	protected float[][] incrimentPointsCyl(float[] pts){
		float[][] newVectors = {new float[pts.length * 2], new float[pts.length * 2]};
		float[] v1 = new float[3], v2 = new float[3], newV = new float[3];
		float[] ctrb = new float[] {0.0f, 0.0f, 0.0f};
		float[] ctrt = new float[] {0.0f, 0.0f, HEIGHT};
		
		// Having a list like this lets us avoid a thousand arrcopy calls, so I can just
		// do it in the below loop
		float[][] copyOrder = {v1, newV, ctrb, newV, v2, ctrb};
		
		// Act on each group of 9 points, each polygon at the same time
		for(int i=0; i<pts.length/9; i++) {

			// First copy original vectors out  
			System.arraycopy(pts, i*9, v1, 0, 3);
			System.arraycopy(pts, i*9+3, v2, 0, 3);
			
			// Then calculate the new vector in the middle
			float split[] = norm(vectorAdd(v1, v2));
			System.arraycopy(split, 0, newV, 0, 3);
			
			// Then add everything to the new vector list in order
			for (int j=0; j<6; j++) {
				// Build top and bottom circles (using same coords) 
				System.arraycopy(copyOrder[j], 0, newVectors[0], i*18 + j*3, 3);
				System.arraycopy(copyOrder[j], 0, newVectors[1], i*18 + j*3, 3);
				
				// Add the height into the top vectors
				newVectors[1][i*18+j*3+2] = HEIGHT;
			}
		}
		
		return newVectors;
	}
	
	public static void main(String[] args) {
		 new H2_iking_cylinder();
	}
	
	
	public void display(GLAutoDrawable drawable) {
		num_ticks += 1;
		
		gl = (GL4) drawable.getGL();
		
		// Clear depth and color buffers
		gl.glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

		// Increment number of points
		if (num_ticks % INC_TIMING == 0 && num_ticks/60 < NUM_ITERS) {
			float inc[][] = incrimentPointsCyl(top);
			top = inc[0];
			bottom = inc[1];
			connectors = connectTopAndBottom(top, bottom);
			
			// Load new points into vPoints
			vPoints = new float[top.length * 2];
			combineTopAndBottom(top, bottom, vPoints);
		}
		
		// Restart the incriment process
		if (num_ticks/60 == RESET_AT) {
			num_ticks = 0;
			vPoints = getStartPoints();
		}
		
		// Rotate
		xth += x_delta;
		yth += y_delta;
		zth += z_delta;
		
		// Grow or shrink
		if (scale >= MAX_SCALE) {
			isShrinking = true;
		}
		else if (scale <= MIN_SCALE) {
			isShrinking = false;
		}
		scale = isShrinking ? scale-scale_delta : scale+scale_delta;
		
		// Combine transforms and send to GPU
		float[] rotMatrix = getRotMatrix(xth, yth, zth);
		rotMatrix = matMult4x4(rotMatrix, getScaleMatrix(scale));
		
		int lPtr = gl.glGetUniformLocation(vfPrograms, "drawLines");
		int rmPtr = gl.glGetUniformLocation(vfPrograms, "rotMatrix");
		gl.glProgramUniformMatrix4fv(vfPrograms, rmPtr, 1, true, rotMatrix, 0);
		gl.glProgramUniform1i(vfPrograms, lPtr, 0);
		
		// Draw caps of cylinder
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[POSITION]); // use handle 0 		
		FloatBuffer vBuf = Buffers.newDirectFloatBuffer(vPoints);
		gl.glBufferData(GL_ARRAY_BUFFER, vBuf.limit()*Float.BYTES, vBuf, GL_STATIC_DRAW); 
		gl.glVertexAttribPointer(POSITION, 3, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer
		gl.glDrawArrays(GL_TRIANGLES, 0, vPoints.length / 3);
		
		// Draw vertical connectors of cylinder
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[POSITION]); // use handle 0 		
		FloatBuffer cBuf = Buffers.newDirectFloatBuffer(connectors);
		gl.glBufferData(GL_ARRAY_BUFFER, cBuf.limit()*Float.BYTES, cBuf, GL_STATIC_DRAW); 
		gl.glVertexAttribPointer(POSITION, 3, GL_FLOAT, false, 0, 0);
		gl.glDrawArrays(GL_QUADS, 0, connectors.length / 3);
		
		if (OVERLAY_WIRES) {
			gl.glProgramUniform1i(vfPrograms, lPtr, 1);
			gl.glDrawArrays(GL_LINES, 0, connectors.length / 3);
			
			gl.glBufferData(GL_ARRAY_BUFFER, vBuf.limit()*Float.BYTES, vBuf, GL_STATIC_DRAW); 
			gl.glVertexAttribPointer(POSITION, 3, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer
			gl.glDrawArrays(GL_LINES, 0, vPoints.length / 3);
		}
	}
	
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		HEIGHT = 2;
		vPoints = getStartPoints();
	}
}
