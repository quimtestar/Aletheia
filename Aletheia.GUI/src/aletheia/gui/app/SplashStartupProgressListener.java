package aletheia.gui.app;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JWindow;

import aletheia.persistence.PersistenceManager.StartupProgressListener;
import aletheia.version.VersionManager;

public class SplashStartupProgressListener implements StartupProgressListener
{
	private final JWindow window;

	private float progress = 0;

	public SplashStartupProgressListener()
	{
		JWindow window = null;
		try (InputStream is = ClassLoader.getSystemResourceAsStream("aletheia/gui/app/splash.png"))
		{
			BufferedImage image = ImageIO.read(is);
			JPanel panel = new JPanel()
			{
				private static final long serialVersionUID = 7793703420918559601L;

				@Override
				protected void paintComponent(Graphics graphics)
				{
					super.paintComponent(graphics);
					graphics.drawImage(image, 0, 0, this);
					graphics.setColor(new Color(0x000054));
					graphics.drawString("Version " + VersionManager.getVersion(), 12, 492);
					int position = (int) (progress * (SplashStartupProgressListener.this.window.getSize().width - 4));
					graphics.fillRect(2, 466, position, 6);
					graphics.setColor(Color.WHITE);
					graphics.fillRect(position, 466, SplashStartupProgressListener.this.window.getSize().width - 4, 6);
				}

			};
			panel.setBackground(Color.WHITE);
			panel.setOpaque(false);
			panel.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
			window = new JWindow();
			window.toFront();
			window.setFocusable(false);
			window.setBackground(new Color(0, 0, 0, 0));
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
	}

	@Override
	public void updateProgress(float progress)
	{
		this.progress = progress;
		if (window != null)
			window.repaint();
	}

	public void close()
	{
		if (window != null)
			window.dispose();
	}

}
