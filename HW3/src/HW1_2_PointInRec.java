//import net.java.games.jogl.GL;
//import net.java.games.jogl.GLDrawable; 
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.sun.opengl.util.GLUT;

/**
 * 
 * 
 * <p>
 * Title: Foundations of 3D Graphics Programming : Using JOGL and Java3D
 * </p>
 * 
 * <p>
 * Description: a point bounces in a circle ...
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
public class HW1_2_PointInRec extends HW1_2_PointInCircle {
	double left, right, bottom, top; // boundary 
	double cleft, cright, cbottom, ctop; // boundary 

	
	public HW1_2_PointInRec() {
		left=10; right=WIDTH-30; bottom=10; top=HEIGHT-50; // smaller boundary
		r = WIDTH/5; // smaller circle.
		cleft=left+r/2; cright=right-r/2; cbottom=bottom+r/2; ctop=top-r/2; // smaller clipping area

		for (int i = 0; i < pnum; i++) {
			// random moving direction
			direction[i][0] = Math.random()-0.5;
			direction[i][1] = Math.random()-0.5;
			direction[i][2] = 0;
			normalize(direction[i]); // make it a unit vector

			// randomly generated colors
			clr[i][0] = Math.random();
			clr[i][1] = Math.random();
			clr[i][2] = Math.random();

			// generate a point outside the circle and inside the rectangle
			do {
				point[i][0] = Math.random() * WIDTH;
				point[i][1] = Math.random() * HEIGHT;
				point[i][2] = 0;
			} while (r > distance(cx, cy, point[i][0], point[i][1]) 
					  || (point[i][0]<left || point[i][0]>right)
					  || (point[i][1]<bottom || point[i][1]>top));
		}
	}
	
	
	// Called for OpenGL rendering every reshape
	public void display(GLAutoDrawable drawable) {

		cnt++; 
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);

		gl.glPointSize(4);
		for (int i = 0; i < pnum; i++) {
			// move in a direction
			point[i][0] = point[i][0] + direction[i][0];
			point[i][1] = point[i][1] + direction[i][1];

			// bounce when point on/outside the circle
			if (r >= distance(cx, cy, point[i][0], point[i][1])) {

				// change the direction of the point around r
				bounceInCircle(point[i], direction[i]);
			}
			
			// bounce when point on/outside the rectangle 
			if (point[i][0]<=left || point[i][0]>=right) {

				// change the direction of the point around x
				direction[i][0] = -direction[i][0];
				point[i][0] = point[i][0] + direction[i][0];
				point[i][1] = point[i][1] + direction[i][1];
			}
			if (point[i][1]<=bottom || point[i][1]>=top) {

				// change the direction of the point around x
				direction[i][1] = -direction[i][1];
				point[i][0] = point[i][0] + direction[i][0];
				point[i][1] = point[i][1] + direction[i][1];
			}
			
			// draw a colored point if it is inside clipping
			if (point[i][1]>cbottom && point[i][1]<ctop 
					&& point[i][0]>cleft && point[i][0]<cright) 
				gl.glColor3dv(clr[i], 0);  // specify the color

			drawPoint(point[i][0], point[i][1]); 

			if (i % 24 == 0) clipLine(point[i], point[i+1], i); 
	//		
			if (i % 48 == 3 && i < pnum-5) clipTriangle(point[i], point[i+1], point[i+2], i);
			
			// draw a white circle
			gl.glColor3f(0.5f, 0.5f, 0.5f);
			gl.glPointSize(2);
			drawCircle(cx, cy, r);
			drawRectangle(left, right, bottom, top);
			drawRectangle(cleft, cright, cbottom, ctop);

			
		}
		// sleep to slow down the rendering
		try {
			Thread.sleep(10);
		} catch (Exception ignore) {
		}
	}
	

	
	public void drawRectangle(double left, double right, double bottom, double top) {
		gl.glBegin(GL.GL_LINE_LOOP);
			gl.glVertex2d(left, bottom);
			gl.glVertex2d(right, bottom);
			gl.glVertex2d(right, top);
			gl.glVertex2d(left, top);
		gl.glEnd();
	}

	public void labelVertex(double x, double y, int i) {

		    // label the vertex
		    gl.glRasterPos3d(x, y, 0); // start poistion
		    glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, "v[");
		    glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, Integer.toString(i));
		    glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, "]");
	}
	
	// specify to draw a Triangle
	  public void drawTriangle(double p1[], double p2[], double p3[]) {

	  gl.glBegin(GL.GL_TRIANGLES);
	  gl.glVertex2d(p1[0], p1[1]); 
	  gl.glVertex2d(p2[0], p2[1]);
	  gl.glVertex2d(p3[0], p3[1]);
	  gl.glEnd();
	}


	  
	  // draw a clipped triangle with vivid color
	  public void clipTriangle(double p1[], double p2[], double p3[], int i) {
			double p0[][] = new double[7][3]; //generated vertex list
			double p[][] = new double[7][3]; //generated final vertex list
			int iv[] = new int[6]; // indicator whether to label the vertex 
			double t; // for parametric equation
			int vcnt, vc=3; // vertex count of final polygon
					  
			gl.glColor3d(0.5, 0.5, 0.5);
			drawTriangle( p1,  p2, p3);
			// parametric line equation p = p1 + t*(p2-p1)
			
			//initial vertex list
			for (int j=0; j<3; j++) {
				p0[0][j] = p1[j];
				p0[1][j] = p2[j];
				p0[2][j] = p3[j];
			}
			vc = 3;  vcnt = 0; 				
			
			//left boundary
			for (int j=0; j<vc; j++) { //walk through the vertices
				if (p0[j][0] < cleft) { // start walking outside
					if (p0[(j+1) % vc][0] > cleft) { // outside->inside: add the intersection and the next vertex 
						// find the intersection 
						t = (cleft-p0[j][0])/(p0[(j+1) % vc][0]-p0[j][0]);
						p[vcnt][0] = cleft;  
						p[vcnt][1] = p0[j][1] + t*(p0[(j+1) % vc][1]-p0[j][1]); 
						vcnt++; 
						p[vcnt][0] = p0[(j+1) % vc][0]; 
						p[vcnt][1] = p0[(j+1) % vc][1]; 
						vcnt++; 
					}			
					//outside->outside: ignore
				}
				else { //start walking inside
					if (p0[(j+1) % vc][0] > cleft) { // inside->inside: add the vertex 
						p[vcnt][0] = p0[(j+1) % vc][0]; 
						p[vcnt][1] = p0[(j+1) % vc][1]; 
						vcnt++; 
					} else { //inside->outside: add the intersection
						// find the intersection 
						t = (cleft-p0[j][0])/(p0[(j+1) % vc][0]-p0[j][0]);
						p[vcnt][0] = cleft;  
						p[vcnt][1] = p0[j][1] + t*(p0[(j+1) % vc][1]-p0[j][1]); 
						vcnt++; 
					}				
				}				
			}
			
			for (int k=0; k<vcnt; k++) {
				for (int j=0; j<3; j++) {
					p0[k][j] = p[k][j];
					p0[k][j] = p[k][j];
					p0[k][j] = p[k][j];
				}
			}
			vc = vcnt;  vcnt = 0; 				

			//right boundary
			for (int j=0; j<vc; j++) { //walk through the vertices
				if (p0[j][0] > cright) { // start walking outside
					if (p0[(j+1) % vc][0] < cright) { // outside->inside: add the intersection and the next vertex 
						// find the intersection 
						t = (cright-p0[j][0])/(p0[(j+1) % vc][0]-p0[j][0]);
						p[vcnt][0] = cright;  
						p[vcnt][1] = p0[j][1] + t*(p0[(j+1) % vc][1]-p0[j][1]); 
						vcnt++; 
						p[vcnt][0] = p0[(j+1) % vc][0]; 
						p[vcnt][1] = p0[(j+1) % vc][1]; 
						vcnt++; 
					}			
					//outside->outside: ignore
				}
				else { //start walking inside
					if (p0[(j+1) % vc][0] < cright) { // inside->inside: add the vertex 
						p[vcnt][0] = p0[(j+1) % vc][0]; 
						p[vcnt][1] = p0[(j+1) % vc][1]; 
						vcnt++; 
					} else { //inside->outside: add the intersection
						// find the intersection 
						t = (cright-p0[j][0])/(p0[(j+1) % vc][0]-p0[j][0]);
						p[vcnt][0] = cright;  
						p[vcnt][1] = p0[j][1] + t*(p0[(j+1) % vc][1]-p0[j][1]); 
						vcnt++; 
					}				
				}				
			}	
			
			
			for (int k=0; k<vcnt; k++) {
				for (int j=0; j<3; j++) {
					p0[k][j] = p[k][j];
					p0[k][j] = p[k][j];
					p0[k][j] = p[k][j];
				}
			}
			vc = vcnt;  vcnt = 0; 				

			//bottom boundary
			for (int j=0; j<vc; j++) { //walk through the vertices
				if (p0[j][1] < cbottom) { // start walking outside
					if (p0[(j+1) % vc][1] > cbottom) { // outside->inside: add the intersection and the next vertex 
						// find the intersection 
						t = (cbottom-p0[j][1])/(p0[(j+1) % vc][1]-p0[j][1]);
						p[vcnt][1] = cbottom;  
						p[vcnt][0] = p0[j][0] + t*(p0[(j+1) % vc][0]-p0[j][0]); 
						vcnt++; 
						p[vcnt][0] = p0[(j+1) % vc][0]; 
						p[vcnt][1] = p0[(j+1) % vc][1]; 
						vcnt++; 
					}			
					//outside->outside: ignore
				}
				else { //start walking inside
					if (p0[(j+1) % vc][1] > cbottom) { // inside->inside: add the vertex 
						p[vcnt][0] = p0[(j+1) % vc][0]; 
						p[vcnt][1] = p0[(j+1) % vc][1]; 
						vcnt++; 
					} else { //inside->outside: add the intersection
						// find the intersection 
						t = (cbottom-p0[j][1])/(p0[(j+1) % vc][1]-p0[j][1]);
						p[vcnt][1] = cbottom;  
						p[vcnt][0] = p0[j][0] + t*(p0[(j+1) % vc][0]-p0[j][0]); 
						vcnt++; 
					}				
				}				
			}

			for (int k=0; k<vcnt; k++) {
				for (int j=0; j<3; j++) {
					p0[k][j] = p[k][j];
					p0[k][j] = p[k][j];
					p0[k][j] = p[k][j];
				}
			}
			vc = vcnt;  vcnt = 0; 				

			//top boundary
			for (int j=0; j<vc; j++) { //walk through the vertices
				if (p0[j][1] > ctop) { // start walking outside
					if (p0[(j+1) % vc][1] < ctop) { // outside->inside: add the intersection and the next vertex 
						// find the intersection 
						t = (ctop-p0[j][1])/(p0[(j+1) % vc][1]-p0[j][1]);
						p[vcnt][1] = ctop;  
						p[vcnt][0] = p0[j][0] + t*(p0[(j+1) % vc][0]-p0[j][0]); 
						vcnt++; 
						p[vcnt][0] = p0[(j+1) % vc][0]; 
						p[vcnt][1] = p0[(j+1) % vc][1]; 
						vcnt++; 
					}			
					//outside->outside: ignore
				}
				else { //start walking inside
					if (p0[(j+1) % vc][1] < ctop) { // inside->inside: add the vertex 
						p[vcnt][0] = p0[(j+1) % vc][0]; 
						p[vcnt][1] = p0[(j+1) % vc][1]; 
						vcnt++; 
					} else { //inside->outside: add the intersection
						// find the intersection 
						t = (ctop-p0[j][1])/(p0[(j+1) % vc][1]-p0[j][1]);
						p[vcnt][1] = ctop;  
						p[vcnt][0] = p0[j][0] + t*(p0[(j+1) % vc][0]-p0[j][0]); 
						vcnt++; 
					}				
				}				
			}


			// draw clipped polygon
			gl.glColor3dv(clr[i], 0);  // specify the color
			gl.glBegin(GL.GL_POLYGON);
			for (int j=0; j<vcnt; j++) {
				gl.glVertex3dv(p[j],0); 
			}
			gl.glEnd();
			for (int j=0; j<vcnt; j++) {
				labelVertex(p[j][0], p[j][1], j); 
			}

	  }


	  // draw a clipped line with vivid color
	  public void clipLine(double p1[], double p2[], int i) {
			double p11[] = new double[3]; 
			double p22[] = new double[3]; 
			double t; 
			int iv1=0, iv2=0; //indicator for whether labeling the vertex
			
			for (int j=0; j<3; j++) {
				p11[j] = p1[j];
				p22[j] = p2[j];				
			}
		  
			gl.glColor3d(0.5, 0.5, 0.5);
			drawLine( p1,  p2);
			// parametric line equation:  p = p1 + t*(p2-p1)
			
			//left and right
			if (p11[0]<cleft) {
				t = (cleft-p11[0])/(p22[0]-p11[0]);
				p11[0] = cleft;  iv1++; 
				p11[1] = p11[1] + t*(p22[1]-p11[1]); 
			}
			if (p11[0]>cright) {
				t = (cright-p11[0])/(p22[0]-p11[0]);
				p11[0] = cright;  iv1++; 
				p11[1] = p11[1] + t*(p22[1]-p11[1]); 
			}
			if (p22[0]<cleft) {
				t = (cleft-p22[0])/(p11[0]-p22[0]);
				p22[0] = cleft;  iv2++; 
				p22[1] = p22[1] + t*(p11[1]-p22[1]); 
			}
			if (p22[0]>cright) {
				t = (cright-p22[0])/(p11[0]-p22[0]);
				p22[0] = cright;  iv2++; 
				p22[1] = p22[1] + t*(p11[1]-p22[1]); 
			}
			// bottom and top  
			if (p11[1]<cbottom) {
				t = (cbottom-p11[1])/(p22[1]-p11[1]);
				p11[1] = cbottom;  iv1++; 
				p11[0] = p11[0] + t*(p22[0]-p11[0]); 
			}
			if (p11[1]>ctop) {
				t = (ctop-p11[1])/(p22[1]-p11[1]);
				p11[1] = ctop;  iv1++; 
				p11[0] = p11[0] + t*(p22[0]-p11[0]); 
			}
			if (p22[1]<cbottom) {
				t = (cbottom-p22[1])/(p11[1]-p22[1]);
				p22[1] = cbottom;  iv2++; 
				p22[0] = p22[0] + t*(p11[0]-p22[0]); 
			}
			if (p22[1]>ctop) {
				t = (ctop-p22[1])/(p11[1]-p22[1]);
				p22[1] = ctop;  iv2++; 
				p22[0] = p22[0] + t*(p11[0]-p22[0]); 
			}

			
			// draw clipped line
			gl.glColor3dv(clr[i], 0);  // specify the color
			drawLine( p11,  p22);
			if (iv1==0) labelVertex(p11[0], p11[1], i); 
			if (iv2==0) labelVertex(p22[0], p22[1], i); 
			
	  }
		

	  
	// specify to draw a Line
	  public void drawLine(double p1[], double p2[]) {

	  gl.glBegin(GL.GL_LINES);
	  gl.glVertex2d(p1[0], p1[1]);
	  gl.glVertex2d(p2[0], p2[1]);
	  gl.glEnd();
	}

	
	public void bounceInCircle(double point[], double direction[]) {
		double n[] = new double[3];
		double rd[] = new double[3];

		//n: a vector from point to origin
		n[0] =  point[0] - cx;
		n[1] = point[1] - cy;
		n[2] = 0;
		normalize(n);

		for (int i = 0; i < 3; i++) {
			rd[i] = -direction[i];
			point[i] += n[i]; // so the point remains in circle
		}
		reflect(rd, n, direction); 
	}


	public static void main(String[] args) {
		HW1_2_PointInRec f = new HW1_2_PointInRec();

		f.setTitle("HW1_2 - points bounce in a rectangle");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);

	}
}
