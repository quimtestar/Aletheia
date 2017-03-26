package aletheia.parser.tokenprocessor;

import aletheia.model.statement.Context;
import aletheia.model.term.Term;
import aletheia.parser.TermParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "S_t", right =
{ "turnstile" })
public class St_turnstile_StatementReferenceTokenSubProcessor extends StatementReferenceTokenSubProcessor
{

	protected St_turnstile_StatementReferenceTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	public Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction, ReferenceType referenceType)
			throws TermParserException
	{
		if (context == null)
			throw new TermParserException("Cannot refer to the consequent without a context", token.getChildren().get(0).getStartLocation(),
					token.getChildren().get(0).getStopLocation(), input);
		if (referenceType != ReferenceType.TYPE)
			throw new TermParserException("Invalid reference type to the consequent", token.getChildren().get(0).getStartLocation(),
					token.getChildren().get(0).getStopLocation(), input);
		return context.getConsequent();
	}

}
