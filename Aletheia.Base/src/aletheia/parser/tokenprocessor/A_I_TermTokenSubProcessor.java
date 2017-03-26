package aletheia.parser.tokenprocessor;

import java.util.Map;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.model.term.VariableTerm;
import aletheia.parser.TermParserException;
import aletheia.parser.tokenprocessor.parameterRef.IdentifierParameterRef;
import aletheia.parser.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "A", right =
{ "I" })
public class A_I_TermTokenSubProcessor extends TermTokenSubProcessor
{
	protected A_I_TermTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction,
			Map<ParameterRef, ParameterVariableTerm> tempParameterTable, Map<ParameterVariableTerm, Identifier> parameterIdentifiers) throws TermParserException
	{
		Identifier identifier = getProcessor().processIdentifier((NonTerminalToken) token.getChildren().get(0), input);
		VariableTerm variable = tempParameterTable.get(new IdentifierParameterRef(identifier));
		if (variable == null && context != null && transaction != null)
		{
			Statement statement = context.identifierToStatement(transaction).get(identifier);
			if (statement != null)
				variable = statement.getVariable();
		}
		if (variable == null)
			throw new TermParserException("Identifier:" + "'" + identifier + "'" + " not defined", token.getChildren().get(0).getStartLocation(),
					token.getChildren().get(0).getStopLocation(), input);
		return variable;
	}

}
