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
package aletheia.gui.catalogjtree;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.TransferHandler;

import aletheia.gui.common.renderer.PersistentJTreeNodeRenderer;
import aletheia.model.catalog.Catalog;

public abstract class CatalogJTreeNodeRenderer extends PersistentJTreeNodeRenderer
{
	private static final long serialVersionUID = 5730310422944823498L;

	private final CatalogTreeNode node;

	private class Listener implements MouseListener, KeyListener
	{

		boolean draggable = false;

		@Override
		public void mouseClicked(MouseEvent e)
		{
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			draggable = true;
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			draggable = false;
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
			draggable = false;
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
			if (draggable && ((e.getModifiers() & MouseEvent.MOUSE_PRESSED) != 0))
				getCatalogJTree().getTransferHandler().exportAsDrag(getCatalogJTree(), e, TransferHandler.COPY);
		}

		@Override
		public void keyPressed(KeyEvent ev)
		{
			switch (ev.getKeyCode())
			{
			case KeyEvent.VK_UP:
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_PAGE_UP:
			case KeyEvent.VK_PAGE_DOWN:
			case KeyEvent.VK_HOME:
			case KeyEvent.VK_END:
				getCatalogJTree().cancelEditing();
				Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(ev);
				break;
			}
		}

		@Override
		public void keyReleased(KeyEvent ev)
		{
		}

		@Override
		public void keyTyped(KeyEvent ev)
		{
		}

	}

	private final Listener listener;

	public CatalogJTreeNodeRenderer(CatalogJTree catalogJTree, CatalogTreeNode node)
	{
		super(catalogJTree.getFontManager(), true, catalogJTree, false);
		this.node = node;
		this.listener = new Listener();
		addMouseListener(listener);
		addKeyListener(listener);
	}

	@Override
	public CatalogJTree getPersistentJTree()
	{
		return (CatalogJTree) super.getPersistentJTree();
	}

	protected CatalogJTree getCatalogJTree()
	{
		return getPersistentJTree();
	}

	protected CatalogTreeNode getNode()
	{
		return node;
	}

	protected Catalog getCatalog()
	{
		return node.getCatalog();
	}

}
