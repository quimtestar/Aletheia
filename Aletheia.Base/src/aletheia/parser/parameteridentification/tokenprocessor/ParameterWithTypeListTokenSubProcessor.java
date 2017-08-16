package aletheia.parser.parameteridentification.tokenprocessor;

import aletheia.parser.AletheiaParserException;
import aletheia.parser.parameteridentification.tokenprocessor.TokenProcessor.ParameterWithTypeList;
import aletheia.parsergenerator.tokens.NonTerminalToken;

public abstract class ParameterWithTypeListTokenSubProcessor extends TokenSubProcessor<ParameterWithTypeList, Void>
{

	protected ParameterWithTypeListTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected ParameterWithTypeList subProcess(NonTerminalToken token, String input, Void parameter) throws AletheiaParserException
	{
		return subProcess(token, input);
	}

	protected abstract ParameterWithTypeList subProcess(NonTerminalToken token, String input) throws AletheiaParserException;

}
