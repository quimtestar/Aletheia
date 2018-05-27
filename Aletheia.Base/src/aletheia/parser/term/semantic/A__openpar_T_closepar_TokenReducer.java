package aletheia.parser.term.semantic;

import aletheia.model.term.Term;
import aletheia.parser.term.TermParser.TrivialProductionTokenPayloadReducer;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;

@AssociatedProduction(left = "A", right =
{ "openpar", "T", "closepar" })
public class A__openpar_T_closepar_TokenReducer extends TrivialProductionTokenPayloadReducer<Term>
{
	public A__openpar_T_closepar_TokenReducer()
	{
		super(1);
	}

}