package aletheia.parser.tokenprocessor;

import java.util.Collections;
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

@ProcessorProduction(left = "C", right =
{ "C", "question", "A" })
public class C_C_question_A_TermTokenSubProcessor extends TermTokenSubProcessor
{

	protected C_C_question_A_TermTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction,
			Map<ParameterRef, ParameterVariableTerm> tempParameterTable, Map<ParameterVariableTerm, Identifier> parameterIdentifiers) throws TermParserException
	{
		Term term = getProcessor().processTerm((NonTerminalToken) token.getChildren().get(0), input, context, transaction, tempParameterTable);
		Term termMatch = getProcessor().processTerm((NonTerminalToken) token.getChildren().get(2), input, context, transaction, tempParameterTable);
		if (!(termMatch instanceof FunctionTerm))
			throw new TermParserException("Not a function term.", token.getChildren().get(2).getStartLocation(), token.getChildren().get(2).getStopLocation(),
					input);
		FunctionTerm functionMatch = (FunctionTerm) termMatch;
		Term.Match match = functionMatch.getBody().match(Collections.singleton(functionMatch.getParameter()), term);
		if (match == null)
			throw new TermParserException("No match.", token.getStartLocation(), token.getStopLocation(), input);
		return match.getAssignMapLeft().get(functionMatch.getParameter());
	}

}
