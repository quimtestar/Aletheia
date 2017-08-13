package aletheia.parser.parameteridentification.tokenprocessor;

import aletheia.model.identifier.Identifier;
import aletheia.model.term.FunctionTerm.FunctionParameterIdentification;
import aletheia.model.term.Term.ParameterIdentification;
import aletheia.parser.AletheiaParserException;
import aletheia.parser.parameteridentification.tokenprocessor.TokenProcessor.ParameterWithTypeParameterIdentification;
import aletheia.parsergenerator.tokens.NonTerminalToken;

@ProcessorProduction(left = "F", right =
{ "openfun", "P", "closefun" })
public class F_openfun_P_closefun_FunctionParameterIdentificationTokenSubProcessor extends ParameterIdentificationTokenSubProcessor
{

	protected F_openfun_P_closefun_FunctionParameterIdentificationTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected FunctionParameterIdentification subProcess(NonTerminalToken token, String input) throws AletheiaParserException
	{
		ParameterWithTypeParameterIdentification parameterWithType = (ParameterWithTypeParameterIdentification) getProcessor()
				.processParameterIdentification((NonTerminalToken) token.getChildren().get(1), input);
		Identifier parameter = null;
		ParameterIdentification parameterType = null;
		if (parameterWithType != null)
		{
			parameter = parameterWithType.getParameter();
			parameterType = parameterWithType.getParameterType();
		}
		if (parameter == null && parameterType == null)
			return null;
		else
			return new FunctionParameterIdentification(parameter, parameterType, null);
	}

}
