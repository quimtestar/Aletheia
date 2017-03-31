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
import aletheia.model.statement.Statement;
import aletheia.parser.TermParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "S", right = "hexref")
public class S_hexref_StatementTokenSubProcessor extends StatementTokenSubProcessor
{

	protected S_hexref_StatementTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	public Statement subProcess(NonTerminalToken token, String input, Context context, Transaction transaction) throws TermParserException
	{
		String hexRef = ((TaggedTerminalToken) token.getChildren().get(0)).getText();
		if (context == null)
		{
			Statement statement = transaction.getPersistenceManager().getRootContextByHexRef(transaction, hexRef);
			if (statement == null)
				throw new TermParserException("Reference: + " + "'" + hexRef + "'" + " not found on root level", token.getChildren().get(0).getStartLocation(),
						token.getChildren().get(0).getStopLocation(), input);
			return statement;
		}
		else
		{
			Statement statement = context.getStatementByHexRef(transaction, hexRef);
			if (statement == null)
				throw new TermParserException("Reference: + " + "'" + hexRef + "'" + " not found on context: \"" + context.label() + "\"",
						token.getChildren().get(0).getStartLocation(), token.getChildren().get(0).getStopLocation(), input);
			return statement;
		}
	}

}
