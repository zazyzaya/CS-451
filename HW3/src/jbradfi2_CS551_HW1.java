/*
 * Jim Bradfield
 * CS 551, Fall 2013
 * Homework 1 - Bouncing Points
 * 
 * While we could write this by simply extending the J* reference classes, (a) keeping track
 * of the constant extending and overwriting seemed an unnecessary hassle, and (b) it seemed
 * more valuable, both educationally and in terms of creating a solid object-oriented basis
 * to expand upon in later assignments, to write the whole thing from scratch.  Keeping
 * multiple classes in one file certainly isn't best practice, but it still works and the
 * compartmentalized functionality should be useful in the future.
 */

import java.awt.*;
import java.awt.event.*;
import javax.media.opengl.*;
import com.sun.opengl.util.Animator;
import com.sun.opengl.util.FPSAnimator;

public class jbradfi2_CS551_HW1 {
	
	static int WIDTH = 640;
	static int HEIGHT = 480;
	

	public static void main(String[] args) {
		jbradfi2_CS551_HW1 tmp; 
		tmp = new jbradfi2_CS551_HW1(); 
		 GLFrame f = tmp.new GLFrame();
		
		f.setTitle("CS551 Homework 1 - Jim Bradfield, Fall 2013");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);
	}


class GLFrame extends Frame implements GLEventListener {
	private GL gl;
	private GLCanvas canvas;
	private Animator animator;
	private final int NUM_POINTS = 50;

	private MobilePoint[] points;
	private Circle circle;
	private Rectangle rect;

	public GLFrame() {
		this.canvas = new GLCanvas();
		this.canvas.addGLEventListener(this);
		this.add(canvas, BorderLayout.CENTER);
		this.gl = canvas.getGL();

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				animator.stop();
				System.exit(0);
			}
		});
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		// Set double-buffering, then set BG to black
		gl.glDrawBuffer(GL.GL_FRONT_AND_BACK);
		gl.glClearColor(0f, 0f, 0f, 0f);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		
		// scale the circle against the smallest dimension so the whole thing fits in frame
		int smallestDim = Math.min(canvas.getWidth(), canvas.getHeight());
		circle = new Circle(canvas.getWidth() / 2, canvas.getHeight() /2, smallestDim / 3);
		rect = new Rectangle(10.0, 10.0, canvas.getWidth() - 20.0, canvas.getHeight() - 20.0);

		points = new MobilePoint[NUM_POINTS];
		for (int i = 0; i < points.length; i++) {
			points[i] = new MobilePoint(Math.random() * canvas.getWidth(),
					Math.random() * canvas.getHeight(), 3 * Math.random(), 
					3 * Math.random(), new Color(Math.random(), Math.random(), Math.random()));
			// Remarks: There's certainly a more rigorous way to generate points outside of the 
			// circle, but the time required to just spam points until they land in the right
			// place is negligible so whatever.
			while(circle.contains(points[i].getPosition()) 
					|| !rect.contains(points[i].getPosition())) {
				points[i].setPosition(new Vector2(Math.random() * canvas.getWidth(),
								Math.random() * canvas.getHeight()));
			}
		}
				
		animator = new FPSAnimator(canvas, 60);
		animator.start();
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		update();
		draw(drawable);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(0, width, 0, height, -1f, 1f);
		gl.glViewport(0, 0, width, height);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
	}

	@Override
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged) {
	}
	
	public void update() {
		for (MobilePoint point : points) {
			point.update();
			
			// Handle bouncing off the circle.
			if (circle.contains(point.getPosition())) {
				Vector2 normal = new Vector2(point.getPosition().X - circle.getPosition().X,
						point.getPosition().Y - circle.getPosition().Y);
				point.setVelocity(point.getVelocity().reflectAround(normal).negate());
			}
			
			// Handle bouncing off the rectangle
			if (point.getPosition().X < rect.getPosition().X 
					|| point.getPosition().X > rect.getPosition().X + rect.getWidth())
				point.setVelocity(-point.getVelocity().X, point.getVelocity().Y);
			if (point.getPosition().Y < rect.getPosition().Y 
					|| point.getPosition().Y > rect.getPosition().Y + rect.getHeight())
				point.setVelocity(point.getVelocity().X, -point.getVelocity().Y);
		}
	}
	
	public void draw(GLAutoDrawable drawable) {
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		circle.draw(gl);
		rect.draw(gl);
		for (MobilePoint point : points) {
			point.draw(gl);
		}
	}
}
}

class Vector2 {
	public double X;
	public double Y;

	public Vector2() {
		this.X = 0.0;
		this.Y = 0.0;
	}

	public Vector2(double x, double y) {
		this.X = x;
		this.Y = y;
	}

	public void set(double x, double y) {
		this.X = x;
		this.Y = y;
	}
	
	public double length() {
		return Math.sqrt(X*X + Y*Y);
	}
	
	public Vector2 normalize() {
		double d = this.length();
		return new Vector2(X / d, Y / d);
	}
	
	public Vector2 negate() {
		return new Vector2(-X, -Y);
	}
	
	public Vector2 scale(double scalar) {
		return new Vector2(scalar * X, scalar * Y);
	}
	
	public Vector2 add(Vector2 vec) {
		return new Vector2(this.X + vec.X, Y + vec.Y);
	}
	
	public double dotProduct(Vector2 vec) {
		return this.X * vec.X + this.Y * vec.Y;
	}
	
	public Vector2 reflectAround(Vector2 vec) {
		Vector2 normal = vec.normalize();
		return normal.scale(2.0 * normal.dotProduct(this)).add(this.negate());
	}
}

class Color {
	public double Red;
	public double Green;
	public double Blue;
	
	public Color() {
		this.Red = 1.0;
		this.Green = 1.0;
		this.Blue = 1.0;
	}
	
	public Color(double r, double g, double b) {
		this.Red = r;
		this.Green = g;
		this.Blue = b;
	}
	
	public void glColor(GL gl) {
		gl.glColor3d(Red, Green, Blue);
	}
}

class MobilePoint {
	private Vector2 position;
	private Vector2 velocity;
	private Color color;

	public MobilePoint() {
		position = new Vector2();
		velocity = new Vector2();
		color = new Color();
	}

	public MobilePoint(double x, double y, double velX, double velY) {
		this(x, y, velX, velY, new Color());
	}
	
	public MobilePoint(double x, double y, double velX, double velY, Color color) {
		this(new Vector2(x, y), new Vector2(velX, velY), color);
	}

	public MobilePoint(Vector2 pos, Vector2 vel) {
		this(pos, vel, new Color());
	}
	
	public MobilePoint(Vector2 pos, Vector2 vel, Color color) {
		this.position = pos;
		this.velocity = vel;
		this.color = color;
	}

	public Vector2 getPosition() {
		return position;
	}
	
	public void setPosition(double x, double y) {
		this.setPosition(new Vector2(x, y));
	}

	public void setPosition(Vector2 position) {
		this.position = position;
	}

	public Vector2 getVelocity() {
		return velocity;
	}

	public void setVelocity(double x, double y) {
		this.setVelocity(new Vector2(x, y));
	}
	
	public void setVelocity(Vector2 velocity) {
		this.velocity = velocity;
	}
	
	public void update() {
		position = position.add(velocity);
	}
	
	public void draw(GL gl) {
		gl.glPointSize(5);
		gl.glBegin(GL.GL_POINTS);
			color.glColor(gl);
			gl.glVertex2d(position.X, position.Y);
		gl.glEnd();
	}
}

 class Circle {
	private Vector2 position;
	private double radius;
	private Color color;
	
	public Circle() {
		this.position = new Vector2(0, 0);
		this.radius = 1;
	}
	
	public Circle(double x, double y, double r) {
		this(new Vector2(x, y), r);
	}
	
	public Circle(double x, double y, double r, Color color) {
		this(new Vector2(x, y), r, color);
	}
	
	public Circle(Vector2 p, double r) {
		this(p, r, new Color());
	}
	
	public Circle(Vector2 p, double r, Color color) {
		this.position = p;
		this.radius = r;
		this.color = color;
	}

	public Vector2 getPosition() {
		return position;
	}

	public void setPosition(Vector2 position) {
		this.position = position;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}
	
	public boolean contains(Vector2 point) {
		Vector2 distance = new Vector2(point.X - position.X, point.Y - position.Y);
		if (distance.length() <= radius) return true;
		return false;
	}
	
	public void draw(GL gl) {
		double incr = 1f / radius;
		double theta = 0.0;
		
		gl.glPointSize(1);
		gl.glBegin(GL.GL_POINTS);
			color.glColor(gl);
			while (theta < 2.0 * Math.PI) {
				gl.glVertex2d(position.X + radius * Math.cos(theta),
						      position.Y + radius * Math.sin(theta));
				theta += incr;
			}
		gl.glEnd();
	}
}

class Rectangle {
	private Vector2 position;
	private double width;
	private double height;
	private Color color;
	
	public Rectangle() {
		this(0.0, 0.0, 1.0, 1.0);
	}
	
	public Rectangle(double x, double y, double width, double height) {
		this(new Vector2(x, y), width, height);
	}
	
	public Rectangle(Vector2 pos, double width, double height) {
		this(pos, width, height, new Color());
	}
	
	public Rectangle(Vector2 pos, double width, double height, Color color) {
		this.position = pos;
		this.width = width;
		this.height = height;
		this.color = color;
	}

	public Vector2 getPosition() {
		return position;
	}

	public void setPosition(Vector2 position) {
		this.position = position;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}
	
	public boolean contains(Vector2 point) {
		if (position.X < point.X && point.X < position.X + width
				&& position.Y < point.Y && point.Y < position.Y + height)
			return true;
		return false;
	}
	
	public void draw(GL gl) {
		gl.glLineWidth(1);
		gl.glBegin(GL.GL_LINES);
			color.glColor(gl);
			gl.glVertex2d(position.X, position.Y);
			gl.glVertex2d(position.X + width, position.Y);
			gl.glVertex2d(position.X + width, position.Y);
			gl.glVertex2d(position.X + width, position.Y + height);
			gl.glVertex2d(position.X + width, position.Y + height);
			gl.glVertex2d(position.X, position.Y + height);
			gl.glVertex2d(position.X, position.Y + height);
			gl.glVertex2d(position.X, position.Y);
		gl.glEnd();
	}
}