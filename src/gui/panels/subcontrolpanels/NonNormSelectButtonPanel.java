package gui.panels.subcontrolpanels;

import gui.panels.ControlPanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class NonNormSelectButtonPanel extends JPanel {
	
	private ControlPanel controlPanel;
	private int maxNum;
	private String name;
	
	private JButton breakButton, unbreakButton;
	private JSpinner spinner;
		
	public NonNormSelectButtonPanel (String name, int n, ControlPanel cp) {
		super(new GridBagLayout());
		this.name = name;
		this.maxNum = n;
		controlPanel = cp;
		
		this.setBackground(Color.white);
		this.setForeground(Color.black);
		this.setFont(new Font("SansSerif", Font.BOLD, 14));
		this.setOpaque(true);
		this.setSize(15, 30);
		this.setMinimumSize(new Dimension(15, 40));
		this.setMaximumSize(new Dimension(15, 40));
		this.setPreferredSize(new Dimension(15, 40));
		
		breakButton = new JButton("Break " + name);
		unbreakButton = new JButton("Unbreak " + name);
		spinner = new JSpinner(new SpinnerNumberModel(1, 1, maxNum , 1));
		
		breakButton.setSize(new Dimension(7, 20));
		unbreakButton.setSize(new Dimension(7, 20));
		
		breakButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
		unbreakButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
		
		breakButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		unbreakButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		breakButton.addActionListener((new BreakListener()));
		unbreakButton.addActionListener((new UnbreakListener()));
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.LINE_START;
		this.add(breakButton, c);
		
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.LINE_START;
		this.add(unbreakButton, c);
		
		c = new GridBagConstraints();
		c.gridx = 3;
		c.gridy = 0;
		c.gridheight = 2;
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.LINE_END;
		this.add(spinner, c);
		
		this.validate();		
	}

	public class BreakListener implements ActionListener {
		/**
		 * Invoked whenever the button is pressed
		 */
		public void actionPerformed(ActionEvent ae) {

		}
	}
	
	public class UnbreakListener implements ActionListener {
		/**
		 * Invoked whenever the button is pressed
		 */
		public void actionPerformed(ActionEvent ae) {

		}
	}

	
	
}
