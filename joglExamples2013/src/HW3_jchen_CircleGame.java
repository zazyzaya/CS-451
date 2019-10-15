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
 * Description: circles bounce in a circle and on edges ...
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2012
 * </p>
 * 
 * <p>
 * Company: George Mason University
 * </p>
 * 
 * @author Dr. Jim X. Chen
 * @version 1.0
 */
public class HW3_jchen_CircleGame extends HW2_jchen_PolygonGame {
		
	double cRadius = 10; 
	
	// Called for OpenGL rendering every reshape
	public void display(GLAutoDrawable drawable) {
		
		int i, j; 

		gl.glClear(GL.GL_COLOR_BUFFER_BIT);

		//draw a big white circle
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		gl.glPointSize(2);
		drawCircle(cx, cy, r);
		gl.glPointSize(1);
		
		// draw a line
		bresenhamLine(bLine[0][0], bLine[0][1], bLine[1][0], bLine[1][1]); 
		
		// the first two points as a line
		antialiasedLine((int) point[0][0], (int) point[0][1], (int) point[1][0], (int) point[1][1]); 
		
		// use the next three points as a triangle
		
		gl.glColor3f(1, 1, 0); 
		gl.glBegin(GL.GL_TRIANGLES); 
		gl.glVertex2d(point[2][0], point[2][1]);
		gl.glVertex2d(point[3][0], point[3][1]);
		gl.glVertex2d(point[4][0], point[4][1]);
		gl.glEnd(); 
		
		
		for (i = 0; i < pnum; i++) {
			// move in a direction			
			
			point[i][0] = point[i][0] + direction[i][0];
			point[i][1] = point[i][1] + direction[i][1];
			direction[i][1] -= 0.001; // drop towards the ground

			// bounce when point on/outside the circle
			if (r <= cRadius + distance(cx, cy, point[i][0], point[i][1])) {

				// change the direction of the point around r
				bounceInCircle(point[i], direction[i]);

				/*
				if (i<2) { // so if the line bounces on the circle, the two ends directions are changed  at the same time. 
					j = (i+1) % 2; 
					direction[j][0] = direction[i][0]; 	
					direction[j][1] = direction[i][1]; 	
					
				} else if (i<5) {
					for (j=0; j<2; j++) {
						direction[((i-1) % 3)+2][j] = direction[i][j]; 	
						direction[(i % 3)+2][j] = direction[i][j]; 	
					}
				}
				*/

			}

			double normal[] = new double[3]; 
			
			// bounce when point on the horizontal line
			if (!deadPoint[i] && point[i][0]>bLine[0][0] && point[i][0]<bLine[1][0] && (point[i][1]-bLine[0][1])<0.5 && (bLine[0][1]-point[i][1])<0.5 ) {

				// change the direction of the point around 
				normal[0] = 0; normal[1] = 1; normal[2] = 0; 
				bounceOnLine(point[i], normal, direction[i]);
			}
			
			// bounce with edges 
			if (i>1) bounceOnLine1(point[i], direction[i],  point[0],  point[1]); 			
			if (i<2 || i>4) {
				bounceOnEdge(point[i], direction[i],  point[2],  point[3], point[4]); 
				bounceOnEdge(point[i], direction[i],  point[3],  point[4], point[2]); 
				bounceOnEdge(point[i], direction[i],  point[4],  point[2], point[3]); 
			}
			
			// deadpoint handling
			if ((bLine[0][1]-point[i][1])>1.5) {
				
				if (!deadPoint[i] && i>4) {
					numOfPoints--;
					deadPoint[i] = true; 
				}

				// change the direction of the point around 
				//if (i>4) deadPoint[i] = true;
				direction[i][0] = direction[i][0]*0.999;
				if (direction[i][1]>-1) direction[i][1]-= 0.001; 
				//direction[i][1] = direction[i][1]*0.999;
			}		
			
			// draw a colored point in Circle
			gl.glColor3dv(clr[i], 0);  // specify the color
			if (!deadPoint[i]) 
				drawPointCircle(point[i][0], point[i][1]);
			else if (Math.random()*100<50) {// randomly choose to draw the dead points 
				gl.glColor3f(0.1f,0.1f,0.1f);
				drawPointCircle(point[i][0], point[i][1]);
			}
			gl.glColor3f(0.9f,0.9f,0.9f);
			gl.glWindowPos2d(HEIGHT/20, 20);
			glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, "Alive: ");
			glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, Integer.toString(numOfPoints));

		}
		// sleep to slow down the rendering
		try {
			Thread.sleep(1);
		} catch (Exception ignore) {
		}
	}
	
	public void bounceOnEdge(double point[], double direction[], double v1[], double v2[], double v3[]){
		
		double dx, dy, tx, ty; 
		double normal[] = new double[3]; 
		double v12[] = new double[3]; 
		double v31[] = new double[3]; 
		double v32[] = new double[3]; 
		double vc[] = new double[3]; 
				
		// bounce when point on edges 
		// x = x0 + (xn - x0)t => tx = (x-x0)/dx 
		// y = y0 + (yn - y0)t => ty = (y-y0)/dy 
		dx = v2[0] - v1[0];
		dy = v2[1] - v1[1];
		tx = point[0] - v1[0];
		ty = point[1] - v1[1];
		
		tx = tx/dx; ty = ty/dy; 

		if (tx>=0 && tx<=1 && (tx-ty)*(tx-ty) < 0.002) {

			// bounce on the line
			for (int i=0; i<3; i++) {
				v12[i] = v2[i] - v1[i]; 
				v31[i] = v1[i] - v3[i]; 
				v32[i] = v2[i] - v3[i]; 
			}
			crossprod(v32, v31, vc);
			crossprod(vc, v12, normal); 
								
			bounceOnLine(point, normal, direction);
		}		
	}
	
	public void bounceOnLine1(double point[], double direction[], double v1[], double v2[]){
		
		double dx, dy, tx, ty; 
		double normal[] = new double[3]; 
				
		// bounce when point on edges 
		// x = x0 + (xn - x0)t => tx = (x-x0)/dx 
		// y = y0 + (yn - y0)t => ty = (y-y0)/dy 
		dx = v2[0] - v1[0];
		dy = v2[1] - v1[1];
		tx = point[0] - v1[0];
		ty = point[1] - v1[1];
		
		tx = tx/dx; ty = ty/dy; 
			
		if (tx>=0 && tx<=1 && (tx-ty)*(tx-ty) < 0.002) {
			// bounce on the line
			normal [0] = -dy; normal [1] = dx; normal[2] = 0; 
			if (dotprod(direction, normal)>0) { // normal is reversed
				normal [0] = dy; normal [1] = -dx; 					
			}
				
			bounceOnLine(point, normal, direction);
		}					
	}
	

	public void bounceOnLine(double point[], double n[], double direction[]) {
		double rd[] = new double[3];

		normalize(n);

		for (int i = 0; i < 3; i++) {
			rd[i] = -direction[i];
			point[i] += rd[i]; // so the point reverse a little 
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

	// when mouse is moved, we use it to update the line position
	public void keyPressed(KeyEvent e) { 
		int step = 4; 
		
		if (e.getKeyCode() == KeyEvent.VK_UP)
		{
			bLine[0][1] = bLine[0][1]+step;
			bLine[1][1] = bLine[1][1]+step;
	
		}
		if (e.getKeyCode() == KeyEvent.VK_DOWN)
		{
			bLine[0][1] = bLine[0][1]-step;
			bLine[1][1] = bLine[1][1]-step;
	
		}
		if (e.getKeyCode() == KeyEvent.VK_RIGHT)
		{
			bLine[0][0] = bLine[0][0]+step;
			bLine[1][0] = bLine[1][0]+step;
	
		}
		if (e.getKeyCode() == KeyEvent.VK_LEFT)
		{
			bLine[0][0] = bLine[0][0]-step;
			bLine[1][0] = bLine[1][0]-step;
	
		}

	}

	public void keyReleased(KeyEvent e) { 
	}
	
	public void keyTyped(KeyEvent e) { 
	}
	
	
	
	// specify to draw a point
	  public void drawPointCircle(double x, double y) {

		  gl.glPointSize(1);
		  gl.glBegin(GL.GL_POINTS);
		  gl.glVertex2d(x, y);
		  gl.glEnd();
		  drawCircle(x, y, cRadius); 
	  }

	
	
	public static void main(String[] args) {
		HW3_jchen_CircleGame f = new HW3_jchen_CircleGame();

		f.setTitle("HW3 - Circles bounce in a circle");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);

	}
}
