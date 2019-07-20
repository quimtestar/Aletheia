package aletheia.gui.app;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;

import aletheia.persistence.PersistenceManager.StartupProgressListener;
import aletheia.version.VersionManager;

public class SplashStartupProgressListener implements StartupProgressListener
{
	private final JWindow window;
	private final JProgressBar progressBar;

	public SplashStartupProgressListener()
	{
		JWindow window = null;
		JProgressBar progressBar = null;
		try (InputStream is = ClassLoader.getSystemResourceAsStream("aletheia/gui/app/splash_image.png"))
		{
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
			JLabel label = new JLabel("Version " + VersionManager.getVersion());
			label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
			label.setOpaque(true);
			label.setBackground(Color.WHITE);
			progressBar = new JProgressBar();
			window = new JWindow();
			window.toFront();
			window.setFocusable(false);
			window.setBackground(new Color(0, 0, 0, 0));
			JPanel panel = new JPanel();
			panel.setOpaque(false);
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.add(imagePanel);
			panel.add(label);
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

	public void close()
	{
		if (window != null)
			window.dispose();
	}

}
