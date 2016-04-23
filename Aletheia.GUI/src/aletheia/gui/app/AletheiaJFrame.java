package aletheia.gui.app;

import javax.swing.JFrame;

import aletheia.common.AletheiaConstants;

public abstract class AletheiaJFrame extends JFrame
{
	private static final long serialVersionUID = -5082905593301164532L;

	public void setExtraTitle(String extraTitle)
	{
		setTitle(AletheiaConstants.TITLE + (extraTitle != null ? " " + extraTitle : ""));
	}

}
