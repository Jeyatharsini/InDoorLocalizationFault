package db2.esper.graphic2d;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JPanel;

import db2.esper.common.Wall;

@SuppressWarnings("serial")
public class Map extends JPanel {
	
	public Map(int width, int height) {
		//setBackground(Color.BLUE);
		setSize(width, height);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
        //doDrawing(g);
	}
	
	public void drawWalls(ArrayList<Wall> walls) {
		Graphics2D graphics2d = (Graphics2D) getGraphics();
		
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
		
		System.out.println("MAX X: " + maxX); 
		System.out.println("MAX Y: " + maxY);
		
		double XConversion = width / maxX;
		double YConversion = height / maxY;
		
		// normalizzo i valori e passo tutto in pixel
		for (Wall wall : walls) {
			int x1 = (int) (wall.getStartX() * XConversion);
			int y1 = (int) (wall.getStartY() * YConversion);
			int x2 = (int) (wall.getEndX() * XConversion);
			int y2 = (int) (wall.getEndY() * YConversion);
			
			System.out.println("COORDINATES: " + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ", ");		

			graphics2d.drawLine(x1, y1, x2, y2);
			
		}
		
		
	}
	

}