package aletheia.parser.term.semantic;

import aletheia.model.term.Term;
import aletheia.parser.term.TermParser.TrivialProductionTokenPayloadReducer;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;

@AssociatedProduction(left = "S_p", right =
{ "S", "bar", "S_p" })
public class Sp__S_bar_Sp_TokenReducer extends TrivialProductionTokenPayloadReducer<Term>
{

	public Sp__S_bar_Sp_TokenReducer()
	{
		super(2);
	}

}