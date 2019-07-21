/*******************************************************************************
 * Copyright (c) 2019 Quim Testar.
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
package aletheia.gui.app.splash;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;

import aletheia.version.VersionManager;

public class SimpleSplashStartupProgressListener extends SplashStartupProgressListener
{
	private final JWindow window;
	private final JProgressBar progressBar;

	public SimpleSplashStartupProgressListener()
	{
		JWindow window = null;
		JProgressBar progressBar = null;
		try (InputStream is = ClassLoader.getSystemResourceAsStream("aletheia/gui/app/splash/splash_image.png"))
		{
			window = new JWindow();
			window.toFront();
			window.setFocusable(false);
			window.setBackground(new Color(0, 0, 0, 0));
			JPanel panel = new JPanel();
			panel.setOpaque(false);
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

			BufferedImage image = ImageIO.read(is);
			JPanel imagePanel = new JPanel()
			{
				private static final long serialVersionUID = 7793703420918559601L;

				@Override
				protected void paintComponent(Graphics graphics)
				{
					super.paintComponent(graphics);
					graphics.drawImage(image, 0, 0, this);
				}

			};
			imagePanel.setOpaque(false);
			imagePanel.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
			panel.add(imagePanel);
			panel.add(new Box.Filler(new Dimension(0, 3), new Dimension(0, 3), new Dimension(0, 3)));

			JLabel label = new JLabel("Version " + VersionManager.getVersion());
			label.setBorder(
					BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 1, false), BorderFactory.createEmptyBorder(2, 5, 2, 5)));
			label.setOpaque(true);
			label.setBackground(Color.WHITE);
			panel.add(label);
			panel.add(new Box.Filler(new Dimension(0, 3), new Dimension(0, 3), new Dimension(0, 3)));

			progressBar = new JProgressBar();
			progressBar.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1, false));
			progressBar.setBackground(Color.WHITE);
			progressBar.setForeground(new Color(0x000054));
			panel.add(progressBar);

			window.setContentPane(panel);
			window.pack();
			Dimension size = window.getSize();
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			window.setLocation((screenSize.width - size.width) / 2, (screenSize.height - size.height) / 2);
			window.setVisible(true);
		}
		catch (Exception e)
		{
		}
		this.window = window;
		this.progressBar = progressBar;
	}

	@Override
	public void updateProgress(float progress)
	{
		if (progressBar != null)
			progressBar.setValue((int) (progress * 100));
	}

	@Override
	public void close()
	{
		if (window != null)
			window.dispose();
	}

}
