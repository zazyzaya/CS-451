/*
 * Created on 2004-2-27
 * @author Jim X. Chen: scan-convert randomly generated lines with antialiasing
 */
//import net.java.games.jogl.*;
import javax.media.opengl.*;

public class J1_4_Line extends J1_3_xFont {
	double clr[] = new double[3]; //foreground color

	// called for OpenGL rendering every reshape
	public void display(GLAutoDrawable drawable) {

	//	gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		// generate a random line;
		int x0 = (int) (Math.random() * WIDTH);
		int y0 = (int) (Math.random() * HEIGHT);
		int xn = (int) ((Math.random() * WIDTH));
		int yn = (int) (Math.random() * HEIGHT);

		clr[0]=Math.random(); 
		clr[1]=Math.random(); 
		clr[2]=Math.random();
		gl.glColor3dv(clr, 0);
		bresenhamLine(x0, y0, xn, yn); // draw a line without antialiasing 
		gl.glFlush(); //drawable.swapBuffers(); //make it appear

		// sleep to slow down the rendering
		try {
			Thread.sleep(500);
		} catch (Exception ignore) { }

		// draw a three pixel antialiased line
		antialiasedLine(x0, y0, xn, yn);
		gl.glFlush(); //drawable.swapBuffers(); //make it appear
		try {
			Thread.sleep(500);
		} catch (Exception ignore) {}

	}

	 
	// draw pixel with intensity by its distance to the line
	void IntensifyPixel(int x, int y, float D, int flag) {
		
		if (D < 0) {
				D = -D; // negative if the pixel is above the line
		} 

		double fclr[] = new double [4]; // foreground color
		
		fclr[0] = clr[0];  fclr[1] = clr[1];  fclr[2] = clr[2];

		fclr[0] = fclr[0]*(1 - D/2.5);// + bclr[0] * d/2.5;
		fclr[1] = fclr[1]*(1 - D/2.5);// + bclr[1] * d/2.5;
		fclr[2] = fclr[2]*(1 - D/2.5);// + bclr[2] * d/2.5;

		gl.glColor3dv(fclr, 0);
	    writepixel(x, y, flag); /* write framebuffer */

	}

	// scan-convert a 3 pixel wide antialiased line
	void antialiasedLine(int x0, int y0, int xn, int yn) {
		int dx, dy, incrE, incrNE, d, x, y, flag = 0;
		float D = 0, sin_a, cos_a, sin_cos_a, Denom;

		if (xn < x0) {
			// swapd(& x0, & xn);
			int temp = x0;
			x0 = xn;
			xn = temp;
			// swapd(& y0, & yn);
			temp = y0;
			y0 = yn;
			yn = temp;
		}

		if (yn < y0) {
			y0 = -y0;
			yn = -yn;
			flag = 10;
		}

		dy = yn - y0;
		dx = xn - x0;
		if (dx < dy) {
			// swapd(& x0, & y0);
			int temp = x0;
			x0 = y0;
			y0 = temp;
			// swapd(& xn, & yn);
			temp = xn;
			xn = yn;
			yn = temp;
			// swapd(& dy, & dx);
			temp = dy;
			dy = dx;
			dx = temp;

			flag++;
		}

		x = x0;
		y = y0;
		d = 2 * dy - dx; // decision factor
		incrE = 2 * dy;
		incrNE = 2 * (dy - dx);

		Denom = (float) Math.sqrt((double) (dx * dx + dy * dy));
		sin_a = dy / Denom;
		cos_a = dx / Denom;
		sin_cos_a = sin_a - cos_a;

		while (x < xn + 1) {
			IntensifyPixel(x, y, D, flag);
			IntensifyPixel(x, y + 1, D - cos_a, flag); // N
			IntensifyPixel(x, y - 1, D + cos_a, flag); // S
			IntensifyPixel(x, y + 2, D - 2*cos_a, flag); // N
			IntensifyPixel(x, y - 2, D + 2*cos_a, flag); // S

			x++;
			// consider the next pixel
			if (d <= 0) {
				D += sin_a; // distance to the line from E
				d += incrE;
			} else {
				D += sin_cos_a; // distance to the line: NE
				y++;
				d += incrNE;
			}
		}
	}

	public static void main(String[] args) {
		J1_4_Line f = new J1_4_Line();

		f.setTitle("JOGL J1_4_Line");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);
	}
}
