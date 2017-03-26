package aletheia.parser.tokenprocessor;

import aletheia.parsergenerator.tokens.NonTerminalToken;

@ProcessorProduction(left = "R_t", right =
{ "ampersand" })
public class Rt_ampersand_ReferenceTokenSubProcessor extends ReferenceTypeTokenSubProcessor
{

	protected Rt_ampersand_ReferenceTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected ReferenceType subProcess(NonTerminalToken token)
	{
		return ReferenceType.TYPE;
	}

}
