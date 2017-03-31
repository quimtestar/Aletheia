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

import java.util.UUID;

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.term.Term;
import aletheia.parser.TermParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.TerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "S_ts", right =
{ "uuid" })
public class Sts_uuid_StatementReferenceTokenSubProcessor extends StatementReferenceTokenSubProcessor
{

	protected Sts_uuid_StatementReferenceTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	public Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction, ReferenceType referenceType)
			throws TermParserException
	{
		UUID uuid = getProcessor().processUuid((TerminalToken) token.getChildren().get(0), input);
		Statement statement = transaction.getPersistenceManager().getStatement(transaction, uuid);
		if (statement == null)
			throw new TermParserException("Statement not found with UUID: " + uuid, token.getChildren().get(0).getStartLocation(),
					token.getChildren().get(0).getStopLocation(), input);
		return dereferenceStatement(statement, referenceType, token.getChildren().get(0).getStartLocation(), token.getChildren().get(0).getStopLocation(),
				input);
	}

}
