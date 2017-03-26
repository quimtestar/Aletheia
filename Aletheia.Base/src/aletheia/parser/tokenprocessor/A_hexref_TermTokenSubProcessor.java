package aletheia.parser.tokenprocessor;

import java.util.Map;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.TermParserException;
import aletheia.parser.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "A", right =
{ "hexref" })
public class A_hexref_TermTokenSubProcessor extends TermTokenSubProcessor
{
	protected A_hexref_TermTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction,
			Map<ParameterRef, ParameterVariableTerm> tempParameterTable, Map<ParameterVariableTerm, Identifier> parameterIdentifiers) throws TermParserException
	{
		String hexRef = ((TaggedTerminalToken) token.getChildren().get(0)).getText();
		Statement statement = context.getStatementByHexRef(transaction, hexRef, 5000);
		if (statement == null)
			throw new TermParserException("Reference not found on context", token.getChildren().get(0).getStartLocation(),
					token.getChildren().get(0).getStopLocation(), input);
		return statement.getVariable();
	}

}
