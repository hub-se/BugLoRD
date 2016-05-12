/*
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Abstract base panel class taken from:
 * 
 * <br>GRAL: GRAphing Library for Java(R)
 *
 * <br>(C) Copyright 2009-2016 Erich Seifert 'dev[at]erichseifert.de',
 * <br>Michael Seifert 'michael[at]erichseifert.de'
 */
public abstract class Panel extends JPanel {
	/** Version id for serialization. */
	private static final long serialVersionUID = 8221256658243821951L;

	/**
	 * Performs basic initialization with a default size of 800 x 600 pixels.
	 */
	public Panel() {
		this(800, 600);
	}
	
	/**
	 * Performs basic initialization with the given width and height.
	 * @param width
	 * is the width of the panel
	 * @param height
	 * is the height of the panel
	 */
	public Panel(final int width, final int height) {
		super(new BorderLayout());
		setPreferredSize(new Dimension(width, height));
		setBackground(Color.WHITE);
	}

	/**
	 * Returns a short title for the example.
	 * @return A title text.
	 */
	public abstract String getTitle();

	/**
	 * Returns a more detailed description of the example contents.
	 * @return A description of the example.
	 */
	public abstract String getDescription();

	/**
	 * Opens a frame and shows the example in it.
	 * @return the frame instance used for displaying the example.
	 */
	public JFrame showInFrame() {
		final JFrame frame = new JFrame(getTitle());
		frame.getContentPane().add(this, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(getPreferredSize());
		frame.setVisible(true);
		return frame;
	}

	/* (non-Javadoc)
	 * @see java.awt.Component#toString()
	 */
	@Override
	public String toString() {
		return getTitle();
	}
}
