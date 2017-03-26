package aletheia.parser.tokenprocessor;

import java.util.Map;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.model.term.FunctionTerm.NullParameterTypeException;
import aletheia.model.term.Term.ReplaceTypeException;
import aletheia.parser.TermParserException;
import aletheia.parser.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "B", right =
{ "B", "bar", "C" })
public class B_B_bar_C_TermTokenSubProcessor extends TermTokenSubProcessor
{

	protected B_B_bar_C_TermTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction,
			Map<ParameterRef, ParameterVariableTerm> tempParameterTable, Map<ParameterVariableTerm, Identifier> parameterIdentifiers) throws TermParserException
	{
		Term term = getProcessor().processTerm((NonTerminalToken) token.getChildren().get(0), input, context, transaction, tempParameterTable);
		Term oldTerm = getProcessor().processTerm((NonTerminalToken) token.getChildren().get(2), input, context, transaction, tempParameterTable);
		ParameterVariableTerm param = new ParameterVariableTerm(oldTerm.getType());
		try
		{
			return new FunctionTerm(param, term.replaceSubterm(oldTerm, param));
		}
		catch (ReplaceTypeException | NullParameterTypeException e)
		{
			throw new TermParserException(e, token.getStartLocation(), token.getStopLocation(), input);
		}
	}

}
