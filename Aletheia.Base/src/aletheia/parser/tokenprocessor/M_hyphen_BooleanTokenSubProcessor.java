package aletheia.parser.tokenprocessor;

import aletheia.parser.TermParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;

@ProcessorProduction(left = "M", right = "hyphen")
public class M_hyphen_BooleanTokenSubProcessor extends BooleanTokenSubProcessor
{

	protected M_hyphen_BooleanTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected boolean subProcess(NonTerminalToken token, String input) throws TermParserException
	{
		return true;
	}

}
