package aletheia.parser.parameteridentification.tokenprocessor;

import aletheia.model.term.FunctionTerm.FunctionParameterIdentification;
import aletheia.parser.AletheiaParserException;
import aletheia.parser.parameteridentification.tokenprocessor.TokenProcessor.ParameterWithTypeList;
import aletheia.parsergenerator.tokens.NonTerminalToken;

@ProcessorProduction(left = "F", right =
{ "openfun", "M", "closefun" })
public class F_openfun_M_closefun_FunctionParameterIdentificationTokenSubProcessor extends FunctionParameterIdentificationTokenSubProcessor
{

	protected F_openfun_M_closefun_FunctionParameterIdentificationTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected FunctionParameterIdentification subProcess(NonTerminalToken token, String input) throws AletheiaParserException
	{
		ParameterWithTypeList parameterWithTypeList = getProcessor().processParameterWithTypeList((NonTerminalToken) token.getChildren().get(1), input);
		return subProcess(parameterWithTypeList, null);
	}

}
