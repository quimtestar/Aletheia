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

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.parser.AletheiaParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.statement.GenericRootContextsMap;
import aletheia.utilities.MiscUtilities;

@ProcessorProduction(left = "S", right = "I")
public class S_I_StatementTokenSubProcessor extends StatementTokenSubProcessor
{

	protected S_I_StatementTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	public Statement subProcess(NonTerminalToken token, String input, Context context, Transaction transaction) throws AletheiaParserException
	{
		Identifier identifier = getProcessor().processIdentifier((NonTerminalToken) token.getChildren().get(0), input);
		if (context == null)
		{
			GenericRootContextsMap rcm = transaction.getPersistenceManager().identifierToRootContexts(transaction).get(identifier);
			if (rcm == null || rcm.size() < 1)
				throw new AletheiaParserException("Identifier: " + "'" + identifier + "'" + " not defined at root level",
						token.getChildren().get(0).getStartLocation(), token.getChildren().get(0).getStopLocation(), input);
			if (rcm.size() > 1)
				throw new AletheiaParserException("Multiple root contexts with identifier: " + "'" + identifier + "'",
						token.getChildren().get(0).getStartLocation(), token.getChildren().get(0).getStopLocation(), input);
			else
				return MiscUtilities.firstFromCloseableIterable(rcm.values());
		}
		else
		{
			Statement statement = context.identifierToStatement(transaction).get(identifier);
			if (statement == null)
				throw new AletheiaParserException("Identifier: " + "'" + identifier + "'" + " not defined in context: \"" + context.label() + "\"",
						token.getChildren().get(0).getStartLocation(), token.getChildren().get(0).getStopLocation(), input);
			return statement;
		}
	}

}
