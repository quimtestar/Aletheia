package aletheia.parser.term.semantic;

import aletheia.parser.term.TermParser.ConstantProductionTokenPayloadReducer;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;

@AssociatedProduction(left = "M", right =
{ "hyphen" })
public class M__hyphen_TokenReducer extends ConstantProductionTokenPayloadReducer<Boolean>
{

	public M__hyphen_TokenReducer()
	{
		super(true);
	}

}