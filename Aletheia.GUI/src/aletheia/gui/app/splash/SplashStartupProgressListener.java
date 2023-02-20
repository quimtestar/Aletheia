/*******************************************************************************
 * Copyright (c) 2019, 2023 Quim Testar.
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
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
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

import aletheia.gui.fonts.FontManager;
import aletheia.gui.preferences.GUIAletheiaPreferences;
import aletheia.version.VersionManager;

public class SplashStartupProgressListener extends AbstractSplashStartupProgressListener
{
	private final JWindow window;
	private final JProgressBar progressBar;

	private static Rectangle screenBounds()
	{
		Rectangle appBounds = GUIAletheiaPreferences.instance.appearance().aletheiaJFrameBounds().getBounds();
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		for (GraphicsDevice graphicsDevice : graphicsEnvironment.getScreenDevices())
		{
			Rectangle screenBounds = graphicsDevice.getDefaultConfiguration().getBounds();
			if (screenBounds.contains(appBounds.getCenterX(), appBounds.getCenterY()))
				return screenBounds;
		}
		return graphicsEnvironment.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
	}

	private static Image image(Rectangle screenBounds)
	{
		try (InputStream is = ClassLoader.getSystemResourceAsStream("aletheia/gui/app/splash/splash_image.png"))
		{
			BufferedImage original = ImageIO.read(is);
			if (screenBounds.getWidth() >= original.getWidth() && screenBounds.getHeight() >= original.getHeight())
				return original;
			else
			{
				int width = Integer.min(screenBounds.width, original.getWidth() * screenBounds.height / original.getHeight());
				int height = Integer.min(screenBounds.height, original.getHeight() * screenBounds.width / original.getWidth());
				return original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			}
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public SplashStartupProgressListener()
	{
		Rectangle screenBounds = screenBounds();
		Image image = image(screenBounds);
		if (image == null)
		{
			window = null;
			progressBar = null;
		}
		else
		{
			window = new JWindow();
			window.setBackground(new Color(0, 0, 0, 0));
			JPanel panel = new JPanel();
			panel.setOpaque(false);
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

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
			imagePanel.setPreferredSize(new Dimension(image.getWidth(null), image.getHeight(null)));
			panel.add(imagePanel);
			panel.add(new Box.Filler(new Dimension(0, 3), new Dimension(0, 3), new Dimension(0, 3)));

			JLabel label = new JLabel("Version " + VersionManager.getVersion());
			label.setBorder(
					BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 1, false), BorderFactory.createEmptyBorder(2, 5, 2, 5)));
			label.setOpaque(true);
			label.setForeground(Color.DARK_GRAY);
			label.setBackground(Color.WHITE);
			panel.add(label);
			panel.add(new Box.Filler(new Dimension(0, 3), new Dimension(0, 3), new Dimension(0, 3)));

			progressBar = new JProgressBar();
			progressBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.WHITE, 1, false),
					BorderFactory.createLineBorder(new Color(0x000054), 1, false)));
			progressBar.setBackground(Color.WHITE);
			progressBar.setForeground(new Color(0x000054));
			progressBar.setFont(new FontManager(8).defaultFont());
			progressBar.setStringPainted(true);
			panel.add(progressBar);

			window.setContentPane(panel);
			window.pack();
			Dimension size = window.getSize();

			window.setLocation((int) (screenBounds.getCenterX() - size.width / 2d), (int) (screenBounds.getCenterY() - size.height / 2d));
			window.setFocusable(false);
			window.setVisible(true);
			window.toFront();
		}
	}

	@Override
	public void updateProgress(float progress, String text)
	{
		if (progressBar != null)
		{
			progressBar.setValue((int) (progress * 100));
			progressBar.setString(text);
		}
	}

	@Override
	public void close()
	{
		if (window != null)
			window.dispose();
	}

}
