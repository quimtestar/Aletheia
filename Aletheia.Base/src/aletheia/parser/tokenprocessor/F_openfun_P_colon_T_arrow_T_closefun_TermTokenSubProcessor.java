package aletheia.parser.tokenprocessor;

import java.util.Map;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.model.term.FunctionTerm.NullParameterTypeException;
import aletheia.parser.TermParserException;
import aletheia.parser.tokenprocessor.parameterRef.IdentifierParameterRef;
import aletheia.parser.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "F", right =
{ "openfun", "P", "colon", "T", "arrow", "T", "closefun" })
public class F_openfun_P_colon_T_arrow_T_closefun_TermTokenSubProcessor extends TermTokenSubProcessor
{
	protected F_openfun_P_colon_T_arrow_T_closefun_TermTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction,
			Map<ParameterRef, ParameterVariableTerm> tempParameterTable, Map<ParameterVariableTerm, Identifier> parameterIdentifiers) throws TermParserException
	{
		ParameterRef parameterRef = getProcessor().processParameterRef((NonTerminalToken) token.getChildren().get(1), input);
		Term type = getProcessor().processTerm((NonTerminalToken) token.getChildren().get(3), input, context, transaction, tempParameterTable);
		ParameterVariableTerm parameter = new ParameterVariableTerm(type);
		if (parameterIdentifiers != null && parameterRef instanceof IdentifierParameterRef)
			parameterIdentifiers.put(parameter, ((IdentifierParameterRef) parameterRef).getIdentifier());
		ParameterVariableTerm oldpar = tempParameterTable.put(parameterRef, parameter);
		try
		{
			Term body = getProcessor().processTerm((NonTerminalToken) token.getChildren().get(5), input, context, transaction, tempParameterTable,
					parameterIdentifiers);
			try
			{
				return new FunctionTerm(parameter, body);
			}
			catch (NullParameterTypeException e)
			{
				throw new TermParserException(e, token.getStartLocation(), token.getStopLocation(), input);
			}
		}
		finally
		{
			if (oldpar != null)
				tempParameterTable.put(parameterRef, oldpar);
			else
				tempParameterTable.remove(parameterRef);
		}
	}

}
