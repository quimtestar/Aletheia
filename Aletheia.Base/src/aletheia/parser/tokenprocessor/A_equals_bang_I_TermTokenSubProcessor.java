package aletheia.parser.tokenprocessor;

import java.util.Map;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.Statement;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.TermParserException;
import aletheia.parser.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "A", right =
{ "equals", "bang", "I" })
public class A_equals_bang_I_TermTokenSubProcessor extends TermTokenSubProcessor
{

	protected A_equals_bang_I_TermTokenSubProcessor(TokenProcessor tokenProcessor)
	{
		super(tokenProcessor);
	}

	@Override
	protected Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction,
			Map<ParameterRef, ParameterVariableTerm> tempParameterTable, Map<ParameterVariableTerm, Identifier> parameterIdentifiers) throws TermParserException
	{
		Identifier identifier = getProcessor().processIdentifier((NonTerminalToken) token.getChildren().get(2), input);
		Statement statement = context.identifierToStatement(transaction).get(identifier);
		if (statement instanceof Declaration)
			return ((Declaration) statement).getValue();
		else
			throw new TermParserException("Referenced statement: '" + identifier + "' after the bang must be a declaration",
					token.getChildren().get(2).getStartLocation(), token.getChildren().get(2).getStopLocation(), input);
	}

}
