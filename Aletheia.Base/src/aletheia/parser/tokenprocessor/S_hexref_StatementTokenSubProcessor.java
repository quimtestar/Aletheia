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
