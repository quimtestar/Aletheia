package aletheia.parser.term.semantic;

import aletheia.parser.term.TermParser.ConstantProductionTokenPayloadReducer;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;

@AssociatedProduction(left = "M", right = {})
public class M___TokenReducer extends ConstantProductionTokenPayloadReducer<Boolean>
{

	public M___TokenReducer()
	{
		super(false);
	}

}