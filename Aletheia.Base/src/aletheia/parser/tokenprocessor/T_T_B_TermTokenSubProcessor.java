package aletheia.parser.tokenprocessor;

import java.util.Map;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.model.term.Term.ComposeTypeException;
import aletheia.parser.TermParserException;
import aletheia.parser.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "T", right =
{ "T", "B" })
public class T_T_B_TermTokenSubProcessor extends TermTokenSubProcessor
{

	protected T_T_B_TermTokenSubProcessor(TokenProcessor tokenProcessor)
	{
		super(tokenProcessor);
	}

	@Override
	protected Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction,
			Map<ParameterRef, ParameterVariableTerm> tempParameterTable, Map<ParameterVariableTerm, Identifier> parameterIdentifiers) throws TermParserException
	{
		Term term = getProcessor().processTerm((NonTerminalToken) token.getChildren().get(0), input, context, transaction, tempParameterTable);
		Term tail = getProcessor().processTerm((NonTerminalToken) token.getChildren().get(1), input, context, transaction, tempParameterTable);
		try
		{
			return term.compose(tail);
		}
		catch (ComposeTypeException e)
		{
			throw new TermParserException(e, token.getChildren().get(0).getStartLocation(), token.getChildren().get(0).getStopLocation(), input);
		}
	}

}
