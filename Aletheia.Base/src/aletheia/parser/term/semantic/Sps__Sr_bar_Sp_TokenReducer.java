package aletheia.parser.term.semantic;

import aletheia.model.term.Term;
import aletheia.parser.term.TermParser.TrivialProductionTokenPayloadReducer;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;

@AssociatedProduction(left = "S_ps", right =
{ "S_r", "bar", "S_p" })
public class Sps__Sr_bar_Sp_TokenReducer extends TrivialProductionTokenPayloadReducer<Term>
{

	public Sps__Sr_bar_Sp_TokenReducer()
	{
		super(2);
	}

}