//import net.java.games.jogl.GL;
//import net.java.games.jogl.GLDrawable; 
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.sun.opengl.util.GLUT;

import java.awt.event.*; // adding mouse motion event

/**
 
 * <p>
 * Title: Foundations of 3D Graphics Programming : Using JOGL and Java3D
 * </p>
 * 
 * <p>
 * Description: spheres bounce in a circle and on edges ...
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2013
 * </p>
 * 
 * <p>
 * Company: George Mason University
 * </p>
 * 
 * @author Dr. Jim X. Chen
 * @version 1.0
 */
public class HW4_jchen_sphereCircleGame extends HW3_jchen_CircleGame {
		
	  static float sVdata[][] = { {1.0f, 0.0f, 0.0f}
      , {0.0f, 1.0f, 0.0f}
      , {0.0f, 0.0f, 1.0f}
      , {-1.0f, 0.0f, 0.0f}
      , {0.0f, -1.0f, 0.0f}
      , {0.0f, 0.0f, -1.0f}
	  };
	  long depth = 2; 

	
	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {

		super.reshape(drawable, x, y, w, h); 
		
		//1. specify drawing into only the back_buffer
		gl.glDrawBuffer(GL.GL_BACK); 
		gl.glEnable(GL.GL_DEPTH_TEST);
		   
		//2. origin at the center of the drawing area
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(0, w, 0 ,h , -w, w);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity(); 

		//3. interval to swap buffers to avoid rendering too fast
		gl.setSwapInterval(1);
	}
	
	// Called for OpenGL rendering every reshape
	public void display(GLAutoDrawable drawable) {
		
		int i, j; 

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

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

			}

			double normal[] = new double[3]; 
			
			// bounce when point on the horizontal line
			if (!deadPoint[i] && point[i][0]>bLine[0][0] && point[i][0]<bLine[1][0] && 
					(point[i][1]-bLine[0][1])<0.5+cRadius && (bLine[0][1]-point[i][1])<0.5+cRadius ) {

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
			}		
			
			// draw a colored point in Circle
			gl.glColor3dv(clr[i], 0);  // specify the color
			if (!deadPoint[i]) {
				drawPointCircle(point[i][0], point[i][1]);
				drawSphere(point[i][0], point[i][1]);
			}
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
	
	
	  private void subdivideSphere(
		      float v1[],
		      float v2[],
		      float v3[],
		      long depth, double x, double y) {
		    float v12[] = new float[3];
		    float v23[] = new float[3];
		    float v31[] = new float[3];
		    int i;

		    if (depth==0) {
		      gl.glColor3f(v1[0]*v1[0], v2[1]*v2[1], v3[2]*v3[2]);
		      drawtriangle(v1, v2, v3, x, y);
		      return;
		    }
		    for (i = 0; i<3; i++) {
		      v12[i] = v1[i]+v2[i];
		      v23[i] = v2[i]+v3[i];
		      v31[i] = v3[i]+v1[i];
		    }
		    normalize(v12);
		    normalize(v23);
		    normalize(v31);
		    subdivideSphere(v1, v12, v31, depth-1, x, y);
		    subdivideSphere(v2, v23, v12, depth-1, x, y);
		    subdivideSphere(v3, v31, v23, depth-1, x, y);
		    subdivideSphere(v12, v23, v31, depth-1, x, y);
		  }


		  public void drawSphere(double x, double y) {
			  
		    subdivideSphere(sVdata[0], sVdata[1], sVdata[2], depth, x, y);
		    subdivideSphere(sVdata[0], sVdata[2], sVdata[4], depth, x, y);
		    subdivideSphere(sVdata[0], sVdata[4], sVdata[5], depth, x, y);
		    subdivideSphere(sVdata[0], sVdata[5], sVdata[1], depth, x, y);

		    subdivideSphere(sVdata[3], sVdata[1], sVdata[5], depth, x, y);
		    subdivideSphere(sVdata[3], sVdata[5], sVdata[4], depth, x, y);
		    subdivideSphere(sVdata[3], sVdata[4], sVdata[2], depth, x, y);
		    subdivideSphere(sVdata[3], sVdata[2], sVdata[1], depth, x, y);
		  }
		  
			public void drawtriangle(float[] v1, float[] v2, float[] v3,
					double x, double y) {
				  float radius = (float) cRadius; 
	
				  float v11[] = new float[3]; // points
				  float v22[] = new float[3]; // points
				  float v33[] = new float[3]; // points


				v11[0] = v1[0]*radius + (float) x; 
				v11[1] = v1[1]*radius + (float) y; 
				v11[2] = v1[2]*radius; 
				v22[0] = v2[0]*radius + (float) x; 
				v22[1] = v2[1]*radius + (float) y; 
				v22[2] = v2[2]*radius; 

				v33[0] = v3[0]*radius + (float) x; 
				v33[1] = v3[1]*radius + (float) y; 
				v33[2] = v3[2]*radius; 
				
				gl.glBegin(GL.GL_TRIANGLES);
				gl.glVertex3fv(v11, 0);
				gl.glVertex3fv(v22, 0);
				gl.glVertex3fv(v33, 0);
				gl.glEnd();
			}
			


	
	public static void main(String[] args) {
		HW4_jchen_sphereCircleGame f = new HW4_jchen_sphereCircleGame();

		f.setTitle("HW4 - spheres bounce in a circle");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);

	}
}
