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
package aletheia.gui.common.renderer;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import aletheia.gui.common.PopupManager;
import aletheia.gui.person.PersonInfoJPanel;
import aletheia.model.authority.Person;
import aletheia.model.authority.PrivatePerson;

public class PersonLabelRenderer extends AbstractRenderer
{
	private static final long serialVersionUID = -6400156605798606875L;

	private static final Color defaultColor = AbstractRenderer.getDefaultColor();
	private static final Color privateColor = AbstractRenderer.getPrivatePersonColor();

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
			displayPopup(e.getLocationOnScreen());
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
			undisplayPopup();
		}
	}

	private final Person person;
	private final Color color;
	private final JLabel personLabel;
	private final Listener listener;
	private final PersonInfoJPanel personInfoJPanel;
	private final PopupManager popupManager;

	public PersonLabelRenderer(Person person)
	{
		super();
		this.person = person;
		this.color = person instanceof PrivatePerson ? privateColor : defaultColor;
		this.personLabel = addPersonReference(person, color);
		this.listener = new Listener();
		this.personLabel.addMouseListener(listener);
		this.personInfoJPanel = new PersonInfoJPanel(person, color);
		this.personInfoJPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		this.popupManager = new PopupManager(this, personInfoJPanel);
		addAncestorListener(new AncestorListener()
		{

			@Override
			public void ancestorAdded(AncestorEvent event)
			{
			}

			@Override
			public void ancestorRemoved(AncestorEvent event)
			{
				undisplayPopup();
			}

			@Override
			public void ancestorMoved(AncestorEvent event)
			{
			}
		});
	}

	protected Person getPerson()
	{
		return person;
	}

	public void displayPopup(final Point point)
	{
		popupManager.show(point);
	}

	public void undisplayPopup()
	{
		popupManager.hide();
	}

}
