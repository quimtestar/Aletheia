package aletheia.parser.term.semantic;

import aletheia.model.term.Term;
import aletheia.parser.term.TermParser.TrivialProductionTokenPayloadReducer;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;

@AssociatedProduction(left = "S_ps", right =
{ "uuid", "bar", "S_p" })
public class Sps__uuid_bar_Sp_TokenReducer extends TrivialProductionTokenPayloadReducer<Term>
{

	public Sps__uuid_bar_Sp_TokenReducer()
	{
		super(2);
	}

}