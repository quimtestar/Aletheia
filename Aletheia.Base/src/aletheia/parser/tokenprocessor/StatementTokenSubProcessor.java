package aletheia.parser.tokenprocessor;

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.parser.TermParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

public abstract class StatementTokenSubProcessor extends TokenSubProcessor<Statement, StatementTokenSubProcessor.Parameter>
{
	public static class Parameter
	{
		private final Context context;
		private final Transaction transaction;

		public Parameter(Context context, Transaction transaction)
		{
			super();
			this.context = context;
			this.transaction = transaction;
		}
	}

	protected StatementTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Statement subProcess(NonTerminalToken token, String input, Parameter parameter) throws TermParserException
	{
		return subProcess(token, input, parameter.context, parameter.transaction);
	}

	public abstract Statement subProcess(NonTerminalToken token, String input, Context context, Transaction transaction) throws TermParserException;

}
