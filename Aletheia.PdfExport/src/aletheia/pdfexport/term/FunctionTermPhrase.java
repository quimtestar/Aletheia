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
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.Term.ParameterNumerator;
import aletheia.pdfexport.SimpleChunk;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;

public class FunctionTermPhrase extends TermPhrase
{
	private static final long serialVersionUID = -357818252943847397L;

	protected FunctionTermPhrase(PersistenceManager persistenceManager, Transaction transaction, Map<IdentifiableVariableTerm, Identifier> variableToIdentifier,
			ParameterNumerator parameterNumerator, FunctionTerm term)
	{
		super(term);
		addSimpleChunk(new SimpleChunk("<"));
		parameterNumerator.numberParameter(term.getParameter());
		addBasePhrase(termPhrase(persistenceManager, transaction, variableToIdentifier, parameterNumerator, term.getParameter()));
		parameterNumerator.unNumberParameter();
		addSimpleChunk(new SimpleChunk(":"));
		addBasePhrase(termPhrase(persistenceManager, transaction, variableToIdentifier, parameterNumerator, term.getParameter().getType()));
		addSimpleChunk(new SimpleChunk(" \u2192 "));
		parameterNumerator.numberParameter(term.getParameter());
		addBasePhrase(termPhrase(persistenceManager, transaction, variableToIdentifier, parameterNumerator, term.getBody()));
		parameterNumerator.unNumberParameter();
		addSimpleChunk(new SimpleChunk(">"));
	}

	@Override
	public FunctionTerm getTerm()
	{
		return (FunctionTerm) super.getTerm();
	}

}
