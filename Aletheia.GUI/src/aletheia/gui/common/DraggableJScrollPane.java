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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JScrollPane;

public class DraggableJScrollPane extends JScrollPane
{
	private static final long serialVersionUID = -3805468761267919902L;

	private class Listener implements MouseListener, MouseMotionListener
	{
		private Point originDragRelativePoint = null;
		private Point originScrollPoint = null;

		private Point relativePoint(MouseEvent e)
		{
			Point scrollPoint = getScrollPoint();
			Point relativePoint = new Point(e.getPoint());
			relativePoint.translate(-scrollPoint.x, -scrollPoint.y);
			return relativePoint;
		}

		@Override
		public void mouseClicked(MouseEvent e)
		{
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			if (dragging)
			{
				originDragRelativePoint = relativePoint(e);
				originScrollPoint = getScrollPoint();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			originDragRelativePoint = null;
			originScrollPoint = null;
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
			if (dragging && originDragRelativePoint != null && originScrollPoint != null)
			{
				Point dragRelativePoint = relativePoint(e);
				Point scrollPoint = new Point(originScrollPoint);
				scrollPoint.translate(originDragRelativePoint.x - dragRelativePoint.x, originDragRelativePoint.y - dragRelativePoint.y);
				setScrollPoint(scrollPoint);
			}
		}

		@Override
		public void mouseMoved(MouseEvent e)
		{
		}

	}

	private final Listener listener;
	private boolean dragging;

	public DraggableJScrollPane(Component view, Component source)
	{
		super(view);
		this.listener = new Listener();
		source.addMouseListener(this.listener);
		source.addMouseMotionListener(this.listener);
		this.dragging = false;
	}

	public DraggableJScrollPane(Component view)
	{
		this(view, view);
	}

	public boolean isDragging()
	{
		return dragging;
	}

	public void setDragging(boolean dragging)
	{
		this.dragging = dragging;
	}

	private Point getScrollPoint()
	{
		return new Point(getHorizontalScrollBar().getValue(), getVerticalScrollBar().getValue());
	}

	private void setScrollPoint(Point p)
	{
		getHorizontalScrollBar().setValue(p.x);
		getVerticalScrollBar().setValue(p.y);
	}

}
