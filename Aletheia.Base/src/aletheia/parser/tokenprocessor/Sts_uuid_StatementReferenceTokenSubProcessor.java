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
