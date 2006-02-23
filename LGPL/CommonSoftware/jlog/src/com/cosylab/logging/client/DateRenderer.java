/*
 *    ALMA - Atacama Large Millimiter Array
 *    (c) European Southern Observatory, 2002
 *    Copyright by ESO (in the framework of the ALMA collaboration)
 *    and Cosylab 2002, All rights reserved
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *    MA 02111-1307  USA
 */
package com.cosylab.logging.client;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Date;

import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
/**
 * DateRenderer defines the label for displaying the time format.
 * Creation date: (1/22/02 4:15:49 PM)
 * @author: 
 */
public class DateRenderer extends javax.swing.JLabel implements javax.swing.table.TableCellRenderer {

	protected static javax.swing.border.Border noFocusBorder;
	private boolean isSelected;
	private boolean hasFocus;
	private Color bColor;
	private Color fColor;

/**
 * DateRenderer constructor comment.
 */
public DateRenderer() {
	super();
	setPreferredSize(new java.awt.Dimension(18, 18));
	setOpaque(true);
	noFocusBorder = new EmptyBorder(1, 2, 1, 2);
	setBorder(noFocusBorder);


}
	/**
	 *  This method is sent to the renderer by the drawing table to
	 *  configure the renderer appropriately before drawing.  Return
	 *  the Component used for drawing.
	 *
	 * @param	table		the JTable that is asking the renderer to draw.
	 *				This parameter can be null.
	 * @param	value		the value of the cell to be rendered.  It is
	 *				up to the specific renderer to interpret
	 *				and draw the value.  eg. if value is the
	 *				String "true", it could be rendered as a
	 *				string or it could be rendered as a check
	 *				box that is checked.  null is a valid value.
	 * @param	isSelected	true is the cell is to be renderer with
	 *				selection highlighting
	 * @param	row	        the row index of the cell being drawn.  When
	 *				drawing the header the rowIndex is -1.
	 * @param	column	        the column index of the cell being drawn
	 */
public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	this.isSelected = isSelected;
	this.hasFocus = hasFocus;

	if (isSelected) {
	    fColor = table.getSelectionForeground();
	    bColor = table.getSelectionBackground();
	} else {
	    fColor = table.getForeground();
	    bColor = table.getBackground();
	}
	setForeground(fColor);
	setBackground(bColor);
	
	setFont(table.getFont());
	if (value == null)
		return this;
		
	java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS");
//    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh':'mm':'ss");
	setText(sdf.format((Date)value));
	
	if (hasFocus) {
		setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
		if (table.isCellEditable(row, column)) {
			setForeground(UIManager.getColor("Table.focusCellForeground"));
			setBackground(UIManager.getColor("Table.focusCellBackground"));
		}
	} else {
		setBorder(noFocusBorder);
	}
	return this;
}
public void paint( Graphics g )
	{
		g.setColor( bColor );

		g.fillRect( 0, 0, getWidth() - 1, getHeight() - 1 );

		super.paint( g );
	}
}
