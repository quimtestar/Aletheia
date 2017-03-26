package aletheia.parser.tokenprocessor;

import aletheia.parsergenerator.tokens.NonTerminalToken;

@ProcessorProduction(left = "R_t", right =
{ "ampersand", "caret" })
public class Rt_ampersand_caret_ReferenceTokenSubProcessor extends ReferenceTypeTokenSubProcessor
{

	protected Rt_ampersand_caret_ReferenceTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected ReferenceType subProcess(NonTerminalToken token)
	{
		return ReferenceType.INSTANCE;
	}

}
