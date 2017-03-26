package aletheia.parser.tokenprocessor;

import aletheia.model.statement.Context;
import aletheia.model.term.Term;
import aletheia.parser.TermParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "R", right =
{ "R_t", "S_ts" })
public class R_Rt_Sts_ReferenceTokenSubProcessor extends ReferenceTokenSubProcessor
{

	protected R_Rt_Sts_ReferenceTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction) throws TermParserException
	{
		ReferenceType referenceType = getProcessor().processReferenceType((NonTerminalToken) token.getChildren().get(0));
		return getProcessor().processStatementReference((NonTerminalToken) token.getChildren().get(1), input, context, transaction, referenceType);
	}

}
