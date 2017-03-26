package aletheia.parser.tokenprocessor;

import java.util.Map;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.TermParserException;
import aletheia.parser.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

public abstract class TermTokenSubProcessor extends TokenSubProcessor<Term, TermTokenSubProcessor.Parameter>
{

	public static class Parameter
	{
		private final Context context;
		private final Transaction transaction;
		private final Map<ParameterRef, ParameterVariableTerm> tempParameterTable;
		private final Map<ParameterVariableTerm, Identifier> parameterIdentifiers;

		public Parameter(Context context, Transaction transaction, Map<ParameterRef, ParameterVariableTerm> tempParameterTable,
				Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
		{
			super();
			this.context = context;
			this.transaction = transaction;
			this.tempParameterTable = tempParameterTable;
			this.parameterIdentifiers = parameterIdentifiers;
		}

	}

	public TermTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Term subProcess(NonTerminalToken token, String input, Parameter parameter) throws TermParserException
	{
		return subProcess(token, input, parameter.context, parameter.transaction, parameter.tempParameterTable, parameter.parameterIdentifiers);
	}

	protected abstract Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction,
			Map<ParameterRef, ParameterVariableTerm> tempParameterTable, Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
			throws TermParserException;

}
