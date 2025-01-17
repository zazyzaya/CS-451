/*
 * Created on 2004-3-6
 * @author Jim X. Chen: 2D robot arm transformations
 */
//import net.java.games.jogl.*;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LINES;
import static com.jogamp.opengl.GL.GL_POINTS;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;

import java.nio.FloatBuffer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;

public class J2_3_Robot2d_iking extends J2_1_Clock2d_iking {
	protected MatrixStack matStack = new MatrixStack();
	private Matrix_Lib_iking matOps;
	private String vShaderSourceFile = "src/clock_2d_iking_v.shader";
	private String fShaderSourceFile = "src/clock_2d_iking_f.shader";
	
	float O[] = {0, 0}, A[] = {0.5f, 0};
	float B[] = {0.75f, 0}, C[] = {1f, 0};
	float alpha=-40, beta=-40, gama=60,
			dalpha = 1f, dbeta = 1.2f, dgama = -2f;

	public void display(GLAutoDrawable glDrawable) {
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);

		alpha += dalpha/100;
		beta += dbeta/100;
		gama += dgama/100;

		gl.glProgramUniform4f(vfPrograms, colorPtr, 0f, 1f, 1f, 1f);
		transDrawArm(alpha, beta, gama);

		gl.glProgramUniform4f(vfPrograms, colorPtr, 1f, 1f, 0f, 1f);
		transDrawArm(-beta, -gama, alpha);

		gl.glProgramUniform4f(vfPrograms, colorPtr, 1f, 0f, 1f, 1f);
		transDrawArm(gama, -alpha, -beta);
	}

	

	// Method II: 2D robot arm transformations
	public void transDrawArm(float a, float b, float g) {
		matStack.push();
		
		matStack.rotate(a);
		drawArm(O, A);
		
		matStack.translate(A[0], A[1]);
		matStack.rotate(b);
		matStack.translate(-A[0], -A[1]);
		drawArm(A, B);
		
		matStack.translate(B[0], 0);
		matStack.rotate(g);
		matStack.translate(-B[0], 0);
		gl.glPointSize(6f);
		drawArm(B, C);
		
		matStack.pop();
	}

	// draw the coordinates directly
	public void drawArm(float C[], float H[]) {
		vPoints = new float[] {
			C[0], C[1], 0, 1.0f,
			H[0], H[1], 0, 1.0f
		};
		
		// Load points into buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[POSITION]); // use handle 0 		
		FloatBuffer vBuf = Buffers.newDirectFloatBuffer(vPoints);
		gl.glBufferData(GL_ARRAY_BUFFER, vBuf.limit()*Float.BYTES, vBuf, GL_STATIC_DRAW); 
		gl.glVertexAttribPointer(POSITION, 4, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer

		// Load most recent matrix into uniform
		float[] mmx = matStack.peek();
		gl.glProgramUniformMatrix4fv(vfPrograms, mxPtr, 1, true, mmx, 0);

		// Draw line
		gl.glDrawArrays(GL_LINES, 0, vPoints.length / 4);
	}


	public static void main(String[] args) {
		J2_3_Robot2d_iking f = new J2_3_Robot2d_iking();

		f.setTitle("JOGL J2_3_Robot2d");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);
	}
}
