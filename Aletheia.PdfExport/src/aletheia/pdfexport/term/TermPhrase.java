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

import aletheia.model.identifier.Identifier;
import aletheia.model.term.CompositionTerm;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.ProjectionTerm;
import aletheia.model.term.SimpleTerm;
import aletheia.model.term.TauTerm;
import aletheia.model.term.Term;
import aletheia.model.term.VariableTerm;
import aletheia.pdfexport.BasePhrase;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;

public abstract class TermPhrase extends BasePhrase
{
	private static final long serialVersionUID = -8646175155849019680L;

	private final Term term;

	protected TermPhrase(Term term)
	{
		super();
		this.term = term;
	}

	public Term getTerm()
	{
		return term;
	}

	public static TermPhrase termPhrase(PersistenceManager persistenceManager, Transaction transaction,
			Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, Term term)
	{
		return termPhrase(persistenceManager, transaction, variableToIdentifier, term.parameterNumerator(), term);
	}

	protected static TermPhrase termPhrase(PersistenceManager persistenceManager, Transaction transaction,
			Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator, Term term)
	{
		if (term instanceof SimpleTerm)
		{
			if (term instanceof CompositionTerm)
				return new CompositionTermPhrase(persistenceManager, transaction, variableToIdentifier, parameterNumerator, (CompositionTerm) term);
			else if (term instanceof VariableTerm)
			{
				if (term instanceof IdentifiableVariableTerm)
					return new IdentifiableVariableTermReferencePhrase(persistenceManager, transaction, variableToIdentifier, (IdentifiableVariableTerm) term);
				else if (term instanceof ParameterVariableTerm)
					return new ParameterVariableTermPhrase(parameterNumerator, (VariableTerm) term);
				else
					throw new Error();
			}
			else if (term instanceof TauTerm)
				return new TauTermPhrase((TauTerm) term);
			else if (term instanceof ProjectionTerm)
				return new ProjectionTermPhrase(persistenceManager, transaction, variableToIdentifier, parameterNumerator, (ProjectionTerm) term);
			else
				throw new Error();
		}
		else if (term instanceof FunctionTerm)
			return new FunctionTermPhrase(persistenceManager, transaction, variableToIdentifier, parameterNumerator, (FunctionTerm) term);
		else
			throw new Error();
	}

}
