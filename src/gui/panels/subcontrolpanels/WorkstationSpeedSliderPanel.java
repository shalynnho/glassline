package gui.panels.subcontrolpanels;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class WorkstationSpeedSliderPanel extends JPanel {
	
	private String[] workstations = {"DRILL", "CROSS SEAMER", "GRINDER"};
	private JComboBox workstationComboBox;
	private JSlider speedSlider;
	
	
	public WorkstationSpeedSliderPanel() {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		
		workstationComboBox = new JComboBox(workstations);
		speedSlider = new JSlider(0, 10, 5);
		
		this.add(workstationComboBox);
		this.add(speedSlider);
		this.validate();
	}

}
