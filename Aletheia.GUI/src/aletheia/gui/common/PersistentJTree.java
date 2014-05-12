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

import javax.swing.JTree;

import aletheia.persistence.PersistenceManager;

public class PersistentJTree extends JTree
{
	private static final long serialVersionUID = 664849945430061056L;

	public PersistentJTree(PersistentTreeModel model)
	{
		super(model);
	}

	@Override
	public PersistentTreeModel getModel()
	{
		return (PersistentTreeModel) super.getModel();
	}

	public PersistenceManager getPersistenceManager()
	{
		return getModel().getPersistenceManager();
	}

	public void updateFontSize()
	{
		getModel().cleanRenderers();
		updateUI();
	}

}
