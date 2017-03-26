package aletheia.parser.tokenprocessor;

import java.util.Map;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.TermParserException;
import aletheia.parser.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "A", right =
{ "A", "apostrophe" })
public class A_A_apostrophe_TermTokenSubProcessor extends TermTokenSubProcessor
{
	protected A_A_apostrophe_TermTokenSubProcessor(TokenProcessor processor)
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
			FunctionTerm functionTerm = (FunctionTerm) term;
			Term body = functionTerm.getBody();
			if (body.freeVariables().contains(functionTerm.getParameter()))
				throw new TermParserException("Function's body depends on function parameter", token.getStartLocation(), token.getStopLocation(), input);
			else
				return body;
		}
		else
			throw new TermParserException("Only can take the body of a function term", token.getStartLocation(), token.getStopLocation(), input);
	}

}
