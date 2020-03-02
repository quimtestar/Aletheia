/*******************************************************************************
 * Copyright (c) 2019, 2020 Quim Testar.
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
 *******************************************************************************/
package aletheia.test.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import aletheia.gui.app.splash.SplashStartupProgressListener;
import aletheia.test.Test;

public class GuiTest0004 extends Test
{

	@Override
	public void run() throws Exception
	{
		JFrame jFrame = new JFrame("POCTLLA");
		jFrame.setContentPane(new JLabel("Poctlla!"));
		jFrame.setPreferredSize(new Dimension(800, 600));
		jFrame.pack();
		jFrame.setVisible(true);

		JDialog jDialog = new JDialog(jFrame, true);
		JButton button = new JButton();
		button.setAction(new AbstractAction("POCTLLA!")
		{

			private static final long serialVersionUID = 5322598456346991575L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				Thread t = new Thread(() -> {
					try (SplashStartupProgressListener s = new SplashStartupProgressListener())
					{
						for (int i = 0; i <= 5; i++)
						{
							s.updateProgress(i / 5f);
							try
							{
								Thread.sleep(250);
							}
							catch (InterruptedException e1)
							{
								e1.printStackTrace();
							}
						}
					}
				});
				t.start();
			}
		});
		jDialog.add(button);
		jDialog.setPreferredSize(new Dimension(200, 75));
		jDialog.pack();
		jDialog.setVisible(true);

	}

}
