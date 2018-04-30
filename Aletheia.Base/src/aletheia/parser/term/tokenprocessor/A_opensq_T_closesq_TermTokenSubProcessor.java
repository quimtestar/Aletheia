package aletheia.parser.term.tokenprocessor;

import java.util.Map;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.ProjectedCastTypeTerm;
import aletheia.model.term.ProjectedCastTypeTerm.ProjectedCastTypeException;
import aletheia.model.term.Term;
import aletheia.parser.AletheiaParserException;
import aletheia.parser.term.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "A", right =
{ "opensq", "T", "closesq" })
public class A_opensq_T_closesq_TermTokenSubProcessor extends TermTokenSubProcessor
{
	protected A_opensq_T_closesq_TermTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction,
			Map<ParameterRef, ParameterVariableTerm> tempParameterTable, Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
			throws AletheiaParserException
	{
		try
		{
			return new ProjectedCastTypeTerm(
					getProcessor().processTerm((NonTerminalToken) token.getChildren().get(1), input, context, transaction, tempParameterTable));
		}
		catch (ProjectedCastTypeException e)
		{
			throw new AletheiaParserException(e, token.getChildren().get(1).getStartLocation(), token.getChildren().get(1).getStopLocation(), input);
		}
	}

}
