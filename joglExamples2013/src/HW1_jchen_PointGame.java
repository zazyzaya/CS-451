//import net.java.games.jogl.GL;
//import net.java.games.jogl.GLDrawable; 
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.sun.opengl.util.GLUT;

import java.awt.event.*; // adding mouse motion event

/**
 * 
 * 
 * <p>
 * Title: Foundations of 3D Graphics Programming : Using JOGL and Java3D
 * </p>
 * 
 * <p>
 * Description: points bounce in a circle ...
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * 
 * <p>
 * Company: George Mason University
 * </p>
 * 
 * @author Dr. Jim X. Chen
 * @version 1.0
 */
public class HW1_jchen_PointGame extends HW1_2_PointInCircle implements MouseMotionListener {

	int bLine[][] = new int[2][2]; // horizontal line
	int numOfPoints = pnum; // number of points
	boolean deadPoint[] = new boolean[pnum]; // fall-below points

	public HW1_jchen_PointGame() {

		bLine[0][1] = bLine[1][1] = HEIGHT/3; 
		for (int i = 0; i < pnum; i++) {

			// randomly generated colors
			deadPoint[i] = false;
		}

	}

	  public void init(GLAutoDrawable drawable) {

		    super.init(drawable);
		    
		    // listen to mouse motion
		    drawable.addMouseMotionListener(this);
	  }

	
	// Called for OpenGL rendering every reshape
	public void display(GLAutoDrawable drawable) {


		gl.glClear(GL.GL_COLOR_BUFFER_BIT);

		// draw a white circle
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		gl.glPointSize(2);
		drawCircle(cx, cy, r);
		
		// draw a line
		bresenhamLine(bLine[0][0], bLine[0][1], bLine[1][0], bLine[1][1]); 

		gl.glPointSize(3);
		for (int i = 0; i < pnum; i++) {
			// move in a direction
			point[i][0] = point[i][0] + direction[i][0];
			point[i][1] = point[i][1] + direction[i][1];
			direction[i][1] -= 0.0015; 

			// bounce when point on/outside the circle
			if (r <= distance(cx, cy, point[i][0], point[i][1])) {

				// change the direction of the point around r
				bounceInCircle(point[i], direction[i]);
			}
			
			// bounce when point on the line
			if (!deadPoint[i] && point[i][0]>bLine[0][0] && point[i][0]<bLine[1][0] && (point[i][1]-bLine[0][1])<0.6 && (bLine[0][1]-point[i][1])<0.6 ) {

				// change the direction of the point around 
				bounceOnLine(point[i], direction[i]);
			}

			// deadpoint handling
			if ((bLine[0][1]-point[i][1])>1.5) {
				
				if (!deadPoint[i]) numOfPoints--; 

				// change the direction of the point around 
				deadPoint[i] = true;
				direction[i][0] = direction[i][0]*0.999;
				if (direction[i][1]>-1) direction[i][1]-= 0.005; 
				//direction[i][1] = direction[i][1]*0.999;
			}		
			
			// draw a colored point in Circle
			gl.glColor3dv(clr[i], 0);  // specify the color
			if (!deadPoint[i]) 
				drawPoint(point[i][0], point[i][1]);
			else if (Math.random()*100<50) {// randomly choose to draw the dead points 
				gl.glColor3f(0.9f,0.9f,0.9f);
				drawPoint(point[i][0], point[i][1]);
			}
			gl.glColor3f(0.9f,0.9f,0.9f);
			gl.glWindowPos2d(HEIGHT/20, 20);
			glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, "Alive: ");
			glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, Integer.toString(numOfPoints));

		}
		// sleep to slow down the rendering
		//try {
		//	Thread.sleep(5);
		//} catch (Exception ignore) {
		//}
	}
	
	public void mouseDragged(MouseEvent e) {
		
		// pretty much reset the game
		for (int i = 0; i < pnum; i++) {

			point[i][1] = Math.random() * HEIGHT; // random height
			deadPoint[i] = false; 

			
			direction[i][0] = Math.random();
			direction[i][1] = Math.random();
			direction[i][2] = 0;
			normalize(direction[i]); // make it a unit vector
		}
	}


	// when mouse is moved, we use it to update the line position
	public void mouseMoved(MouseEvent e) { 

		// The mouse location ==> center of the line 
		bLine[0][0] = e.getX()-WIDTH/8;
		bLine[1][0] = e.getX()+WIDTH/8;

	}


	public void bounceOnLine(double point[], double direction[]) {
		double n[] = new double[3];
		double rd[] = new double[3];

		//n: line normal
		n[0] =  0;
		n[1] = -direction[1];
		n[2] = 0;
		normalize(n);

		for (int i = 0; i < 3; i++) {
			rd[i] = -direction[i];
			point[i] += n[i]; // so the point remains in circle
		}
		reflect(rd, n, direction); 
	}

	
	public void bounceInCircle(double point[], double direction[]) {
		double n[] = new double[3];
		double rd[] = new double[3];

		//n: a vector from point to origin
		n[0] =  cx - point[0];
		n[1] = cy - point[1];
		n[2] = 0;
		normalize(n);

		for (int i = 0; i < 3; i++) {
			rd[i] = -direction[i];
			point[i] += n[i]; // so the point remains in circle
		}
		reflect(rd, n, direction); 
	}


	public static void main(String[] args) {
		HW1_jchen_PointGame f = new HW1_jchen_PointGame();

		f.setTitle("HW1_2 - points bounce in a circle");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);

	}
}
