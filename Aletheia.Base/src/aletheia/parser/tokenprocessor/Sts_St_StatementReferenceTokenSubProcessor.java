package aletheia.parser.tokenprocessor;

import aletheia.model.statement.Context;
import aletheia.model.term.Term;
import aletheia.parser.TermParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "S_ts", right =
{ "S_t" })
public class Sts_St_StatementReferenceTokenSubProcessor extends StatementReferenceTokenSubProcessor
{

	protected Sts_St_StatementReferenceTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	public Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction, ReferenceType referenceType)
			throws TermParserException
	{
		return getProcessor().processStatementReference((NonTerminalToken) token.getChildren().get(0), input, context, transaction, referenceType);
	}

}
