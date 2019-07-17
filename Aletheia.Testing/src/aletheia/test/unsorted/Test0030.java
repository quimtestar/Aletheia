package aletheia.test.unsorted;

import java.lang.reflect.Field;
import aletheia.gui.app.AletheiaJPanel;
import aletheia.gui.app.DesktopAletheiaGUI;
import aletheia.gui.app.DesktopAletheiaJFrame;
import aletheia.test.Test;

public class Test0030 extends Test
{

	@Override
	public void run() throws Exception
	{
		DesktopAletheiaGUI aletheiaGUI = new DesktopAletheiaGUI();
		DesktopAletheiaJFrame aletheiaJFrame = new DesktopAletheiaJFrame(aletheiaGUI);
		aletheiaJFrame.pack();
		aletheiaJFrame.setVisible(true);

		Field aletheiaContentPaneField = aletheiaJFrame.getClass().getDeclaredField("aletheiaContentPane");
		aletheiaContentPaneField.setAccessible(true);
		AletheiaJPanel aletheiaJPanel = (AletheiaJPanel) aletheiaContentPaneField.get(aletheiaJFrame);

		String s = "\u2190 \u00bbpoctlla\u00ab \u2192";

		aletheiaJPanel.getCliJPanel().getErrB().println(s);

	}

}
