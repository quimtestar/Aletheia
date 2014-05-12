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
package aletheia.gui.common;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.plaf.LayerUI;

import aletheia.gui.app.AletheiaEventQueue;
import aletheia.gui.app.AletheiaJFrame;
import aletheia.persistence.Transaction;
import aletheia.persistence.exceptions.PersistenceLockTimeoutException;

public class PersistentJTreeLayerUI<T extends JComponent> extends LayerUI<T>
{
	private static final long serialVersionUID = 1563872290147699211L;

	private class MyThrowableProcessor implements AletheiaEventQueue.ThrowableProcessor<T, PersistenceLockTimeoutException>
	{
		protected MyThrowableProcessor()
		{
			super();
		}

		@Override
		public boolean processThrowable(T persistentJTree, PersistenceLockTimeoutException e)
		{
			lock(e.getOwners());
			return true;
		}

	}

	public static class LockGlassPane extends JPanel
	{
		private static final long serialVersionUID = -9115248462459573413L;

		public static class Listener implements MouseListener, KeyListener
		{

			@Override
			public void keyTyped(KeyEvent e)
			{
			}

			@Override
			public void keyPressed(KeyEvent e)
			{
			}

			@Override
			public void keyReleased(KeyEvent e)
			{
			}

			@Override
			public void mouseClicked(MouseEvent e)
			{
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
			}

		}

		public LockGlassPane()
		{
			Listener listener = new Listener();
			addMouseListener(listener);
			addKeyListener(listener);
			setOpaque(false);
		}
	}

	private final AletheiaJFrame aletheiaJFrame;
	private final MyThrowableProcessor myThrowableProcessor;
	private final LockGlassPane lockGlassPane;

	private class LockTransactionManager
	{
		private final Set<Transaction> pending;

		private class TransactionHook implements Transaction.Hook
		{

			@Override
			public void run(Transaction closedTransaction)
			{
				unlock(closedTransaction);
			}
		}

		private final TransactionHook transactionHook;

		private LockTransactionManager()
		{
			this.pending = new HashSet<Transaction>();
			this.transactionHook = new TransactionHook();
		}

		private synchronized void lock(Collection<? extends Transaction> transactions)
		{
			lockJLayer(jLayer);
			for (Transaction transaction : transactions)
			{
				pending.add(transaction);
				transaction.runWhenClose(transactionHook);
			}
		}

		private synchronized void unlock(Transaction transaction)
		{
			pending.remove(transaction);
			if (pending.isEmpty())
				unlockJLayer(jLayer);
		}

	}

	private final LockTransactionManager lockTransactionManager;

	private JLayer<T> jLayer;

	public PersistentJTreeLayerUI(AletheiaJFrame aletheiaJFrame, T view)
	{
		super();
		this.aletheiaJFrame = aletheiaJFrame;
		this.myThrowableProcessor = new MyThrowableProcessor();
		this.lockGlassPane = new LockGlassPane();
		this.lockTransactionManager = new LockTransactionManager();
		JLayer<T> jLayer_ = new JLayer<T>(view, this);
		if (jLayer != jLayer_)
			throw new RuntimeException();
	}

	public JLayer<T> getJLayer()
	{
		return jLayer;
	}

	@Override
	public void paint(Graphics g, JComponent c)
	{
		try
		{
			super.paint(g, c);
		}
		catch (PersistenceLockTimeoutException e)
		{
		}
	}

	@Override
	public Dimension getPreferredSize(JComponent c)
	{
		try
		{
			return super.getPreferredSize(c);
		}
		catch (PersistenceLockTimeoutException e)
		{
			return new Dimension(1, 1);
		}
	}

	@SuppressWarnings("unchecked")
	private JLayer<T> castToJLayer(JComponent c)
	{
		return (JLayer<T>) c;
	}

	@Override
	public synchronized void installUI(JComponent c)
	{
		super.installUI(c);
		if (jLayer != null)
			throw new RuntimeException();
		jLayer = castToJLayer(c);
		jLayer.setGlassPane(lockGlassPane);
		aletheiaJFrame.getAletheiaEventQueue().addThrowableProcessor(jLayer.getView(), PersistenceLockTimeoutException.class, myThrowableProcessor);
	}

	@Override
	public synchronized void uninstallUI(JComponent c)
	{
		super.uninstallUI(c);
		if (jLayer != c)
			throw new RuntimeException();
		aletheiaJFrame.getAletheiaEventQueue().removeThrowableProcessor(jLayer.getView(), PersistenceLockTimeoutException.class, myThrowableProcessor);
		jLayer = null;
	}

	private synchronized void lockJLayer(JLayer<T> jLayer)
	{
		jLayer.getGlassPane().setVisible(true);
		jLayer.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

	private synchronized void unlockJLayer(JLayer<T> jLayer)
	{
		jLayer.getGlassPane().setVisible(false);
		jLayer.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	public synchronized void lock(Collection<? extends Transaction> owners)
	{
		lockTransactionManager.lock(owners);
	}

}
