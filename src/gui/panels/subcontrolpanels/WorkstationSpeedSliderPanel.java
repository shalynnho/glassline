package gui.panels.subcontrolpanels;

import gui.panels.ControlPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class WorkstationSpeedSliderPanel extends JPanel {
	
	private static final long serialVersionUID = 7286657091005148296L;
	
	private ControlPanel controlPanel;
	
	private String[] workstations = {"Drill0", "Drill1", "Cross Seamer0", "Cross Seamer1", "Grinder0", "Grinder1"};
	private JComboBox<String> workstationComboBox;
	
	private JSlider speedSlider;
	private String selected;
	private int selectedSpeed;
	
	private Map<String,Integer> speeds;	
	
	public WorkstationSpeedSliderPanel(ControlPanel cp) {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		
		controlPanel = cp;
				
		selected = "Drill0";
		workstationComboBox = new JComboBox<String>(workstations);
		workstationComboBox.addActionListener(new ComboBoxListener());
		
		speeds = new HashMap<String,Integer>();
		for (int i = 0; i < workstations.length; i++) {
			speeds.put(workstations[i], 10);
		}
		
		speedSlider = new JSlider(0, 10, 10);
		speedSlider.setMajorTickSpacing(5);
		speedSlider.setSnapToTicks(true);
		speedSlider.setPaintLabels(true);
		speedSlider.addChangeListener(new SliderListener());
		
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
			JSlider slider = (JSlider) evt.getSource();
			int newSpeed = slider.getValue();
			speeds.put(selected,newSpeed);
			controlPanel.getGuiParent().getGUIOfflineWorkstations().get(selected).setSpeed(newSpeed);
			System.out.println("Speed set to " + newSpeed + "for " + selected);
		}
	}
	
	protected class ComboBoxListener implements ActionListener {

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent evt) {
			JComboBox<String> cb = (JComboBox<String>) evt.getSource();
			selected = (String) cb.getSelectedItem();
			speedSlider.setValue(speeds.get(selected));
		}
		
	}

}
