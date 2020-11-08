/*******************************************************************************
 * Copyright (c) 2014, 2015 Quim Testar.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.exceptions.PersistenceLockTimeoutException;
import aletheia.persistence.exceptions.PersistenceManagerClosedException;

public abstract class PersistentTreeModel implements TreeModel
{
	private final static Logger logger = LoggerManager.instance.logger();
	private final static int transactionTimeOut = 100;

	private final PersistenceManager persistenceManager;
	private final Collection<TreeModelListener> listeners;

	public PersistentTreeModel(PersistenceManager persistenceManager)
	{
		this.persistenceManager = persistenceManager;
		this.listeners = Collections.synchronizedCollection(new ArrayList<TreeModelListener>());
	}

	protected Collection<TreeModelListener> getListeners()
	{
		return Collections.unmodifiableCollection(listeners);
	}

	@Override
	public void addTreeModelListener(TreeModelListener listener)
	{
		listeners.add(listener);
	}

	@Override
	public void removeTreeModelListener(TreeModelListener listener)
	{
		listeners.remove(listener);

	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	//XXX: Ad hoc checked exception here to make sure every instance of PersistenceManager closed is controlled?
	public Transaction beginTransaction() throws PersistenceManagerClosedException
	{
		return getPersistenceManager().beginTransaction(transactionTimeOut);
	}

	public abstract void cleanRenderers();

	@Override
	public abstract TreeNode getRoot();

	@Override
	public abstract TreeNode getChild(Object parent, int index);

	private void persistenceLockTimeoutSwingInvokeLater(final Method method, final Object object, final Object... args)
	{
		new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					try
					{
						method.invoke(object, args);
					}
					catch (InvocationTargetException e)
					{
						throw e.getCause();
					}
				}
				catch (PersistenceLockTimeoutException e)
				{
					logger.debug("PersistenceLockTimeoutException. Re-invokeLatering method: " + method);
					SwingUtilities.invokeLater(this);
				}
				catch (Throwable e)
				{
					throw new RuntimeException(e);
				}
			}
		}.run();
	}

	private final static Method treeNodesChangedMethod;
	private final static Method treeNodesInsertedMethod;
	private final static Method treeNodesRemovedMethod;
	private final static Method treeStructureChangedMethod;

	static
	{
		try
		{
			treeNodesChangedMethod = TreeModelListener.class.getMethod("treeNodesChanged", TreeModelEvent.class);
			treeNodesInsertedMethod = TreeModelListener.class.getMethod("treeNodesInserted", TreeModelEvent.class);
			treeNodesRemovedMethod = TreeModelListener.class.getMethod("treeNodesRemoved", TreeModelEvent.class);
			treeStructureChangedMethod = TreeModelListener.class.getMethod("treeStructureChanged", TreeModelEvent.class);

		}
		catch (NoSuchMethodException | SecurityException e)
		{
			throw new Error(e);
		}
	}

	protected void persistenceLockTimeoutSwingInvokeLaterTreeNodesChanged(TreeModelListener listener, TreeModelEvent e)
	{
		persistenceLockTimeoutSwingInvokeLater(treeNodesChangedMethod, listener, e);
	}

	protected void persistenceLockTimeoutSwingInvokeLaterTreeNodesInserted(TreeModelListener listener, TreeModelEvent e)
	{
		persistenceLockTimeoutSwingInvokeLater(treeNodesInsertedMethod, listener, e);
	}

	protected void persistenceLockTimeoutSwingInvokeLaterTreeNodesRemoved(TreeModelListener listener, TreeModelEvent e)
	{
		persistenceLockTimeoutSwingInvokeLater(treeNodesRemovedMethod, listener, e);
	}

	protected void persistenceLockTimeoutSwingInvokeLaterTreeStructureChanged(TreeModelListener listener, TreeModelEvent e)
	{
		persistenceLockTimeoutSwingInvokeLater(treeStructureChangedMethod, listener, e);
	}

}
