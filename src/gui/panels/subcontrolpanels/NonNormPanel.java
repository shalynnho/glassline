package gui.panels.subcontrolpanels;

import gui.panels.ControlPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The NonNormPanel is responsible for initiating and managing non-normative situations. It contains buttons for each possible non-norm.
 * 
 * The non-normative situations are: 1. 2. 3. 4. 5. 6. 7. 8.
 */
@SuppressWarnings("serial")
public class NonNormPanel extends JPanel {
	/** The number of different havoc actions that exist */
	public static final int NUM_NON_NORMS = 8;

	/** The control panel this is linked to */
	ControlPanel parent;

	/** List of buttons for each non-norm */
	List<JButton> nonNormButtons;

	/** Title label **/
	JLabel titleLabel;
	
	/** NonNormSelect Panels */
	List<NonNormSelectPanel> nonNormSelectPanels;

	/**
	 * Creates a new HavocPanel and links the control panel to it
	 * 
	 * @param cp the ControlPanel linked to it
	 */
	public NonNormPanel(ControlPanel cp) {
		parent = cp;

		this.setBackground(Color.black);
		this.setForeground(Color.black);

		// set up layout
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// set up button panel
		JPanel buttonPanel = new JPanel();
		GridLayout grid = new GridLayout(NUM_NON_NORMS / 2, 2);
		grid.setVgap(2);
		grid.setHgap(2);
		buttonPanel.setBackground(Color.black);
		buttonPanel.setLayout(grid);

		// make title
		titleLabel = new JLabel("NON NORMATIVES");
		titleLabel.setForeground(Color.white);
		titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 22));
		JPanel titleLabelPanel = new JPanel();
		titleLabelPanel.add(titleLabel);
		// titleLabelPanel.setAlignmentY(JLabel.CENTER_ALIGNMENT);
		titleLabelPanel.setBackground(Color.black);

		// Set up the nonNormSelectionPanel list
		nonNormSelectPanels = new ArrayList<NonNormSelectPanel>(NUM_NON_NORMS);
		nonNormSelectPanels.add(new NonNormSelectPanel("Conveyor", 15, parent));
		nonNormSelectPanels.add(new NonNormSelectPanel("Popup", 16, null));
		nonNormSelectPanels.add(new NonNormSelectPanel("Offline", 16, null));
		nonNormSelectPanels.add(new NonNormSelectPanel("Online", 16, null));
		nonNormSelectPanels.add(new NonNormSelectPanel("Truck", 16, null));
		nonNormSelectPanels.add(new NonNormSelectPanel("Bin", 16, null));
		nonNormSelectPanels.add(new NonNormSelectPanel("Sensor", 16, null));
		nonNormSelectPanels.add(new NonNormSelectPanel("TEST", 16, null));
		
		// add to panel
		this.add(titleLabelPanel);

		JPanel colorLinesPanel1 = new JPanel();
		colorLinesPanel1.setPreferredSize(new Dimension(350, 2));
		colorLinesPanel1.setBackground(Color.black);
		ImageIcon cl = new ImageIcon("imageicons/singleColoredLine.png");
		JLabel clLabel1 = new JLabel(cl);
		colorLinesPanel1.add(clLabel1);
		this.add(colorLinesPanel1);

		for (NonNormSelectPanel p  : nonNormSelectPanels) {
			buttonPanel.add(p);
		}
		
		buttonPanel.setAlignmentY(JPanel.CENTER_ALIGNMENT);
		this.add(buttonPanel);

		JPanel colorLinesPanel2 = new JPanel();
		colorLinesPanel2.setPreferredSize(new Dimension(350, 40));
		colorLinesPanel2.setBackground(Color.black);
		JLabel clLabel2 = new JLabel();
		colorLinesPanel2.add(clLabel2);
		this.add(colorLinesPanel2);
		this.validate();
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
