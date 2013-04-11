package gui.panels.subcontrolpanels;

import gui.panels.ControlPanel;

import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import shared.enums.MachineType;

/**
 * The GlassSelectPanel class contains buttons allowing the user to select what type of glass to produce.
 */
@SuppressWarnings("serial")
public class GlassSelectPanel extends JPanel {
	/** The ControlPanel this is linked to */
	private ControlPanel parent;

	private JScrollPane scrollPane;
	private JPanel mainPanel;
	private ArrayList<JCheckBox> checkBoxes;
	
	/**
	 * Creates a new GlassSelect and links it to the control panel
	 * 
	 * @param cp the ControlPanel linked to it
	 */
	public GlassSelectPanel(ControlPanel cp) {
		parent = cp;

		checkBoxes = new ArrayList<JCheckBox>();
		checkBoxes.add(new JCheckBox(MachineType.CROSS_SEAMER.toString()));
		checkBoxes.add(new JCheckBox(MachineType.DRILL.toString()));
		checkBoxes.add(new JCheckBox(MachineType.GRINDER.toString()));
		checkBoxes.add(new JCheckBox(MachineType.MANUAL_BREAKOUT.toString()));
		checkBoxes.add(new JCheckBox(MachineType.CUTTER.toString()));
		checkBoxes.add(new JCheckBox(MachineType.WASHER.toString()));
		checkBoxes.add(new JCheckBox(MachineType.UV_LAMP.toString()));
		checkBoxes.add(new JCheckBox(MachineType.OVEN.toString()));
		checkBoxes.add(new JCheckBox(MachineType.PAINT.toString()));
		checkBoxes.add(new JCheckBox(MachineType.BREAKOUT.toString()));

		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		for (int i=0; i<checkBoxes.size(); i++) {
			mainPanel.add(checkBoxes.get(i));
		}
		
		scrollPane = new JScrollPane(this);
		scrollPane.add(mainPanel);
		
		mainPanel.validate();
		scrollPane.validate();
		
	}

	/**
	 * Returns the parent panel
	 * 
	 * @return the parent panel
	 */
	public ControlPanel getGuiParent() {
		return parent;
	}
}
