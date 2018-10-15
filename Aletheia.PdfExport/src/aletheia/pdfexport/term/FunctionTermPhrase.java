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

import java.util.HashMap;
import java.util.Map;

import aletheia.model.identifier.Identifier;
import aletheia.model.parameteridentification.FunctionParameterIdentification;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.model.term.Term.ParameterNumerator;
import aletheia.pdfexport.SimpleChunk;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.CombinedMap;

public class FunctionTermPhrase extends TermPhrase
{
	private static final long serialVersionUID = -357818252943847397L;

	protected FunctionTermPhrase(PersistenceManager persistenceManager, Transaction transaction, Map<IdentifiableVariableTerm, Identifier> variableToIdentifier,
			ParameterNumerator parameterNumerator, ParameterIdentification parameterIdentification,
			Map<ParameterVariableTerm, Identifier> parameterToIdentifier, FunctionTerm functionTerm)
	{
		super(functionTerm);
		Map<ParameterVariableTerm, Identifier> localParameterToIdentifier = new HashMap<>();
		Map<ParameterVariableTerm, Identifier> totalParameterToIdentifier = parameterToIdentifier == null ? localParameterToIdentifier
				: new CombinedMap<>(localParameterToIdentifier, parameterToIdentifier);
		addSimpleChunk(new SimpleChunk("<"));
		Term term = functionTerm;
		boolean first = true;
		int numberedParameters = 0;
		while (term instanceof FunctionTerm)
		{
			ParameterVariableTerm parameter = ((FunctionTerm) term).getParameter();
			Term body = ((FunctionTerm) term).getBody();
			Identifier parameterParameterIdentification = null;
			ParameterIdentification domainParameterIdentification = null;
			ParameterIdentification bodyParameterIdentification = null;
			if (parameterIdentification instanceof FunctionParameterIdentification)
			{
				parameterParameterIdentification = ((FunctionParameterIdentification) parameterIdentification).getParameter();
				domainParameterIdentification = ((FunctionParameterIdentification) parameterIdentification).getDomain();
				bodyParameterIdentification = ((FunctionParameterIdentification) parameterIdentification).getBody();
			}
			if (!first)
				addSimpleChunk(new SimpleChunk(", "));
			TermPhrase parameterTypePhrase = termPhrase(persistenceManager, transaction, variableToIdentifier, parameterNumerator,
					domainParameterIdentification, totalParameterToIdentifier, parameter.getType());
			if (body.isFreeVariable(parameter))
			{
				if (parameterParameterIdentification == null)
				{
					parameterNumerator.numberParameter(parameter);
					numberedParameters++;
				}
				else
					localParameterToIdentifier.put(parameter, parameterParameterIdentification);
				addBasePhrase(new ParameterVariableTermPhrase(parameter, localParameterToIdentifier, parameterNumerator));
				addSimpleChunk(new SimpleChunk(":"));
			}
			addBasePhrase(parameterTypePhrase);
			first = false;
			term = body;
			parameterIdentification = bodyParameterIdentification;
		}
		addSimpleChunk(new SimpleChunk(" \u2192 "));
		addBasePhrase(termPhrase(persistenceManager, transaction, variableToIdentifier, parameterNumerator, parameterIdentification, totalParameterToIdentifier,
				term));
		parameterNumerator.unNumberParameters(numberedParameters);
		addSimpleChunk(new SimpleChunk(">"));
	}

	@Override
	public FunctionTerm getTerm()
	{
		return (FunctionTerm) super.getTerm();
	}

}
