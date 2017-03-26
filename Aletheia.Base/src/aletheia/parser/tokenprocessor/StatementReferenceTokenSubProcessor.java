package aletheia.parser.tokenprocessor;

import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.model.term.Term;
import aletheia.parser.TermParserException;
import aletheia.parsergenerator.tokens.Location;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

public abstract class StatementReferenceTokenSubProcessor extends TokenSubProcessor<Term, StatementReferenceTokenSubProcessor.Parameter>
{
	public static class Parameter
	{
		private final Context context;
		private final Transaction transaction;
		private final ReferenceType referenceType;

		private Parameter(Context context, Transaction transaction, ReferenceType referenceType)
		{
			super();
			this.context = context;
			this.transaction = transaction;
			this.referenceType = referenceType;
		}

	}

	protected StatementReferenceTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Term subProcess(NonTerminalToken token, String input, Parameter parameter) throws TermParserException
	{
		return subProcess(token, input, parameter.context, parameter.transaction, parameter.referenceType);
	}

	public abstract Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction, ReferenceType referenceType)
			throws TermParserException;

	protected Term dereferenceStatement(Statement statement, ReferenceType referenceType, Location startLocation, Location stopLocation, String input)
			throws TermParserException
	{
		switch (referenceType)
		{
		case TYPE:
			return statement.getTerm();
		case INSTANCE:
		{
			if (!(statement instanceof Specialization))
				throw new TermParserException("Cannot reference the instance of a non-specialization statement", startLocation, stopLocation, input);
			return ((Specialization) statement).getInstance();
		}
		case VALUE:
		{
			if (!(statement instanceof Declaration))
				throw new TermParserException("Cannot reference the value of a non-declaration statement", startLocation, stopLocation, input);
			return ((Declaration) statement).getValue();
		}
		default:
			throw new Error();
		}
	}

}
