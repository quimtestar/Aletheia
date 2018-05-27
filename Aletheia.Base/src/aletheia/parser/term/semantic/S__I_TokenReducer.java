package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.statement.GenericRootContextsMap;
import aletheia.utilities.MiscUtilities;

@AssociatedProduction(left = "S", right =
{ "I" })
public class S__I_TokenReducer extends ProductionTokenPayloadReducer<Statement>
{

	@Override
	public Statement reduce(Context context, Transaction transaction, List<Token<? extends Symbol>> antecedents, Production production,
			List<Token<? extends Symbol>> reducees) throws SemanticException
	{
		Identifier identifier = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		if (context == null)
		{
			GenericRootContextsMap rcm = transaction.getPersistenceManager().identifierToRootContexts(transaction).get(identifier);
			if (rcm == null || rcm.size() < 1)
				throw new SemanticException(reducees.get(0), "Identifier: " + "'" + identifier + "'" + " not defined at root level");
			if (rcm.size() > 1)
				throw new SemanticException(reducees.get(0), "Multiple root contexts with identifier: " + "'" + identifier + "'");
			else
				return MiscUtilities.firstFromCloseableIterable(rcm.values());
		}
		else
		{
			Statement statement = context.identifierToStatement(transaction).get(identifier);
			if (statement == null)
				throw new SemanticException(reducees.get(0), "Identifier: " + "'" + identifier + "'" + " not defined in context: \"" + context.label() + "\"");
			return statement;
		}
	}

}