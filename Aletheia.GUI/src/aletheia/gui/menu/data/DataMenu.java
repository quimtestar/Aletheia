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
package aletheia.gui.menu.data;

import java.awt.event.KeyEvent;

import aletheia.gui.menu.AletheiaJMenu;
import aletheia.gui.menu.AletheiaJMenuBar;
import aletheia.gui.menu.AletheiaMenuItem;

public class DataMenu extends AletheiaJMenu
{
	private static final long serialVersionUID = 5598459073059823214L;

	private final PersonsAction personsAction;

	public DataMenu(AletheiaJMenuBar aletheiaJMenuBar)
	{
		super(aletheiaJMenuBar, "Data", KeyEvent.VK_D);
		this.personsAction = new PersonsAction(this);
		this.add(new AletheiaMenuItem(personsAction));
	}

	public PersonsAction getPersonsAction()
	{
		return personsAction;
	}

	public void updatePersistenceManager()
	{
		boolean enable = getAletheiaJFrame().getPersistenceManager() != null;
		personsAction.setEnabled(enable);
	}
}
