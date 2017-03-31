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

import aletheia.model.statement.Context;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.TermParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "R", right =
{ "R_t", "openpar", "S_ps", "closepar" })
public class R_Rt_openpar_Sps_closepar_ReferenceTokenSubProcessor extends ReferenceTokenSubProcessor
{

	protected R_Rt_openpar_Sps_closepar_ReferenceTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction) throws TermParserException
	{
		ReferenceType referenceType = getProcessor().processReferenceType((NonTerminalToken) token.getChildren().get(0));
		Term term = getProcessor().processStatementReference((NonTerminalToken) token.getChildren().get(2), input, context, transaction, referenceType);
		for (IdentifiableVariableTerm v : term.freeIdentifiableVariables())
		{
			if (context == null)
				throw new TermParserException("Referenced term contains free variables", token.getChildren().get(2).getStartLocation(),
						token.getChildren().get(2).getStopLocation(), input);
			else if (!context.statements(transaction).containsKey(v))
			{
				throw new TermParserException("Referenced term contains free variables not of this context", token.getChildren().get(2).getStartLocation(),
						token.getChildren().get(2).getStopLocation(), input);
			}
		}
		return term;
	}

}
