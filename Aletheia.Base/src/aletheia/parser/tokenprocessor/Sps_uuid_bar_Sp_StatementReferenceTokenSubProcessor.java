package aletheia.parser.tokenprocessor;

import java.util.UUID;

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.term.Term;
import aletheia.parser.TermParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.TerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "S_ps", right =
{ "uuid", "bar", "S_p" })
public class Sps_uuid_bar_Sp_StatementReferenceTokenSubProcessor extends StatementReferenceTokenSubProcessor
{

	protected Sps_uuid_bar_Sp_StatementReferenceTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	public Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction, ReferenceType referenceType)
			throws TermParserException
	{
		UUID uuid = getProcessor().processUuid((TerminalToken) token.getChildren().get(0), input);
		Statement st = transaction.getPersistenceManager().getStatement(transaction, uuid);
		if (st == null)
			throw new TermParserException("Statement not found with UUID: " + uuid, token.getChildren().get(0).getStartLocation(),
					token.getChildren().get(0).getStopLocation(), input);
		if (!(st instanceof Context))
			throw new TermParserException("Statement: " + "\"" + st.label() + "\"" + " not a context", token.getChildren().get(0).getStartLocation(),
					token.getChildren().get(0).getStopLocation(), input);
		return getProcessor().processStatementReference((NonTerminalToken) token.getChildren().get(2), input, (Context) st, transaction, referenceType);
	}

}
