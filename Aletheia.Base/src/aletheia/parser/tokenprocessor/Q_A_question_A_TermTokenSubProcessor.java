package aletheia.parser.tokenprocessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.model.term.VariableTerm;
import aletheia.parser.TermParserException;
import aletheia.parser.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "Q", right =
{ "A", "question", "A" })
public class Q_A_question_A_TermTokenSubProcessor extends TermTokenSubProcessor
{

	protected Q_A_question_A_TermTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction,
			Map<ParameterRef, ParameterVariableTerm> tempParameterTable, Map<ParameterVariableTerm, Identifier> parameterIdentifiers) throws TermParserException
	{
		Term termMatch = getProcessor().processTerm((NonTerminalToken) token.getChildren().get(0), input, context, transaction, tempParameterTable);
		Term term = getProcessor().processTerm((NonTerminalToken) token.getChildren().get(2), input, context, transaction, tempParameterTable);
		List<VariableTerm> assignable = new ArrayList<>();
		Term.Match match = termMatch.consequent(assignable).match(new HashSet<>(assignable), term);
		if (match == null)
			throw new TermParserException("No match.", token.getStartLocation(), token.getStopLocation(), input);
		if (assignable.isEmpty())
			throw new TermParserException("Nothing assignable.", token.getStartLocation(), token.getStopLocation(), input);
		Term assigned = match.getAssignMapLeft().get(assignable.get(0));
		if (assigned == null)
			throw new TermParserException("Nothing assignable.", token.getStartLocation(), token.getStopLocation(), input);
		return assigned;
	}

}
