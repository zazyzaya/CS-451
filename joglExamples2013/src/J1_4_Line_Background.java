/*
 * Created on 2004-2-27
 * @author Jim X. Chen: scan-convert randomly generated lines with antialiasing
 */
//import net.java.games.jogl.*;
import javax.media.opengl.*;
import java.nio.ByteBuffer;

public class J1_4_Line_Background extends J1_4_Line {

	// called for OpenGL rendering every reshape
	public void display(GLAutoDrawable drawable) {

		// 1. Clear to a random background color
		gl.glClearColor((float) Math.random(), 
				(float) Math.random(), 
				(float) Math.random(), 0.0f);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);

		super.display(drawable);

	}

	 
	// draw pixel with intensity by its distance to the line
	void IntensifyPixel(int x, int y, float D, int flag) {
		
		   int xf = x, yf = y; // final pixel coordinates

		   // 2. find the actual pixel coordinates: for 
		   //    for reading the framebuffer background color
		    if (flag==1) {
		      xf = y;
		      yf = x;
		    } else if (flag==10) {
		      xf = x;
		      yf = -y;
		    } else if (flag==11) {
		      xf = y;
		      yf = -x;
		    }
		   
			if (D < 0) D = -D; 
			// negative if the pixel is above the line
			
		double fclr[] = new double [4]; // foreground color
		double bclr[] = new double [4]; // background color

		//3. Read the framebuffer pixel color at the current location
		ByteBuffer buffer = ByteBuffer.allocate(4);
	    gl.glReadPixels(xf, yf, 1, 1, 
	    		GL.GL_RGB, GL.GL_UNSIGNED_BYTE, buffer);
	    byte bbuffer[] = buffer.array(); // get the byte array
		
	    //4. Transform byte into double and normalize to 0-1
	    bclr[0] = (double) (bbuffer[0] & 0xFF)/255; 
	    bclr[1] = (double)(bbuffer[1] & 0xFF)/255; 
	    bclr[2] = (double)(bbuffer[2] & 0xFF)/255; 
	    
	    //5. Get the existing foreground color
		fclr[0] = clr[0];  fclr[1] = clr[1];  fclr[2] = clr[2];
		
		// calculate intensity according to the distance D
		fclr[0] = fclr[0]*(1 - D/2.5) + bclr[0] * D/2.5;
		fclr[1] = fclr[1]*(1 - D/2.5) + bclr[1] * D/2.5;
		fclr[2] = fclr[2]*(1 - D/2.5) + bclr[2] * D/2.5;

		//6. Draw the pixel with adjusted intensity level
		gl.glColor3dv(fclr, 0);
		drawPoint(xf, yf);
		gl.glFlush(); //drawable.swapBuffers(); //make it appear

		try {
			Thread.sleep(2);
		} catch (Exception ignore) {}


	}

	public static void main(String[] args) {
		J1_4_Line_Background f = new J1_4_Line_Background();

		f.setTitle("JOGL J1_4_Line_Background");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);
	}
}
