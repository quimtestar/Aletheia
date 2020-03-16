/*******************************************************************************
 * Copyright (c) 2020 Quim Testar.
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
 *******************************************************************************/
package aletheia.gui.common;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

public class DraggableJScrollPane extends JScrollPane
{
	private static final long serialVersionUID = -3805468761267919902L;

	private class Listener implements MouseListener, MouseMotionListener
	{
		private Point originDragPoint = null;
		private Rectangle originViewRect = null;

		@Override
		public void mouseClicked(MouseEvent e)
		{
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			originDragPoint = e.getPoint();
			originViewRect = getViewport().getViewRect();
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			originDragPoint = null;
			originViewRect = null;
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
		}

		@Override
		public void mouseDragged(MouseEvent e)
		{
			if (originDragPoint != null && originViewRect != null)
			{
				Point dragPoint = e.getPoint();
				Rectangle viewRect = new Rectangle(originViewRect);
				viewRect.translate((int) Math.round(originDragPoint.getX() - dragPoint.getX()), (int) Math.round(originDragPoint.getY() - dragPoint.getY()));
				((JComponent) (getViewport().getView())).scrollRectToVisible(viewRect);
			}
		}

		@Override
		public void mouseMoved(MouseEvent e)
		{
		}

	}

	private final Listener listener;

	public DraggableJScrollPane(Component view)
	{
		super(view);
		this.listener = new Listener();
		getViewport().getView().addMouseListener(this.listener);
		getViewport().getView().addMouseMotionListener(this.listener);
	}

}
