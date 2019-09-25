import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;

import java.nio.FloatBuffer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;

public class H2_iking_circle extends H2_iking_cone {
	protected float[] getStartPoints() {
		return new float[] { 
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
			  0f, 0f, 0f,
		};
	}
	
	public void display(GLAutoDrawable drawable) {
		num_ticks += 1;
		
		gl = (GL4) drawable.getGL();
		
		// Clear depth and color buffers
		gl.glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

		// Increment number of points
		if (num_ticks % INC_TIMING == 0 && num_ticks/60 < NUM_ITERS) {
			vPoints = super.incrimentPoints(vPoints);
		}
		
		// Update location of cone
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[POSITION]); // use handle 0 		
		FloatBuffer vBuf = Buffers.newDirectFloatBuffer(vPoints);
		gl.glBufferData(GL_ARRAY_BUFFER, vBuf.limit()*Float.BYTES, vBuf, GL_STATIC_DRAW); 
		gl.glVertexAttribPointer(POSITION, 3, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer
		
		gl.glDrawArrays(GL_TRIANGLES, 0, vPoints.length / 3);
	}
	
	public static void main(String[] args) {
		 new H2_iking_circle();
	}

	public void init(GLAutoDrawable drawable) {
		gl = (GL4) drawable.getGL();
		super.init(drawable);
		
		// Set up the correct points for a circle
		vPoints = getStartPoints();
		
		// Ensure super.incriment points doesn't build a cone
		super.HEIGHT = 0;
		
		// It's not worth the effort to make a whole new shader file so the shape doesn't rotate
		// So I just give it the identity matrix here, and never change it
		float[] rotMatrix = getRotMatrix(0, 0, 0);
		int rmPtr = gl.glGetUniformLocation(vfPrograms, "rotMatrix");
		gl.glProgramUniformMatrix4fv(vfPrograms, rmPtr, 1, true, rotMatrix, 0);
		
		// So 3D clips correctly
		gl.glEnable(GL_DEPTH_TEST);
	}
}
