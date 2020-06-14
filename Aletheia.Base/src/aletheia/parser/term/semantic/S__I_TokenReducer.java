/*******************************************************************************
 * Copyright (c) 2018 Quim Testar
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
package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;
import aletheia.persistence.collections.statement.GenericRootContextsMap;
import aletheia.utilities.MiscUtilities;

@AssociatedProduction(left = "S", right =
{ "I" })
public class S__I_TokenReducer extends ProductionTokenPayloadReducer<Statement>
{

	@Override
	public Statement reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		Context context = antecedentContext(globals, antecedents);
		Identifier identifier = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		if (context == null)
		{
			GenericRootContextsMap rcm = globals.getPersistenceManager().identifierToRootContexts(globals.getTransaction()).get(identifier);
			if (rcm == null || rcm.size() < 1)
				throw new SemanticException(reducees.get(0), "Identifier: " + "'" + identifier + "'" + " not defined at root level");
			if (rcm.size() > 1)
				throw new SemanticException(reducees.get(0), "Multiple root contexts with identifier: " + "'" + identifier + "'");
			else
				return MiscUtilities.firstFromCloseableIterable(rcm.values());
		}
		else
		{
			Statement statement = context.identifierToStatement(globals.getTransaction()).get(identifier);
			if (statement == null)
				throw new SemanticException(reducees.get(0), "Identifier: " + "'" + identifier + "'" + " not defined in context: \"" + context.label() + "\"");
			return statement;
		}
	}

}
