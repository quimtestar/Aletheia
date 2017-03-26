package aletheia.parser.tokenprocessor;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.parser.TermParserException;
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
	public Statement subProcess(NonTerminalToken token, String input, Context context, Transaction transaction) throws TermParserException
	{
		Identifier identifier = getProcessor().processIdentifier((NonTerminalToken) token.getChildren().get(0), input);
		if (context == null)
		{
			GenericRootContextsMap rcm = transaction.getPersistenceManager().identifierToRootContexts(transaction).get(identifier);
			if (rcm == null || rcm.size() < 1)
				throw new TermParserException("Identifier: " + "'" + identifier + "'" + " not defined at root level",
						token.getChildren().get(0).getStartLocation(), token.getChildren().get(0).getStopLocation(), input);
			if (rcm.size() > 1)
				throw new TermParserException("Multiple root contexts with identifier: " + "'" + identifier + "'",
						token.getChildren().get(0).getStartLocation(), token.getChildren().get(0).getStopLocation(), input);
			else
				return MiscUtilities.firstFromCloseableIterable(rcm.values());
		}
		else
		{
			Statement statement = context.identifierToStatement(transaction).get(identifier);
			if (statement == null)
				throw new TermParserException("Identifier: " + "'" + identifier + "'" + " not defined in context: \"" + context.label() + "\"",
						token.getChildren().get(0).getStartLocation(), token.getChildren().get(0).getStopLocation(), input);
			return statement;
		}
	}

}
