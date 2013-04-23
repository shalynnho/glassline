package gui.panels.subcontrolpanels;

import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
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
		speedSlider = new JSlider(0, 10, 10);
		speedSlider.setMajorTickSpacing(5);
		speedSlider.setSnapToTicks(true);
		speedSlider.setPaintLabels(true);
		
		Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
		table.put(0, new JLabel("Low"));
		table.put(5, new JLabel("Medium"));
		table.put(10, new JLabel("High"));
		speedSlider.setLabelTable(table);
		
		this.add(workstationComboBox);
		this.add(speedSlider);
		this.validate();
	}

}
