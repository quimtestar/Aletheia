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

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.Timer;

public class PopupManager
{
	private final static int delay = 1000;

	private final Component owner;
	private final Component contents;

	private Point where;
	private Popup popup;

	private class ShowingListener implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if ((where != null) && (popup == null))
			{
				popup = PopupFactory.getSharedInstance().getPopup(owner, contents, where.x, where.y);
				popup.show();
			}
		}
	}

	private final ShowingListener showingListener;

	private class ShowingTimer extends Timer
	{
		private static final long serialVersionUID = -561147883272299163L;

		public ShowingTimer()
		{
			super(delay, showingListener);
			setRepeats(false);
		}
	}

	private final ShowingTimer showingTimer;

	private class HidingListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (popup != null)
			{
				popup.hide();
				popup = null;
			}
		}
	}

	private final HidingListener hidingListener;

	private class HidingTimer extends Timer
	{
		private static final long serialVersionUID = 3008963401795796997L;

		public HidingTimer()
		{
			super(delay, hidingListener);
			setRepeats(false);
		}
	}

	private final HidingTimer hidingTimer;

	private class Listener implements MouseListener
	{

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
			hidingTimer.stop();
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
			hidingTimer.start();
		}
	}

	private final Listener listener;

	public PopupManager(Component owner, Component contents)
	{
		this.owner = owner;
		this.contents = contents;
		this.showingListener = new ShowingListener();
		this.showingTimer = new ShowingTimer();
		this.hidingListener = new HidingListener();
		this.hidingTimer = new HidingTimer();
		this.listener = new Listener();
		this.contents.addMouseListener(listener);
	}

	public void show(Point where)
	{
		this.where = where;
		hidingTimer.stop();
		showingTimer.start();
	}

	public void hide()
	{
		showingTimer.stop();
		hidingTimer.start();
	}

}
