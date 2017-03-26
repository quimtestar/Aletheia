package aletheia.parser.tokenprocessor;

import java.util.Map;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.ProjectionTerm;
import aletheia.model.term.Term;
import aletheia.model.term.ProjectionTerm.ProjectionTypeException;
import aletheia.parser.TermParserException;
import aletheia.parser.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "A", right =
{ "A", "asterisk" })
public class A_A_asterisk_TermTokenSubProcessor extends TermTokenSubProcessor
{
	protected A_A_asterisk_TermTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction,
			Map<ParameterRef, ParameterVariableTerm> tempParameterTable, Map<ParameterVariableTerm, Identifier> parameterIdentifiers) throws TermParserException
	{
		Term term = getProcessor().processTerm((NonTerminalToken) token.getChildren().get(0), input, context, transaction, tempParameterTable);
		if (term instanceof FunctionTerm)
		{
			try
			{
				return new ProjectionTerm((FunctionTerm) term);
			}
			catch (ProjectionTypeException e)
			{
				throw new TermParserException(e, token.getStartLocation(), token.getStopLocation(), input);
			}
		}
		else
			throw new TermParserException("Only can project a function term", token.getStartLocation(), token.getStopLocation(), input);
	}

}
