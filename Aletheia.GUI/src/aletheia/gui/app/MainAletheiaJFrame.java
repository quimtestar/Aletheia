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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import org.apache.logging.log4j.Logger;

import aletheia.common.AletheiaConstants;
import aletheia.gui.icons.IconManager;
import aletheia.gui.menu.AletheiaJMenuBar;
import aletheia.gui.person.PersonsDialog;
import aletheia.gui.preferences.GUIAletheiaPreferences;
import aletheia.gui.preferences.PeerToPeerNodeGender;
import aletheia.gui.preferences.PersistenceClass;
import aletheia.log4j.LoggerManager;
import aletheia.peertopeer.FemalePeerToPeerNode;
import aletheia.peertopeer.MalePeerToPeerNode;
import aletheia.peertopeer.PeerToPeerNode;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.exceptions.PersistenceLockTimeoutException;
import aletheia.persistence.gui.PersistenceGUIFactory.EncapsulatedCreatePersistenceManagerException;
import aletheia.persistence.gui.PersistenceGUIFactory.RedialogCreatePersistenceManagerException;
import aletheia.utilities.MiscUtilities;

public class MainAletheiaJFrame extends AletheiaJFrame
{
	private static final long serialVersionUID = 3366046447565592510L;
	private static final Logger logger = LoggerManager.instance.logger();

	public static enum ExitState
	{
		OPENED, EXIT, RESTART,
	};

	private class ExitLock
	{
		public ExitState state;

		public ExitLock(ExitState state)
		{
			super();
			this.state = state;
		}
	}

	private class MyWindowListener implements WindowListener
	{

		@Override
		public void windowActivated(WindowEvent e)
		{
		}

		@Override
		public void windowClosed(WindowEvent e)
		{
		}

		@Override
		public void windowClosing(WindowEvent e)
		{
			exit();
		}

		@Override
		public void windowDeactivated(WindowEvent e)
		{
		}

		@Override
		public void windowDeiconified(WindowEvent e)
		{
		}

		@Override
		public void windowIconified(WindowEvent e)
		{
		}

		@Override
		public void windowOpened(WindowEvent e)
		{
		}

	}

	private class MyThrowableProcessor implements AletheiaEventQueue.ThrowableProcessor<MainAletheiaJFrame, PersistenceLockTimeoutException>
	{

		@Override
		public boolean processThrowable(MainAletheiaJFrame aletheiaJFrame, PersistenceLockTimeoutException e)
		{
			aletheiaJFrame.lock(e.getOwners());
			return true;
		}

	}

	private static final Font menuFont = new Font(Font.SANS_SERIF, Font.BOLD, 14);

	private final AletheiaGUI aletheiaGUI;
	private final GUIAletheiaPreferences preferences;
	private final MyWindowListener windowListener;
	private final ExitLock exitLock;

	private class ExtraJFrame extends AletheiaJFrame
	{
		private static final long serialVersionUID = -6280529817834182993L;

		private final AletheiaJPanel aletheiaJPanel;

		public ExtraJFrame(PersistenceManager persistenceManager, String extraTitle) throws InterruptedException
		{
			super();
			this.aletheiaJPanel = new AletheiaJPanel(MainAletheiaJFrame.this, this, persistenceManager);
			this.setExtraTitle(extraTitle);
			this.setIconImages(IconManager.instance.aletheiaIconList);
			this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			this.setContentPane(aletheiaJPanel);
			this.pack();
			this.setVisible(true);
		}

		@Override
		public void setExtraTitle(String extraTitle)
		{
			setTitle(AletheiaConstants.TITLE + (extraTitle != null ? " " + extraTitle : ""));
		}

		@Override
		public void exit()
		{
			try
			{
				close();
			}
			catch (InterruptedException | IOException e)
			{
				logger.error("Exception caught", e);
			}
			dispose();
		}

		private void updateFontSize()
		{
			aletheiaJPanel.updateFontSize();
		}

		private void close() throws InterruptedException, IOException
		{
			aletheiaJPanel.close();
		}

	}

	private final Collection<ExtraJFrame> extraJFrames;

	private PersistenceManager persistenceManager;
	private AbstractAletheiaContentPane aletheiaContentPane;
	private PeerToPeerNode peerToPeerNode;
	private PersonsDialog personsDialog;

	public MainAletheiaJFrame(AletheiaGUI aletheiaGUI)
	{
		super();
		this.aletheiaGUI = aletheiaGUI;
		this.preferences = GUIAletheiaPreferences.instance;
		this.setPreferredSize(preferences.appearance().aletheiaJFrameBounds().getPreferredSize());
		this.setLocation(preferences.appearance().aletheiaJFrameBounds().getLocation());
		this.aletheiaGUI.getAletheiaEventQueue().addThrowableProcessor(this, PersistenceLockTimeoutException.class, new MyThrowableProcessor());
		this.setTitle(AletheiaConstants.TITLE);
		this.setIconImages(IconManager.instance.aletheiaIconList);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setJMenuBar(new AletheiaJMenuBar(this));
		this.windowListener = new MyWindowListener();
		this.addWindowListener(windowListener);
		this.exitLock = new ExitLock(ExitState.OPENED);
		this.extraJFrames = Collections.synchronizedSet(new HashSet<ExtraJFrame>());
		updateContentPane(true);
		updateServerStatus(true);
	}

	public AletheiaGUI getAletheiaGUI()
	{
		return aletheiaGUI;
	}

	@Override
	public void setExtraTitle(String extraTitle)
	{
		throw new UnsupportedOperationException("Main frame's title change not supported.");
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public PeerToPeerNode getPeerToPeerNode()
	{
		return peerToPeerNode;
	}

	public AletheiaEventQueue getAletheiaEventQueue()
	{
		return aletheiaGUI.getAletheiaEventQueue();
	}

	@Override
	public AletheiaJMenuBar getJMenuBar()
	{
		return (AletheiaJMenuBar) super.getJMenuBar();
	}

	public void waitCursor(boolean wait)
	{
		if (wait)
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		else
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	private void shutdown()
	{
		try
		{
			if (peerToPeerNode != null)
				peerToPeerNode.shutdown(true);
			if (personsDialog != null)
				personsDialog.close();
			aletheiaContentPane.close();
			extraJFramesClose();
			if (persistenceManager != null)
			{
				persistenceManager.close();
				persistenceManager = null;
			}
		}
		catch (Exception ex)
		{
			logger.error(ex.getMessage(), ex);
			JOptionPane.showMessageDialog(this, MiscUtilities.wrapText(ex.getMessage(), 80), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void exit(ExitState state)
	{
		synchronized (exitLock)
		{
			if (!state.equals(exitLock.state))
			{
				preferences.appearance().aletheiaJFrameBounds().setPreferredSize(new Dimension(getWidth() + 2, getHeight() + 2));
				preferences.appearance().aletheiaJFrameBounds().setLocation(getLocation());
				shutdown();
				dispose();
				exitLock.state = state;
				exitLock.notify();
			}
		}

	}

	@Override
	public void exit()
	{
		exit(ExitState.EXIT);
	}

	public ExitState waitForClose() throws InterruptedException
	{
		synchronized (exitLock)
		{
			while (exitLock.state == ExitState.OPENED)
				exitLock.wait();
		}
		return exitLock.state;
	}

	public boolean updateServerStatus(boolean init)
	{
		if (persistenceManager == null)
			return false;
		PeerToPeerNodeGender p2pGender = preferences.peerToPeerNode().getP2pGender();
		try
		{
			boolean created = false;
			switch (p2pGender)
			{
			case FEMALE:
			{
				if (peerToPeerNode != null && !(peerToPeerNode instanceof FemalePeerToPeerNode))
				{
					peerToPeerNode.shutdown(true);
					peerToPeerNode = null;
				}
				InetAddress inetAddress = preferences.peerToPeerNode().femalePeerToPeerNode().getP2pExternalAddress();
				int serverPort = preferences.peerToPeerNode().femalePeerToPeerNode().getP2pExternalPort();
				InetSocketAddress externalSocketAddress = new InetSocketAddress(inetAddress, serverPort);
				if (peerToPeerNode == null)
				{
					peerToPeerNode = new FemalePeerToPeerNode(persistenceManager, externalSocketAddress);
					created = true;
				}
				FemalePeerToPeerNode femalePeerToPeerNode = (FemalePeerToPeerNode) peerToPeerNode;
				if (!femalePeerToPeerNode.externalBindSocketAddress().equals(externalSocketAddress))
					femalePeerToPeerNode.externalBind(externalSocketAddress);
				break;
			}
			case MALE:
			{
				if (peerToPeerNode != null && !(peerToPeerNode instanceof MalePeerToPeerNode))
				{
					peerToPeerNode.shutdown(true);
					peerToPeerNode = null;
				}
				InetAddress address = InetAddress.getByName(preferences.peerToPeerNode().malePeerToPeerNode().getP2pSurrogateAddress());
				int port = preferences.peerToPeerNode().malePeerToPeerNode().getP2pSurrogatePort();
				InetSocketAddress surrogateAddress = new InetSocketAddress(address, port);
				if (peerToPeerNode != null)
				{
					if (!((MalePeerToPeerNode) peerToPeerNode).getSurrogateAddress().equals(surrogateAddress))
					{
						peerToPeerNode.shutdown(true);
						peerToPeerNode = null;
					}
				}
				if (peerToPeerNode == null)
				{
					peerToPeerNode = new MalePeerToPeerNode(persistenceManager, surrogateAddress);
					created = true;
				}
				break;
			}
			case DISABLED:
			{
				if (peerToPeerNode != null)
				{
					peerToPeerNode.shutdown(true);
					peerToPeerNode = null;
				}
				break;
			}
			default:
				throw new Error();
			}
			if (created)
			{
				/*
				 * Capture exception notifications and forward'em to the GUI
				
					peerToPeerNode.addListener(new PeerToPeerNode.Listener()
					{
						@Override
						public void exception(String message, Exception e)
						{
							if (aletheiaContentPane != null)
								aletheiaContentPane.exception(message, e);
						}
					});
				 */
			}

			return true;
		}
		catch (Exception e)
		{
			if (peerToPeerNode != null)
			{
				try
				{
					peerToPeerNode.shutdown(true);
				}
				catch (IOException e1)
				{
				}
				catch (InterruptedException e1)
				{
				}
				peerToPeerNode = null;
			}
			logger.error(e.getMessage(), e);
			JOptionPane.showMessageDialog(this, MiscUtilities.wrapText(e.getMessage(), 80), "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

	}

	private void refreshContentPane()
	{
		setContentPane(aletheiaContentPane);
		pack();
	}

	public boolean updateContentPane(boolean init)
	{
		PersistenceClass persistenceClass = preferences.getPersistenceClass();
		if (!init)
		{
			if (persistenceClass.persistenceGUIFactory.getPreferences().configurationMatches(persistenceManager))
				return true;
		}
		try
		{
			if (aletheiaContentPane != null)
			{
				aletheiaContentPane.close();
				aletheiaContentPane.setVisible(false);
				extraJFramesClose();
			}
			if (persistenceManager != null)
			{
				if (peerToPeerNode != null)
				{
					peerToPeerNode.shutdown(true);
					peerToPeerNode = null;
				}
				if (personsDialog != null)
				{
					personsDialog.close();
					personsDialog = null;
				}
				persistenceManager.close();
				persistenceManager = null;
			}
			SplashStartupProgressListener startupProgressListener = new SplashStartupProgressListener();
			try
			{
				persistenceManager = persistenceClass.persistenceGUIFactory.createPersistenceManager(this, startupProgressListener);
				if (persistenceManager == null)
				{
					aletheiaContentPane = new VoidAletheiaContentPane();
					getJMenuBar().getDataMenu().getPersonsAction().setEnabled(false);
					getJMenuBar().getSecurityMenu().updatePersistenceManager();
				}
				else
				{
					if (persistenceManager.getSecretKeyManager().isSecretSet())
					{
						pack();
						setVisible(true);
					}
					aletheiaContentPane = new AletheiaJPanel(this, this, persistenceManager);
					getJMenuBar().getDataMenu().getPersonsAction().setEnabled(true);
					getJMenuBar().getSecurityMenu().updatePersistenceManager();
				}
				refreshContentPane();
			}
			catch (RedialogCreatePersistenceManagerException e)
			{
				aletheiaContentPane = new VoidAletheiaContentPane();
				getJMenuBar().getDataMenu().getPersonsAction().setEnabled(false);
				getJMenuBar().getSecurityMenu().updatePersistenceManager();
				refreshContentPane();
				return false;
			}
			catch (EncapsulatedCreatePersistenceManagerException e)
			{
				throw e.getCause();
			}
			finally
			{
				startupProgressListener.close();
			}
		}
		catch (Exception e)
		{
			if (persistenceManager != null)
			{
				try
				{
					persistenceManager.close();
				}
				catch (Exception e_)
				{

				}
				persistenceManager = null;
			}
			aletheiaContentPane = new VoidAletheiaContentPane();
			refreshContentPane();
			String message = e.getMessage();
			if (message == null)
				message = e.toString();
			logger.error(message, e);
			JOptionPane.showMessageDialog(this, MiscUtilities.wrapText(message, 80), "Error", JOptionPane.ERROR_MESSAGE);
			return false;

		}
		finally
		{
		}

		return true;
	}

	public Font getMenuFont()
	{
		return menuFont;
	}

	public void updateFontSize()
	{
		aletheiaContentPane.updateFontSize();
		if (personsDialog != null)
			personsDialog.updateFontSize();
		extraJFramesUpdateFontSize();
	}

	public void lock(Collection<Transaction> owners)
	{
		aletheiaContentPane.lock(owners);
	}

	public PersonsDialog getPersonsDialog()
	{
		if (personsDialog == null && persistenceManager != null)
			personsDialog = new PersonsDialog(this);
		return personsDialog;
	}

	public void fatalError(Throwable e)
	{
		JOptionPane.showMessageDialog(this, MiscUtilities.wrapText(e.getMessage(), 80), "Error", JOptionPane.ERROR_MESSAGE);
		exit();
	}

	public void openExtraFrame(String extraTitle) throws InterruptedException
	{
		final ExtraJFrame frame = new ExtraJFrame(persistenceManager, extraTitle);
		frame.addWindowListener(new WindowListener()
		{

			@Override
			public void windowOpened(WindowEvent e)
			{
			}

			@Override
			public void windowClosing(WindowEvent e)
			{
			}

			@Override
			public void windowClosed(WindowEvent e)
			{
				try
				{
					frame.close();
				}
				catch (InterruptedException | IOException e1)
				{
					logger.error("Exception caught", e1);
				}
				extraJFrames.remove(frame);
			}

			@Override
			public void windowIconified(WindowEvent e)
			{
			}

			@Override
			public void windowDeiconified(WindowEvent e)
			{
			}

			@Override
			public void windowActivated(WindowEvent e)
			{
			}

			@Override
			public void windowDeactivated(WindowEvent e)
			{
			}
		});
		extraJFrames.add(frame);
	}

	public void openExtraFrame() throws InterruptedException
	{
		openExtraFrame(null);
	}

	public void extraJFramesUpdateFontSize()
	{
		synchronized (extraJFrames)
		{
			for (ExtraJFrame frame : extraJFrames)
				frame.updateFontSize();
		}
	}

	public void extraJFramesClose()
	{
		synchronized (extraJFrames)
		{
			for (ExtraJFrame frame : extraJFrames)
			{
				try
				{
					frame.close();
				}
				catch (InterruptedException | IOException e)
				{
					logger.error("Exception caught", e);
				}
				frame.dispose();
			}
		}
	}

}
