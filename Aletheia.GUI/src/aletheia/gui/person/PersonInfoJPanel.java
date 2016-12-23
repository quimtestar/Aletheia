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
package aletheia.gui.person;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import aletheia.gui.common.renderer.BoldTextLabelRenderer;
import aletheia.gui.common.renderer.TextLabelRenderer;
import aletheia.gui.common.renderer.UUIDLabelRenderer;
import aletheia.model.authority.Person;

public class PersonInfoJPanel extends JPanel
{
	private static final long serialVersionUID = 829926598835206962L;
	private final Person person;

	public PersonInfoJPanel(Person person, Color textColor)
	{
		super();
		this.person = person;
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);
		setBackground(Color.white);
		Insets insets = new Insets(0, 0, 0, 10);
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.insets = insets;
			gbc.anchor = GridBagConstraints.WEST;
			add(new BoldTextLabelRenderer("UUID", textColor), gbc);
		}
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.insets = insets;
			gbc.anchor = GridBagConstraints.WEST;
			add(new UUIDLabelRenderer(person.getUuid(), textColor), gbc);
		}
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.insets = insets;
			gbc.anchor = GridBagConstraints.WEST;
			add(new BoldTextLabelRenderer("Nick", textColor), gbc);
		}
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = 1;
			gbc.insets = insets;
			gbc.anchor = GridBagConstraints.WEST;
			add(new TextLabelRenderer(person.getNick(), textColor), gbc);
		}
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 2;
			gbc.insets = insets;
			gbc.anchor = GridBagConstraints.WEST;
			add(new BoldTextLabelRenderer("Name", textColor), gbc);
		}
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = 2;
			gbc.insets = insets;
			gbc.anchor = GridBagConstraints.WEST;
			add(new TextLabelRenderer(person.getName(), textColor), gbc);
		}
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 3;
			gbc.insets = insets;
			gbc.anchor = GridBagConstraints.WEST;
			add(new BoldTextLabelRenderer("Email", textColor), gbc);
		}
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = 3;
			gbc.insets = insets;
			gbc.anchor = GridBagConstraints.WEST;
			add(new TextLabelRenderer(person.getEmail(), textColor), gbc);
		}
	}

	public Person getPerson()
	{
		return person;
	}

}
