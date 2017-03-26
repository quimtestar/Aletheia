package aletheia.parser.tokenprocessor;

import aletheia.model.statement.Context;
import aletheia.model.term.Term;
import aletheia.parser.TermParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

public abstract class ReferenceTokenSubProcessor extends TokenSubProcessor<Term, ReferenceTokenSubProcessor.Parameter>
{
	public static class Parameter
	{
		private final Context context;
		private final Transaction transaction;

		private Parameter(Context context, Transaction transaction)
		{
			super();
			this.context = context;
			this.transaction = transaction;
		}

	}

	protected ReferenceTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Term subProcess(NonTerminalToken token, String input, Parameter parameter) throws TermParserException
	{
		return subProcess(token, input, parameter.context, parameter.transaction);
	}

	protected abstract Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction) throws TermParserException;

}
