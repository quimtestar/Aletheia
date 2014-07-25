/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.gui.app;

import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import aletheia.gui.icons.IconManager;
import aletheia.gui.lookandfeel.MyLookAndFeel;
import aletheia.log4j.LoggerManager;
import aletheia.utilities.MiscUtilities;

public class AletheiaGUI
{
	private final static Logger logger = LoggerManager.logger();

	private final AletheiaEventQueue aletheiaEventQueue;

	public AletheiaGUI()
	{
		this.aletheiaEventQueue = new AletheiaEventQueue();
	}

	public AletheiaEventQueue getAletheiaEventQueue()
	{
		return aletheiaEventQueue;
	}

	public void run()
	{
		Toolkit.getDefaultToolkit().getSystemEventQueue().push(aletheiaEventQueue);
		JFrame virtualFrame = new JFrame();
		virtualFrame.setIconImages(IconManager.instance.aletheiaIconList);
		run: while (true)
		{
			try
			{
				AletheiaJFrame aletheiaJFrame = new AletheiaJFrame(this);
				aletheiaJFrame.pack();
				aletheiaJFrame.setVisible(true);
				AletheiaJFrame.ExitState state = aletheiaJFrame.waitForClose();
				switch (state)
				{
				case EXIT:
					break run;
				case RESTART:
					break;
				default:
					break;
				}
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
				JOptionPane.showMessageDialog(virtualFrame, MiscUtilities.wrapText(e.getMessage(), 80), "Error", JOptionPane.ERROR_MESSAGE);
				break;
			}
		}
		virtualFrame.dispose();
	}

	public static void main(String[] args)
	{
		try
		{
			LoggerManager.setUncaughtExceptionHandler();
			LookAndFeel laf = new MyLookAndFeel();
			UIManager.setLookAndFeel(laf);
			AletheiaGUI aletheiaGUI = new AletheiaGUI();
			aletheiaGUI.run();
			System.exit(0);
		}
		catch (Exception e)
		{
			logger.fatal("Startup error", e);
			System.exit(1);
		}
	}

}
