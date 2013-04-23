package gui.panels.subcontrolpanels;

import gui.panels.ControlPanel;

import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class WorkstationSpeedSliderPanel extends JPanel {
	
	private static final long serialVersionUID = 7286657091005148296L;
	private ControlPanel parent;
	private String[] workstations = {"Drill0", "Drill1", "CrossSeamer0", "CrossSeamer1", "Grinder0", "Grinder1"};
	private JComboBox<String> workstationComboBox;
	private JSlider speedSlider;
	
	
	public WorkstationSpeedSliderPanel(ControlPanel cp) {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		
		parent = cp;
		workstationComboBox = new JComboBox<String>(workstations);
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
	 
	protected class SliderListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent evt) {
			
		}
	}

}
