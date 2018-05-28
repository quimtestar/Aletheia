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