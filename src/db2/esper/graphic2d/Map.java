package db2.esper.graphic2d;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

import javax.swing.JPanel;

import db2.esper.common.Wall;
import db2.esper.engine.EsperEngine;

@SuppressWarnings("serial")
public class Map extends JPanel {
	private double conversionFactor;
	private Graphics2D graphics2d;
	private static boolean verbose = EsperEngine.VERBOSE;

	public Map(int width, int height) {
		//setBackground(Color.BLUE);
		setSize(width, height);
		graphics2d = (Graphics2D) getGraphics();
		conversionFactor = 0;

	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
	}
	
	/**
	 * Draws the walls loaded passed as argument
	 * @param walls, ArrayList<Wall>, this is the array with the starting and ending coordinates
	 * of the walls
	 */
	public void drawWalls(ArrayList<Wall> walls) {
		
		if (graphics2d == null)
			graphics2d = (Graphics2D) getGraphics();
		
		graphics2d.setColor(Color.black);
		
		Dimension size = getSize();
		Insets insets = getInsets();
		
		int width = size.width - insets.left - insets.right;
		int height = size.height - insets.top - insets.bottom;

		double maxX = 0, 
			   maxY = 0;
		
		// trovo le coordinate massime utili per la normalizzazione
		for (Wall wall : walls) {
			if (maxX < wall.getStartX()) {
				maxX = wall.getStartX();
			}
			if (maxX < wall.getEndX()) {
				maxX = wall.getEndX();
			}
			if (maxY < wall.getStartY()) {
				maxY = wall.getStartY();
			}
			if (maxY < wall.getEndY()) {
				maxY = wall.getEndY();
			}
		}
		
		if ( verbose ) { //DEBUG
			System.out.println("MAX X: " + maxX); 
			System.out.println("MAX Y: " + maxY);
		}
		
		conversionFactor = (maxX > maxY)? width / maxX : height / maxY ;
		
		// normalizzo i valori e passo tutto in pixel
		for (Wall wall : walls) {
			int x1 = (int) (wall.getStartX() * conversionFactor);
			int y1 = (int) (wall.getStartY() * conversionFactor);
			int x2 = (int) (wall.getEndX() * conversionFactor);
			int y2 = (int) (wall.getEndY() * conversionFactor);			
			
			if (verbose) //DEBUG 
				System.out.println("COORDINATES: " + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ", ");		

			graphics2d.drawLine(x1, y1, x2, y2);	
		}
	}
	
	/**
	 * Draws a red Circle in the given position with the given range
	 * @param x, double, X coordinate
	 * @param y, double, Y coordinate
	 * @param radius, double, circle range
	 */
	public void drawInPosition(double x, double y, double radius) {
		if (graphics2d == null)
			graphics2d = (Graphics2D) getGraphics();
		
		graphics2d.setColor(Color.RED);
		
		Shape shape = new Ellipse2D.Double(
				(x-radius)*conversionFactor, 
				(y-radius)*conversionFactor, 
				(2.0*radius)*conversionFactor, 
				(2.0*radius)*conversionFactor
				);
		
		graphics2d.draw(shape);
	}


}