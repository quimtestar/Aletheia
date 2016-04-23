package aletheia.gui.app;

import javax.swing.JFrame;

public abstract class AletheiaJFrame extends JFrame
{
	private static final long serialVersionUID = -5082905593301164532L;

	public abstract void setExtraTitle(String extraTitle);

	public abstract void exit();

}
