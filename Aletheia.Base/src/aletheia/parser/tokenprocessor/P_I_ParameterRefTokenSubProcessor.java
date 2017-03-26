package aletheia.parser.tokenprocessor;

import aletheia.parser.TermParserException;
import aletheia.parser.tokenprocessor.parameterRef.IdentifierParameterRef;
import aletheia.parser.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.tokens.NonTerminalToken;

@ProcessorProduction(left = "P", right =
{ "I" })
public class P_I_ParameterRefTokenSubProcessor extends ParameterRefTokenSubProcessor
{

	protected P_I_ParameterRefTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected ParameterRef subProcess(NonTerminalToken token, String input) throws TermParserException
	{
		return new IdentifierParameterRef(getProcessor().processIdentifier((NonTerminalToken) token.getChildren().get(0), input));
	}

}
