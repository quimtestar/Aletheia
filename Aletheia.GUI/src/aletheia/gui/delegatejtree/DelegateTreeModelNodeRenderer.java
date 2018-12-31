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
package aletheia.gui.delegatejtree;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.apache.logging.log4j.Logger;

import aletheia.gui.common.renderer.PersistentJTreeNodeRenderer;
import aletheia.log4j.LoggerManager;

public abstract class DelegateTreeModelNodeRenderer extends PersistentJTreeNodeRenderer
{
	private static final long serialVersionUID = -7316932623303517961L;
	private static final Logger logger = LoggerManager.instance.logger();

	private class Listener implements KeyListener
	{

		@Override
		public void keyPressed(KeyEvent e)
		{
			switch (e.getKeyCode())
			{
			case KeyEvent.VK_DELETE:
				try
				{
					delete();
				}
				catch (InterruptedException e1)
				{
					logger.error(e1.getMessage(), e1);
				}
				break;
			case KeyEvent.VK_UP:
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_PAGE_UP:
			case KeyEvent.VK_PAGE_DOWN:
			case KeyEvent.VK_HOME:
			case KeyEvent.VK_END:
				getPersistentJTree().cancelEditing();
				Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(e);
				break;
			}
		}

		@Override
		public void keyTyped(KeyEvent e)
		{
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
		}
	}

	private final DelegateTreeModelNode delegateTreeModelNode;
	private final Listener listener;

	public DelegateTreeModelNodeRenderer(DelegateTreeJTree delegateTreeJTree, DelegateTreeModelNode delegateTreeModelNode)
	{
		super(delegateTreeJTree.getFontManager(), true, delegateTreeJTree, false);
		this.delegateTreeModelNode = delegateTreeModelNode;
		this.listener = new Listener();
		addKeyListener(listener);
	}

	@Override
	public DelegateTreeJTree getPersistentJTree()
	{
		return (DelegateTreeJTree) super.getPersistentJTree();
	}

	protected DelegateTreeModelNode getDelegateTreeModelNode()
	{
		return delegateTreeModelNode;
	}

	protected void delete() throws InterruptedException
	{
		getPersistentJTree().deleteNode(getDelegateTreeModelNode());
	}

}
