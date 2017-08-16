package aletheia.parser.parameteridentification.tokenprocessor;

import aletheia.parser.AletheiaParserException;
import aletheia.parser.parameteridentification.tokenprocessor.TokenProcessor.ParameterWithTypeList;
import aletheia.parsergenerator.tokens.NonTerminalToken;

@ProcessorProduction(left = "M", right =
{ "P" })
public class M_P_ParameterIdentificationTokenSubProcessor extends ParameterWithTypeListTokenSubProcessor
{

	protected M_P_ParameterIdentificationTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected ParameterWithTypeList subProcess(NonTerminalToken token, String input) throws AletheiaParserException
	{
		ParameterWithTypeList list = new ParameterWithTypeList();
		list.add(getProcessor().processParameterWithType((NonTerminalToken) token.getChildren().get(0), input));
		return list;
	}

}
