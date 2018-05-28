package aletheia.parser.term.semantic;

import aletheia.parser.term.TermParser.ConstantProductionTokenPayloadReducer;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;

@AssociatedProduction(left = "AN", right =
{ "asterisk" })
public class AN__asterisk_TokenReducer extends ConstantProductionTokenPayloadReducer<Integer>
{

	public AN__asterisk_TokenReducer()
	{
		super(1);
	}

}