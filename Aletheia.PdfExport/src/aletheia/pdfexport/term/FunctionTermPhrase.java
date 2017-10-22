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
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.model.term.Term.ParameterNumerator;
import aletheia.model.term.VariableTerm;
import aletheia.pdfexport.SimpleChunk;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;

public class FunctionTermPhrase extends TermPhrase
{
	private static final long serialVersionUID = -357818252943847397L;

	protected FunctionTermPhrase(PersistenceManager persistenceManager, Transaction transaction, Map<? extends VariableTerm, Identifier> variableToIdentifier,
			ParameterNumerator parameterNumerator, FunctionTerm functionTerm)
	{
		super(functionTerm);
		addSimpleChunk(new SimpleChunk("<"));
		Term term = functionTerm;
		boolean first = true;
		int numberedParameters = 0;
		while (term instanceof FunctionTerm)
		{
			ParameterVariableTerm parameter = ((FunctionTerm) term).getParameter();
			Term body = ((FunctionTerm) term).getBody();
			if (!first)
				addSimpleChunk(new SimpleChunk(", "));
			TermPhrase parameterTypePhrase = termPhrase(persistenceManager, transaction, variableToIdentifier, parameterNumerator, parameter.getType());
			if (body.isFreeVariable(parameter))
			{
				if (!variableToIdentifier.containsKey(parameter))
				{
					parameterNumerator.numberParameter(parameter);
					numberedParameters++;
				}
				addBasePhrase(termPhrase(persistenceManager, transaction, variableToIdentifier, parameterNumerator, parameter));
				addSimpleChunk(new SimpleChunk(":"));
			}
			addBasePhrase(parameterTypePhrase);
			first = false;
			term = body;
		}
		addSimpleChunk(new SimpleChunk(" \u2192 "));
		addBasePhrase(termPhrase(persistenceManager, transaction, variableToIdentifier, parameterNumerator, term));
		parameterNumerator.unNumberParameters(numberedParameters);
		addSimpleChunk(new SimpleChunk(">"));
	}

	@Override
	public FunctionTerm getTerm()
	{
		return (FunctionTerm) super.getTerm();
	}

}
