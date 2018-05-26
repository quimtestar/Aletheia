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
import aletheia.parser.TokenProcessorException;
import aletheia.parsergenerator.semantic.ParseTree;
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
	public Statement subProcess(ParseTree token, Context context, Transaction transaction) throws TokenProcessorException
	{
		Identifier identifier = getProcessor().processIdentifier((ParseTree) token.getChildren().get(0));
		if (context == null)
		{
			GenericRootContextsMap rcm = transaction.getPersistenceManager().identifierToRootContexts(transaction).get(identifier);
			if (rcm == null || rcm.size() < 1)
				throw new TokenProcessorException("Identifier: " + "'" + identifier + "'" + " not defined at root level",
						token.getChildren().get(0).getStartLocation(), token.getChildren().get(0).getStopLocation());
			if (rcm.size() > 1)
				throw new TokenProcessorException("Multiple root contexts with identifier: " + "'" + identifier + "'",
						token.getChildren().get(0).getStartLocation(), token.getChildren().get(0).getStopLocation());
			else
				return MiscUtilities.firstFromCloseableIterable(rcm.values());
		}
		else
		{
			Statement statement = context.identifierToStatement(transaction).get(identifier);
			if (statement == null)
				throw new TokenProcessorException("Identifier: " + "'" + identifier + "'" + " not defined in context: \"" + context.label() + "\"",
						token.getChildren().get(0).getStartLocation(), token.getChildren().get(0).getStopLocation());
			return statement;
		}
	}

}
