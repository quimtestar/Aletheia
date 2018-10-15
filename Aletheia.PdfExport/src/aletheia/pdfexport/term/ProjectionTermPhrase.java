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
package aletheia.pdfexport.term;

import java.util.Map;
import java.util.Stack;

import aletheia.model.identifier.Identifier;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.term.CompositionTerm;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.ProjectionTerm;
import aletheia.model.term.Term;
import aletheia.model.term.Term.ParameterNumerator;
import aletheia.pdfexport.SimpleChunk;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;

public class ProjectionTermPhrase extends TermPhrase
{
	private static final long serialVersionUID = -2536637561143669857L;

	protected ProjectionTermPhrase(PersistenceManager persistenceManager, Transaction transaction,
			Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator,
			ParameterIdentification parameterIdentification, Map<ParameterVariableTerm, Identifier> parameterToIdentifier, ProjectionTerm projectionTerm)
	{
		super(projectionTerm);
		Term term = projectionTerm;
		Stack<ParameterVariableTerm> stack = new Stack<>();
		while (term instanceof ProjectionTerm)
		{
			FunctionTerm function = ((ProjectionTerm) term).getFunction();
			stack.push(function.getParameter());
			term = function.getBody();
		}
		int nProjections = stack.size();
		while (!stack.isEmpty())
		{
			term = new FunctionTerm(stack.pop(), term);
		}
		addBasePhrase(TermPhrase.termPhrase(persistenceManager, transaction, variableToIdentifier, parameterNumerator, parameterIdentification,
				parameterToIdentifier, term));
		String sProjections;
		if (nProjections <= 3)
		{
			StringBuilder sbProjections = new StringBuilder();
			for (int i = 0; i < nProjections; i++)
				sbProjections.append("*");
			sProjections = sbProjections.toString();
		}
		else
			sProjections = "*" + nProjections;
		addSimpleChunk(new SimpleChunk(sProjections));
	}

	@Override
	public CompositionTerm getTerm()
	{
		return (CompositionTerm) super.getTerm();
	}

}
