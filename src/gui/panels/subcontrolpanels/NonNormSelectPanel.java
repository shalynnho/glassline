package gui.panels.subcontrolpanels;

import gui.panels.FactoryPanel;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class NonNormSelectPanel extends JPanel {
	
	private FactoryPanel factoryPanel;
	private int maxNum;
	private String name;
	
	private JButton breakButton, unbreakButton;
	private JSpinner spinner;
	
	private JPanel buttonPanel;
	
	public NonNormSelectPanel (String name, int n, FactoryPanel fp) {
		this.name = name;
		this.maxNum = n;
		factoryPanel = fp;
		
		breakButton = new JButton("Break");
		unbreakButton = new JButton("Unbreak");
		spinner = new JSpinner(new SpinnerNumberModel(0, 0, maxNum - 1 , 1));
		
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		buttonPanel = new JPanel();
		
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		
		buttonPanel.add(breakButton);
		buttonPanel.add(unbreakButton);
		
		this.add(buttonPanel);
		this.add(spinner);
		
		this.validate();		
	}
	
	
}
