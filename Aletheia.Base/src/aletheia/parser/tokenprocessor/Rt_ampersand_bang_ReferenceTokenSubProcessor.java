package aletheia.parser.tokenprocessor;

import aletheia.parsergenerator.tokens.NonTerminalToken;

@ProcessorProduction(left = "R_t", right =
{ "ampersand", "bang" })
public class Rt_ampersand_bang_ReferenceTokenSubProcessor extends ReferenceTypeTokenSubProcessor
{

	protected Rt_ampersand_bang_ReferenceTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected ReferenceType subProcess(NonTerminalToken token)
	{
		return ReferenceType.VALUE;
	}

}
