package aletheia.parser.tokenprocessor;

import java.util.Map;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.model.term.VariableTerm;
import aletheia.parser.TermParserException;
import aletheia.parser.tokenprocessor.parameterRef.NumberedParameterRef;
import aletheia.parser.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "A", right =
{ "atparam" })
public class A_atparam_TermTokenSubProcessor extends TermTokenSubProcessor
{
	protected A_atparam_TermTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction,
			Map<ParameterRef, ParameterVariableTerm> tempParameterTable, Map<ParameterVariableTerm, Identifier> parameterIdentifiers) throws TermParserException
	{
		String atParam = ((TaggedTerminalToken) token.getChildren().get(0)).getText();
		VariableTerm variable = tempParameterTable.get(new NumberedParameterRef(atParam));
		if (variable == null)
			throw new TermParserException("Parameter:" + "'" + atParam + "'" + " not defined", token.getChildren().get(0).getStartLocation(),
					token.getChildren().get(0).getStopLocation(), input);
		return variable;
	}

}
