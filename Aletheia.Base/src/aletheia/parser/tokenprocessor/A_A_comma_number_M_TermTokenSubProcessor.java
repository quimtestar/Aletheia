/*******************************************************************************
 * Copyright (c) 2017 Quim Testar.
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
package aletheia.parser.tokenprocessor;

import java.util.List;
import java.util.Map;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.term.CompositionTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.TermParserException;
import aletheia.parser.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.BufferedList;

@ProcessorProduction(left = "A", right =
{ "A", "comma", "number", "M" })
public class A_A_comma_number_M_TermTokenSubProcessor extends TermTokenSubProcessor
{
	protected A_A_comma_number_M_TermTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction,
			Map<ParameterRef, ParameterVariableTerm> tempParameterTable, Map<ParameterVariableTerm, Identifier> parameterIdentifiers) throws TermParserException
	{
		Term term = getProcessor().processTerm((NonTerminalToken) token.getChildren().get(0), input, context, transaction, tempParameterTable);
		if (term instanceof CompositionTerm)
		{
			List<Term> components;
			if (getProcessor().processBoolean((NonTerminalToken) token.getChildren().get(3), input))
				components = new BufferedList<>(((CompositionTerm) term).aggregateComponents());
			else
				components = new BufferedList<>(((CompositionTerm) term).components());
			int n = Integer.parseInt(((TaggedTerminalToken) token.getChildren().get(2)).getText());
			if (n < 0 || n >= components.size())
				throw new TermParserException("Composition coordinate " + n + " out of bounds for term: " + "'" + term + "'",
						token.getChildren().get(2).getStartLocation(), token.getChildren().get(2).getStopLocation(), input);
			return components.get(n);
		}
		else
			throw new TermParserException("Only can use composition coordinates in compositions", token.getStartLocation(), token.getStopLocation(), input);
	}

}
