package aletheia.parser.term.semantic;

import aletheia.model.term.Term;
import aletheia.parser.term.TermParser.TrivialProductionTokenPayloadReducer;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;

@AssociatedProduction(left = "R", right =
{ "R_t", "S_ts" })
public class R__Rt_Sts_TokenReducer extends TrivialProductionTokenPayloadReducer<Term>
{
	public R__Rt_Sts_TokenReducer()
	{
		super(1);
	}

}