package db2.esper.graphic2d;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.util.Random;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Map extends JPanel {

	public Map(int width, int height) {
		//setBackground(Color.BLUE);
		setSize(width, height);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
        doDrawing(g);
	}
	
	private void doDrawing(Graphics g) {

		Graphics2D g2d = (Graphics2D) g;
		
		g2d.setColor(Color.red);
		
		Dimension size = getSize();
		Insets insets = getInsets();
		
		int w = size.width - insets.left - insets.right;
		int h = size.height - insets.top - insets.bottom;
		
		Random r = new Random();
		
		for (int i = 0; i < 1000; i++) {
		
		    int x = Math.abs(r.nextInt()) % w;
		    int y = Math.abs(r.nextInt()) % h;
		    g2d.drawLine(x, y, x+10, y+10);
		}
    }

}