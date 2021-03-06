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
import aletheia.model.statement.Declaration;
import aletheia.model.statement.Statement;
import aletheia.model.term.Term;
import aletheia.model.term.Term.ReplaceTypeException;
import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "A", right =
{ "A", "bang", "I" })
public class A__A_bang_I_TokenReducer extends ProductionTokenPayloadReducer<Term>
{

	@Override
	public Term reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		Term term = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		Identifier identifier = NonTerminalToken.getPayloadFromTokenList(reducees, 2);
		Statement statement = globals.getContext().identifierToStatement(globals.getTransaction()).get(identifier);
		if (statement instanceof Declaration)
		{
			Declaration declaration = (Declaration) statement;
			if (globals.getParameterIdentifiers() != null)
				globals.getParameterIdentifiers().putAll(declaration.getValue().parameterIdentifierMap(declaration.getValueParameterIdentification()));
			try
			{
				return term.replace(declaration.getVariable(), declaration.getValue());
			}
			catch (ReplaceTypeException e)
			{
				throw new SemanticException(reducees, e);
			}
		}
		else
			throw new SemanticException(reducees.get(2), "Referenced statement: '" + identifier + "' after the bang must be a declaration");
	}

}
