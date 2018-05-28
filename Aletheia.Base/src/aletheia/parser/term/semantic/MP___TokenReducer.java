package aletheia.parser.term.semantic;

import aletheia.parser.term.TermParser.ConstantProductionTokenPayloadReducer;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;

@AssociatedProduction(left = "MP", right = {})
public class MP___TokenReducer extends ConstantProductionTokenPayloadReducer<Integer>
{

	public MP___TokenReducer()
	{
		super(0);
	}

}