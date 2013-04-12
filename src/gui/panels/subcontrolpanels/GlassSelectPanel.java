package gui.panels.subcontrolpanels;

import gui.panels.ControlPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;

import shared.enums.MachineType;
import transducer.TChannel;
import transducer.TEvent;

/**
 * The GlassSelectPanel class contains buttons allowing the user to select what type of glass to produce.
 */
@SuppressWarnings("serial")
public class GlassSelectPanel extends JPanel implements ActionListener {
	/** The ControlPanel this is linked to */
	private ControlPanel parent;

	private JScrollPane scrollPane;
	private JPanel mainPanel;
	private ArrayList<JCheckBox> checkBoxes;
	private JButton goButton = new JButton("GO!");
	
	private ArrayList<MachineType> machineTypes = new ArrayList<MachineType>();
	
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
		
		machineTypes.add(MachineType.CROSS_SEAMER);
		machineTypes.add(MachineType.DRILL);
		machineTypes.add(MachineType.GRINDER);
		machineTypes.add(MachineType.MANUAL_BREAKOUT);
		machineTypes.add(MachineType.CUTTER);
		machineTypes.add(MachineType.WASHER);
		machineTypes.add(MachineType.UV_LAMP);
		machineTypes.add(MachineType.OVEN);
		machineTypes.add(MachineType.PAINT);
		machineTypes.add(MachineType.BREAKOUT);

		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		for (int i=0; i<checkBoxes.size(); i++) {
			//checkBoxes.get(i).setAlignmentX(CENTER_ALIGNMENT);
			mainPanel.add(checkBoxes.get(i));
		}
		//goButton.setAlignmentX(CENTER_ALIGNMENT);
		mainPanel.add(goButton);
		
		mainPanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);		
		scrollPane = new JScrollPane(mainPanel);
		this.add(scrollPane);
		
		mainPanel.validate();
		scrollPane.validate();
		
		// Add action listeners
		goButton.addActionListener(this);		
	}

	/**
	 * Returns the parent panel
	 * 
	 * @return the parent panel
	 */
	public ControlPanel getGuiParent() {
		return parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == goButton) {
			ArrayList<MachineType> recipeParts = new ArrayList<MachineType>();
			for (JCheckBox c: checkBoxes) {
				if (c.isSelected()) {
					for (MachineType m: machineTypes) {
						if (c.getText().equals(m.toString())) {
							recipeParts.add(m);
							c.setSelected(false);
							break;
						}
					}
				}
			}
			//parent.transducer.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
			// How do I add recipe parts to a piee of glass?
		}		
	}
}
