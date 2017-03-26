package aletheia.parser.tokenprocessor;

import aletheia.parser.TermParserException;
import aletheia.parser.tokenprocessor.parameterRef.NumberedParameterRef;
import aletheia.parser.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;

@ProcessorProduction(left = "P", right =
{ "atparam" })
public class P_atparam_ParameterRefTokenSubProcessor extends ParameterRefTokenSubProcessor
{

	protected P_atparam_ParameterRefTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected ParameterRef subProcess(NonTerminalToken token, String input) throws TermParserException
	{
		return new NumberedParameterRef(((TaggedTerminalToken) token.getChildren().get(0)).getText());
	}

}
