/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.logging.log4j.Logger;

import aletheia.gui.app.splash.AbstractSplashStartupProgressListener;
import aletheia.gui.icons.IconManager;
import aletheia.gui.lookandfeel.MyLookAndFeel;
import aletheia.gui.preferences.GUIAletheiaPreferences;
import aletheia.gui.preferences.PersistenceClass;
import aletheia.log4j.LoggerManager;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceConfiguration;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.gui.PersistenceGUIFactory.CreatePersistenceManagerException;
import aletheia.utilities.CommandLineArguments;
import aletheia.utilities.CommandLineArguments.Option;
import aletheia.utilities.CommandLineArguments.Switch;
import aletheia.utilities.MiscUtilities;

public class DesktopAletheiaGUI extends AletheiaGUI
{
	private final static Logger logger = LoggerManager.instance.logger();

	public DesktopAletheiaGUI(Map<String, Switch> globalSwitches)
	{
		super(globalSwitches);
	}

	public DesktopAletheiaGUI()
	{
		super();
	}

	@Override
	protected void run()
	{
		super.run();
		JFrame virtualFrame = new JFrame();
		virtualFrame.setIconImages(IconManager.instance.aletheiaIconList);
		run: while (true)
		{
			try
			{
				DesktopAletheiaJFrame aletheiaJFrame = new DesktopAletheiaJFrame(this);
				class ShutdownHook extends Thread implements AutoCloseable
				{
					private boolean ran = false;

					ShutdownHook()
					{
						super("ShutdownHook");
						Runtime.getRuntime().addShutdownHook(this);
					}

					@Override
					public synchronized void run()
					{
						aletheiaJFrame.exit();
						ran = true;
					}

					@Override
					public synchronized void close() throws Exception
					{
						if (!ran)
							Runtime.getRuntime().removeShutdownHook(this);
					}

				}
				try (ShutdownHook shutdownHook = new ShutdownHook())
				{
					aletheiaJFrame.pack();
					aletheiaJFrame.setVisible(true);
					DesktopAletheiaJFrame.ExitState state = aletheiaJFrame.waitForClose();
					aletheiaJFrame.dispose();
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

	private static class ArgumentsException extends Exception
	{
		private static final long serialVersionUID = -5342220710448665920L;

		private ArgumentsException(String message)
		{
			super(message);
		}

	}

	private static void console(Map<String, Switch> globalSwitches) throws CreatePersistenceManagerException, ArgumentsException
	{
		final PersistenceManager persistenceManager;
		try (AbstractSplashStartupProgressListener startupProgressListener = AbstractSplashStartupProgressListener.makeFromGlobalSwitches(globalSwitches))
		{
			Switch swDbFile = globalSwitches.remove("dbFile");
			if (swDbFile != null)
			{
				if (!(swDbFile instanceof Option))
					throw new ArgumentsException("Missing option dbFile");
				String sDbFile = ((Option) swDbFile).getValue();
				if (sDbFile == null)
					throw new ArgumentsException("Missing option value dbFile");
				File dbFile = new File(sDbFile);
				boolean readOnly = false;
				Switch swReadWrite = globalSwitches.remove("ro");
				if (swReadWrite != null)
					readOnly = true;
				boolean allowCreate = false;
				Switch swAllowCreate = globalSwitches.remove("ac");
				if (swAllowCreate != null)
					allowCreate = true;
				BerkeleyDBPersistenceConfiguration configuration = new BerkeleyDBPersistenceConfiguration();
				configuration.setDbFile(dbFile);
				configuration.setReadOnly(readOnly);
				configuration.setAllowCreate(allowCreate);
				configuration.setStartupProgressListener(startupProgressListener);
				persistenceManager = new BerkeleyDBPersistenceManager(configuration);
			}
			else
			{
				GUIAletheiaPreferences preferences = GUIAletheiaPreferences.instance;
				PersistenceClass persistenceClass = preferences.getPersistenceClass();
				persistenceManager = persistenceClass.persistenceGUIFactory.createPersistenceManager(null, startupProgressListener);
				if (persistenceManager == null)
					throw new ArgumentsException("Persistence manager not set in preferences.");
			}
		}
		try
		{
			Runtime.getRuntime().addShutdownHook(new Thread()
			{
				@Override
				public void run()
				{
					if (persistenceManager.isOpen())
						persistenceManager.close();
				}
			});

			AletheiaCliConsole cliConsole = AletheiaCliConsole.cliConsole(persistenceManager);
			cliConsole.run();
		}
		finally
		{
			persistenceManager.close();
		}
	}

	private static void gui(Map<String, Switch> globalSwitches) throws UnsupportedLookAndFeelException
	{
		Properties props = System.getProperties();
		props.setProperty("awt.useSystemAAFontSettings", "on");
		LoggerManager.instance.setUncaughtExceptionHandler();
		LookAndFeel laf = new MyLookAndFeel();
		UIManager.setLookAndFeel(laf);
		DesktopAletheiaGUI aletheiaGUI = new DesktopAletheiaGUI(globalSwitches);
		aletheiaGUI.run();
	}

	public static void main(String[] args)
	{
		try
		{
			CommandLineArguments cla = new CommandLineArguments(args);
			Map<String, Switch> globalSwitches = new HashMap<>(cla.getGlobalSwitches());
			if (globalSwitches.remove("version") != null)
				version();
			else if ((globalSwitches.remove("c") != null))
				console(globalSwitches);
			else if (GraphicsEnvironment.isHeadless())
			{
				logger.warn("Headless graphics environment detected. Switching to console mode.");
				console(globalSwitches);
			}
			else
				gui(globalSwitches);
			System.exit(0);
		}
		catch (Exception e)
		{
			logger.fatal("Startup error", e);
			System.exit(1);
		}
	}

}
