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
public class HW1_4_Bouncing extends HW1_2_PointInCircle {
	double left, right, bottom, top; // boundary 
	double cleft, cright, cbottom, ctop; // boundary 

	
	public HW1_4_Bouncing() {
		left=10; right=WIDTH-30; bottom=10; top=HEIGHT-50; // smaller boundary
		r = WIDTH/10; // smaller circle.
		cleft=left+r*2; cright=right-r*2; cbottom=bottom+r*2; ctop=top-r*2; // smaller clipping area

		for (int i = 0; i < pnum; i++) {
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
				if (point[i][0]<=left) point[i][0] = left+1;
				else point[i][0] = right-1;


				// change the direction of the point around x
				direction[i][0] = -direction[i][0];
				point[i][0] = point[i][0] + direction[i][0];
				point[i][1] = point[i][1] + direction[i][1];
			}
			if (point[i][1]<=bottom || point[i][1]>=top) {
				if (point[i][1]<=bottom) point[i][1] = bottom+1;
				else point[i][1] = top-1;

				// change the direction of the point around y
				direction[i][1] = -direction[i][1];
				point[i][0] = point[i][0] + direction[i][0];
				point[i][1] = point[i][1] + direction[i][1];
			}
			
			// draw a colored point in Circle
			gl.glColor3dv(clr[i], 0);  // specify the color

			gl.glPushMatrix();
			gl.glTranslated(point[i][0], point[i][1], 0); 
			drawPoint(0, 0); 
			//drawPoint(point[i][0], point[i][1]); 
			gl.glPopMatrix();
			
			for (int j=0; j<pnum-2; j++) { // point bounce off lines
				
				if (j%24 == 0 || j%48==2 || j%48==3  || j%48==4) { // line segment: j and j+1

					//Point c is on the line segment ab: 
					//If distance c to ab is close to zero, then a, b and c are aligned;
					//if (cb) dot (ca) is negative, c is between ab. 

					double ab[] = new double[3]; 
					double cb[] = new double[3]; 
					double ca[] = new double[3]; 
					double v[] = new double[3]; 

					// point bounce of line or edge
					ab[0] = point[j+1][0] - point[j][0]; 
					ab[1] = point[j+1][1] - point[j][1]; 
					cb[0] = point[j+1][0] - point[i][0]; 
					cb[1] = point[j+1][1] - point[i][1]; 
					
					if (j%48==4) { // actually circle back to the first vertex
						ab[0] = point[j-2][0] - point[j][0]; 
						ab[1] = point[j-2][1] - point[j][1]; 
						cb[0] = point[j-2][0] - point[i][0]; 
						cb[1] = point[j-2][1] - point[i][1]; 						
					}
					ab[2] = 0; 				
					cb[2] = 0; 
					ca[0] = point[j][0] - point[i][0]; 
					ca[1] = point[j][1] - point[i][1]; 
					ca[2] = 0; 
					
					if (length(ab)<2) continue; // short line
					if (length(cb)<2) continue; // at the end a line
					if (length(ca)<2) continue; 
					
					double tmp = dotprod(cb, ca); 
					crossprod(cb, ca, v); 
					double distance = length(v)/length(ab); //distance from a point c to a line ab
				
					if ((distance < 2 && tmp < 0)) { // bounce of line
						
						ab[0] = -point[j+1][1] + point[j][1]; // bounce normal
						ab[1] = point[j+1][0] - point[j][0]; 
						if (j%48==4) { // actually circle back to the first vertex
							ab[0] = -point[j-2][1] + point[j][1]; // bounce normal
							ab[1] = point[j-2][0] - point[j][0]; 
						}
						ab[2] = 0; 
						normalize(ab); // the normal perpendicular to ab
						
						// change the point moving direction for reflection
						// !!! here it is possible that the line and the point are moving in the same direction
						// then bouncing should not change the direction. It should move faster instead
						// I didn't deal with such a situation. ################### 
						v[0] = -direction[i][0]; 
						v[1] = -direction[i][1]; 
						v[2] = 0;
						
						point[i][0] = point[i][0]+v[0];
						point[i][1] = point[i][1]+v[1];					
						reflect(v, ab, direction[i]);// bounce off direction 
						// !!! also, if it is very horizontal with the line, it may not be able to bounce out
						// so it will vibrate alone the line. ################### 
												
						point[i][0] = point[i][0] + direction[i][0];
						point[i][1] = point[i][1] + direction[i][1];					
					}
					
					// point bounce of the circle 
					ab[0] = point[j+1][0] - point[j][0]; 
					ab[1] = point[j+1][1] - point[j][1]; 
					cb[0] = point[j+1][0] - cx; 
					cb[1] = point[j+1][1] - cy; 
					
					if (j%48==4) { // actually circle back to the first vertex
						ab[0] = point[j-2][0] - point[j][0]; 
						ab[1] = point[j-2][1] - point[j][1]; 
						cb[0] = point[j-2][0] - cx; 
						cb[1] = point[j-2][1] - cy; 						
					}
					ab[2] = 0; 				
					cb[2] = 0; 
					ca[0] = point[j][0] - cx; 
					ca[1] = point[j][1] - cy; 
					ca[2] = 0; 

					crossprod(cb, ca, v); 
					distance = length(v)/length(ab); //distance to a line
				
					if ((distance < r)) { // bounce of the circle
						 
						direction[j][0] = -direction[j][0]; 
						direction[j][1] = -direction[j][1]; 
						point[j][0] = point[j][0] + direction[j][0];
						point[j][1] = point[j][1] + direction[j][1];					
																
						if (j%48==4) { // actually circle back to the first vertex

							direction[j-2][0] = -direction[j-2][0]; 
							direction[j-2][1] = -direction[j-2][1]; 
							point[j-2][0] = point[j-2][0] + direction[j-2][0];
							point[j-2][1] = point[j-2][1] + direction[j-2][1];					
						} else {
							direction[j+1][0] = -direction[j+1][0]; 
							direction[j+1][1] = -direction[j+1][1]; 
							point[j+1][0] = point[j+1][0] + direction[j+1][0];
							point[j+1][1] = point[j+1][1] + direction[j+1][1];					
							
						}
					}

					
				}
			}
			
			
			if (i % 24 == 0 && i<pnum-2) clipLine(point[i], point[i+1], i); 			

			if (i % 48 == 2 && i < pnum-5) {
				clipTriangle(point[i], point[i+1], point[i+2], i);
				
			}
			

			
			//if (i % 48 == 3 && i < pnum-5) clipTriangle(point[i], point[i+1], point[i+2], i);
			
			// draw a white circle
			gl.glColor3f(1.0f, 1.0f, 1.0f);
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

			
			//drawLine( p11,  p22);
			//if (iv1==0) labelVertex((int) p11[0], (int) p11[1], i); 
			//if (iv2==0) labelVertex((int) p22[0], (int) p22[1], i); 
			
	  }


	  // draw a clipped line with vivid color
	  public void clipLine(double p1[], double p2[], int i) {
			double p11[] = new double[3]; 
			double p22[] = new double[3]; 
			double t; 
			int iv1=0, iv2=0; //indicator for whether label the vertex
			
			for (int j=0; j<3; j++) {
				p11[j] = p1[j];
				p22[j] = p2[j];				
			}
		  
			gl.glColor3d(0.5, 0.5, 0.5);
			drawLine( p1,  p2);
			// parametric line equation p = p1 + t*(p2-p1)
			
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
			point[i] += n[i]; // so the point move outside the circle
		}
		reflect(rd, n, direction); 
		while (r >= distance(cx, cy, point[0], point[1])) { // prevent it moves into the circle 
			point[0] += n[0];
			point[1] += n[1];
		}

	}


	public static void main(String[] args) {
		HW1_4_Bouncing f = new HW1_4_Bouncing();

		f.setTitle("HW1_4 -  bounce off");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);

	}
}
