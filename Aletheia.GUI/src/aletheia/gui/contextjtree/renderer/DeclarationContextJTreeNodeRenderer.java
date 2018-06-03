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
package aletheia.gui.contextjtree.renderer;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.model.statement.Declaration;
import aletheia.persistence.Transaction;

public class DeclarationContextJTreeNodeRenderer extends ProperStatementContextJTreeNodeRenderer<Declaration>
{
	private static final long serialVersionUID = 6841890763302299054L;

	protected DeclarationContextJTreeNodeRenderer(ContextJTree contextJTree, Declaration declaration)
	{
		super(contextJTree, declaration);
		Transaction transaction = contextJTree.getModel().beginTransaction();
		try
		{
			setActiveFont(getItalicFont());
			addSpaceLabel();
			addOpenSquareBracket();
			addDeclarationLabel();
			addColonLabel();
			addTerm(transaction, declaration, declaration.getValue());
			if (!declaration.getValueProof(transaction).getVariable().equals(declaration.getValue()))
			{
				addSpaceLabel();
				addAlmostEqualLabel();
				addSpaceLabel();
				addTerm(declaration.parentVariableToIdentifier(transaction), declaration.getValueProof(transaction).getVariable());
			}
			addCloseSquareBracket();
		}
		finally
		{
			transaction.abort();
		}

	}

	public Declaration getDeclaration()
	{
		return getStatement();
	}
}
