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

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.logging.log4j.Logger;

import aletheia.common.AletheiaConstants;
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

	static class MyProperties extends Properties
	{
		private static final long serialVersionUID = -588777607193025899L;
		private static final String propertiesFileName_Default = "aletheia.properties";
		private static final String propertiesFileName = System.getProperty("aletheia.properties.file", propertiesFileName_Default);

		private static final String dbfile_name = "aletheia.dbfile_name";
		private static final String read_only = "aletheia.read_only";
		private static final String cache_percent = "aletheia.cache_percent";

		private static final Properties defaults;

		static
		{
			defaults = new Properties();
			defaults.setProperty(read_only, Boolean.toString(true));
			defaults.setProperty(cache_percent, Integer.toString(0));
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
			String dbFileName = getProperty(dbfile_name);
			return dbFileName != null ? new File(dbFileName) : null;
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
				return 0;
			}
		}

	}

	private final MyProperties properties;
	private final PersistenceManager persistenceManager;
	private final AletheiaJPanel aletheiaJPanel;

	private boolean active;

	public SimpleAletheiaJFrame(SimpleAletheiaGUI aletheiaGUI) throws InterruptedException
	{
		super(aletheiaGUI);
		this.properties = new MyProperties();
		BerkeleyDBPersistenceManager.Configuration configuration = new BerkeleyDBPersistenceManager.Configuration();
		if (properties.getDbFile() == null)
			throw new RuntimeException("No DB file location configured");
		configuration.setStartupProgressListener(new SplashStartupProgressListener());
		configuration.setDbFile(properties.getDbFile());
		configuration.setReadOnly(properties.isReadOnly());
		configuration.setCachePercent(properties.getCachePercent());
		this.persistenceManager = new BerkeleyDBPersistenceManager(configuration);
		this.aletheiaJPanel = new AletheiaJPanel(this, this, persistenceManager);
		this.setContentPane(aletheiaJPanel);

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

}
