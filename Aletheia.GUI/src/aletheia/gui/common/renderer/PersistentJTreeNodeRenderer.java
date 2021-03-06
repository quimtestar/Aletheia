/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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

import aletheia.gui.common.PersistentJTree;
import aletheia.gui.fonts.FontManager;
import aletheia.persistence.Transaction;

public abstract class PersistentJTreeNodeRenderer extends AbstractPersistentRenderer
{
	private static final long serialVersionUID = -4744347980265636950L;

	private final PersistentJTree persistentJTree;

	public PersistentJTreeNodeRenderer(FontManager fontManager, boolean border, PersistentJTree persistentJTree, boolean highlightVariableReferences)
	{
		super(fontManager, border, persistentJTree.getPersistenceManager(), highlightVariableReferences);
		this.persistentJTree = persistentJTree;
	}

	public PersistentJTreeNodeRenderer(FontManager fontManager, PersistentJTree persistentJTree, boolean highlightVariableReferences)
	{
		this(fontManager, false, persistentJTree, highlightVariableReferences);
	}

	public PersistentJTree getPersistentJTree()
	{
		return persistentJTree;
	}

	@Override
	protected Transaction beginTransaction()
	{
		return persistentJTree.getModel().beginTransaction();
	}

}
