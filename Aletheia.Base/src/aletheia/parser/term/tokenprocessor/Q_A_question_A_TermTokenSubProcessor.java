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
package aletheia.parser.term.tokenprocessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.AletheiaParserException;
import aletheia.parser.term.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "Q", right =
{ "A", "question", "A" })
public class Q_A_question_A_TermTokenSubProcessor extends TermTokenSubProcessor
{

	protected Q_A_question_A_TermTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction,
			Map<ParameterRef, ParameterVariableTerm> tempParameterTable, Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
			throws AletheiaParserException
	{
		Term termMatch = getProcessor().processTerm((NonTerminalToken) token.getChildren().get(0), input, context, transaction, tempParameterTable);
		Term term = getProcessor().processTerm((NonTerminalToken) token.getChildren().get(2), input, context, transaction, tempParameterTable);
		List<ParameterVariableTerm> assignable = new ArrayList<>();
		Term.Match match = termMatch.consequent(assignable).match(new HashSet<>(assignable), term);
		if (match == null)
			throw new AletheiaParserException("No match.", token.getStartLocation(), token.getStopLocation(), input);
		if (assignable.isEmpty())
			throw new AletheiaParserException("Nothing assignable.", token.getStartLocation(), token.getStopLocation(), input);
		Term assigned = match.getAssignMapLeft().get(assignable.get(0));
		if (assigned == null)
			throw new AletheiaParserException("Nothing assignable.", token.getStartLocation(), token.getStopLocation(), input);
		return assigned;
	}

}