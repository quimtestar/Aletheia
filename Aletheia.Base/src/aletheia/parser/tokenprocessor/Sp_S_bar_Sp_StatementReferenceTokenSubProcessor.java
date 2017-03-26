package aletheia.parser.tokenprocessor;

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.term.Term;
import aletheia.parser.TermParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "S_p", right =
{ "S", "bar", "S_p" })
public class Sp_S_bar_Sp_StatementReferenceTokenSubProcessor extends StatementReferenceTokenSubProcessor
{

	protected Sp_S_bar_Sp_StatementReferenceTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	public Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction, ReferenceType referenceType)
			throws TermParserException
	{
		Statement st = getProcessor().processStatement((NonTerminalToken) token.getChildren().get(0), input, context, transaction);
		if (!(st instanceof Context))
			throw new TermParserException("Statement: " + "\"" + st.label() + "\"" + " not a context", token.getChildren().get(0).getStartLocation(),
					token.getChildren().get(0).getStopLocation(), input);
		return getProcessor().processStatementReference((NonTerminalToken) token.getChildren().get(2), input, (Context) st, transaction, referenceType);
	}

}
