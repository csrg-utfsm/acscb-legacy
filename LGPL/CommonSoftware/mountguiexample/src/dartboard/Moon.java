package dartboard;

import java.awt.*;
import javax.swing.ImageIcon;
/**
 * Insert the type's description here.
 * Creation date: (11/7/00 9:03:30 PM)
 * @author: 
 */
public class Moon extends Planet {
	public static short NO_MOON = -1;
	public static short EMPTY_MOON = 0;
	public static short FIRST_QUARTER = 1;
	public static short HALF_MOON = 2;
	public static short THIRD_QUARTER = 3;
	public static short FULL_MOON = 4;
	
	private short phase = FULL_MOON;

	private ImageIcon[] icons = new ImageIcon[5];
/**
 * Moon constructor comment.
 * @param x int
 * @param y int
 */
public Moon() {
	this(Moon.THIRD_QUARTER);
}
/**
 * Moon constructor comment.
 * @param x int
 * @param y int
 */
public Moon(short phase) {
	super();
	this.phase = phase;

	for (int i = 0; i < 5; i++) {
		java.net.URL url = this.getClass().getClassLoader().getSystemResource("Dartboard/moon" + i + ".gif");
		if (url != null) icons[i] = new javax.swing.ImageIcon(url);
	}
}
protected void drawDart(Graphics g) {
	g.setColor(Color.darkGray);

	if (phase != NO_MOON) g.fillOval(-16, -18, 35, 37);

	if (phase == EMPTY_MOON) {
		if (icons[0] != null) icons[0].paintIcon(dartboard, g, -icons[0].getIconWidth() / 2, -icons[0].getIconHeight() / 2);
	}
	else if (phase == FIRST_QUARTER) {
		if (icons[1] != null) icons[1].paintIcon(dartboard, g, -icons[1].getIconWidth() / 2, -icons[1].getIconHeight() / 2);
	}
	else if (phase == HALF_MOON) {
		if (icons[2] != null) icons[2].paintIcon(dartboard, g, -icons[2].getIconWidth() / 2, -icons[2].getIconHeight() / 2);
	}
	else if (phase == THIRD_QUARTER) {
		if (icons[3] != null) icons[3].paintIcon(dartboard, g, -icons[3].getIconWidth() / 2, -icons[3].getIconHeight() / 2);
	}
	else if (phase == FULL_MOON) {
		if (icons[4] != null) icons[4].paintIcon(dartboard, g, -icons[4].getIconWidth() / 2, -icons[4].getIconHeight() / 2);
	}
	
	if (phase != NO_MOON) g.drawOval(-16, -18, 34, 36);
}
public short getPhase(short phase) {
	return phase;
}
public void setPhase(short phase) {
	this.phase = phase;
}
}
