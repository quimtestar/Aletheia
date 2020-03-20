/*******************************************************************************
 * Copyright (c) 2014, 2019 Quim Testar.
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

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Collection;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.logging.log4j.Logger;

import aletheia.common.AletheiaConstants;
import aletheia.gui.app.splash.AbstractSplashStartupProgressListener;
import aletheia.gui.icons.IconManager;
import aletheia.log4j.LoggerManager;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.peertopeer.PeerToPeerNode;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;

public class SimpleAletheiaJFrame extends MainAletheiaJFrame
{
	private static final long serialVersionUID = 3991904540466721887L;
	private static final Logger logger = LoggerManager.instance.logger();

	public enum Panel
	{
		TABBED, CONTEXT, CATALOG, ALL;
	}

	static class MyProperties extends Properties
	{
		private static final long serialVersionUID = -588777607193025899L;
		private static final String propertiesFileName_Default = "aletheia.simple.properties";
		private static final String propertiesFileName = System.getProperty("aletheia.simple.properties.file", propertiesFileName_Default);

		private static final String dbfile_name = "aletheia.dbfile_name";
		private static final String read_only = "aletheia.read_only";
		private static final String cache_percent = "aletheia.cache_percent";
		private static final String font_size = "aletheia.font_size";
		private static final String active_context_uuid = "aletheia.active_context_uuid";

		private static final boolean default_read_only = true;
		private static final int default_cache_percent = 0;
		private static final int default_font_size = 16;

		private static final Properties defaults;

		static
		{
			defaults = new Properties();
			defaults.setProperty(read_only, Boolean.toString(default_read_only));
			defaults.setProperty(cache_percent, Integer.toString(default_cache_percent));
			defaults.setProperty(font_size, Integer.toString(default_font_size));
		}

		private static String resolveTemplate(String template)
		{
			if (template == null)
				return null;
			Pattern pattern = Pattern.compile("(%randname%)");
			Matcher matcher = pattern.matcher(template);
			StringBuffer buffer = new StringBuffer();
			while (matcher.find())
			{
				String tag = matcher.group();
				switch (tag)
				{
				case "%randname%":
				{
					byte[] bytes = new byte[8];
					new Random().nextBytes(bytes);
					String randomName = Base64.getEncoder().encodeToString(bytes).substring(0, 10);
					matcher.appendReplacement(buffer, randomName);
					break;
				}
				default:
				{
					matcher.appendReplacement(buffer, tag);
					break;
				}
				}
			}
			matcher.appendTail(buffer);
			return buffer.toString();
		}

		private MyProperties()
		{
			super(defaults);
			try
			{
				InputStream is = new FileInputStream(propertiesFileName);
				try
				{
					load(is);
				}
				finally
				{
					is.close();
				}
			}
			catch (IOException e)
			{
			}
		}

		public File getDbFile()
		{
			try
			{
				return new File(resolveTemplate(getProperty(dbfile_name)));
			}
			catch (NullPointerException e)
			{
				return null;
			}
		}

		public boolean isReadOnly()
		{
			return Boolean.parseBoolean(getProperty(read_only));
		}

		public int getCachePercent()
		{
			try
			{
				return Integer.parseInt(getProperty(cache_percent));
			}
			catch (NumberFormatException e)
			{
				return default_cache_percent;
			}
		}

		public int getFontSize()
		{
			try
			{
				return Integer.parseInt(getProperty(font_size));
			}
			catch (NumberFormatException e)
			{
				return default_font_size;
			}
		}

		public UUID getActiveContextUuid()
		{
			try
			{
				return UUID.fromString(getProperty(active_context_uuid));
			}
			catch (NullPointerException e)
			{
				return null;
			}
			catch (IllegalArgumentException e)
			{
				logger.warn("Bad uuid format in properties file");
				return null;
			}
		}

	}

	private static final MyProperties properties = new MyProperties();

	private final PersistenceManager persistenceManager;
	private final AletheiaJPanel aletheiaJPanel;
	private final JTabbedPane aletheiaJTabbedPane;

	private boolean active;

	public SimpleAletheiaJFrame(SimpleAletheiaGUI aletheiaGUI) throws InterruptedException
	{
		super(new FontManager(properties.getFontSize()), aletheiaGUI);
		try (AbstractSplashStartupProgressListener splashStartupProgressListener = AbstractSplashStartupProgressListener
				.makeFromGlobalSwitches(getGlobalSwitches()))
		{
			BerkeleyDBPersistenceManager.Configuration configuration = new BerkeleyDBPersistenceManager.Configuration();
			configuration.setStartupProgressListener(splashStartupProgressListener);
			File dbFile = properties.getDbFile();
			if (dbFile == null)
				throw new RuntimeException("No DB file location configured");
			configuration.setDbFile(dbFile);
			configuration.setReadOnly(properties.isReadOnly());
			configuration.setCachePercent(properties.getCachePercent());
			this.persistenceManager = new BerkeleyDBPersistenceManager(configuration);
			this.aletheiaJPanel = new AletheiaJPanel(this, this, persistenceManager);
			this.aletheiaJPanel.setDragging(true);
			this.aletheiaJTabbedPane = new JTabbedPane();
			this.aletheiaJTabbedPane.addTab("Context", aletheiaJPanel.getContextJTreeJPanel().getContextJTreeDraggableJScrollPane());
			this.aletheiaJTabbedPane.addTab("Catalog", aletheiaJPanel.getCliJPanel().getCatalogJTreeDraggableJScrollPane());

			UUID activeContextUuid = properties.getActiveContextUuid();
			if (activeContextUuid != null)
				try (Transaction transaction = persistenceManager.beginTransaction())
				{
					Context activeContext = persistenceManager.getContext(transaction, activeContextUuid);
					if (activeContext != null)
					{
						aletheiaJPanel.setActiveContext(activeContext);
						if (!activeContext.isProved())
							aletheiaJPanel.getContextJTree().expandStatement(activeContext);
						aletheiaJPanel.getContextJTree().scrollStatementToVisible(activeContext);
					}
					else
						logger.warn("Couldn't set active context because no context with uuid '" + activeContextUuid + "' found");
				}

			switch (aletheiaGUI.getPanel())
			{
			case ALL:
				this.setContentPane(aletheiaJPanel);
				break;
			case CONTEXT:
				this.setContentPane(aletheiaJPanel.getContextJTreeJPanel().getContextJTreeDraggableJScrollPane());
				break;
			case CATALOG:
				this.setContentPane(aletheiaJPanel.getCliJPanel().getCatalogJTreeDraggableJScrollPane());
				break;
			case TABBED:
				this.setContentPane(aletheiaJTabbedPane);
				break;
			}

			Rectangle maxRectangle = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
			if (maxRectangle != null)
			{
				this.setPreferredSize(new Dimension((int) maxRectangle.getWidth(), (int) maxRectangle.getHeight()));
				this.setLocation(maxRectangle.getLocation());
			}
			this.setExtendedState(MAXIMIZED_BOTH);
			this.setResizable(true);
			this.setUndecorated(true);
			this.setTitle(AletheiaConstants.TITLE);
			this.setIconImages(IconManager.instance.aletheiaIconList);
			this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			this.active = true;
			this.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosing(WindowEvent e)
				{
					exit();
				}
			});
		}

	}

	@Override
	public SimpleAletheiaGUI getAletheiaGUI()
	{
		return (SimpleAletheiaGUI) super.getAletheiaGUI();
	}

	public AletheiaJPanel getAletheiaJPanel()
	{
		return aletheiaJPanel;
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public void lock(Collection<Transaction> owners)
	{
		aletheiaJPanel.lock(owners);
	}

	@Override
	public void selectStatement(Statement statement)
	{
		aletheiaJPanel.selectStatement(statement);
	}

	@Override
	public void setActiveContext(Context context)
	{
		aletheiaJPanel.setActiveContext(context);
	}

	@Override
	public void restart()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public PeerToPeerNode getPeerToPeerNode()
	{
		return null;
	}

	@Override
	public AletheiaJFrame openExtraFrame(String extraTitle)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setExtraTitle(String extraTitle)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized void exit()
	{
		if (active)
		{
			try
			{
				aletheiaJPanel.close();
				persistenceManager.close();
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						dispose();
					}
				});
				active = false;
			}
			catch (InterruptedException | IOException e)
			{
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void resetedGui()
	{
		super.resetedGui();
		switch (getAletheiaGUI().getPanel())
		{
		case CONTEXT:
			this.setContentPane(aletheiaJPanel.getContextJTreeJPanel().getContextJTreeDraggableJScrollPane());
			break;
		case CATALOG:
			this.setContentPane(aletheiaJPanel.getCliJPanel().getCatalogJTreeDraggableJScrollPane());
			break;
		case TABBED:
			this.setContentPane(aletheiaJTabbedPane);
		default:
			break;
		}
	}

}
