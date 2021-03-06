/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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
import aletheia.model.statement.Specialization;
import aletheia.persistence.Transaction;

public class SpecializationContextJTreeNodeRenderer extends ProperStatementContextJTreeNodeRenderer<Specialization>
{
	private static final long serialVersionUID = 8330012958934365101L;

	protected SpecializationContextJTreeNodeRenderer(ContextJTree contextJTree, Specialization specialization)
	{
		super(contextJTree, specialization);
		Transaction transaction = contextJTree.getModel().beginTransaction();
		try
		{
			setActiveFont(getItalicFont());
			addSpaceLabel();
			addOpenSquareBracket();
			addSpecializationLabel();
			addColonLabel();
			addTerm(specialization.parentVariableToIdentifier(transaction), specialization.getGeneral(transaction).getVariable());
			addSpaceLabel();
			addReverseArrowLabel();
			addSpaceLabel();
			addTerm(specialization.parentVariableToIdentifier(transaction), specialization.getInstanceParameterIdentification(), specialization.getInstance());
			if (!specialization.getInstanceProof(transaction).getVariable().equals(specialization.getInstance()))
			{
				addSpaceLabel();
				addAlmostEqualLabel();
				addSpaceLabel();
				addTerm(specialization.parentVariableToIdentifier(transaction), specialization.getInstanceProof(transaction).getVariable());
			}
			addCloseSquareBracket();
		}
		finally
		{
			transaction.abort();
		}
	}

	public Specialization getSpecialization()
	{
		return getStatement();
	}

}
