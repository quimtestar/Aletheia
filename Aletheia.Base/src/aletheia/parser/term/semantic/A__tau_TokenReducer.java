package aletheia.parser.term.semantic;

import aletheia.model.term.TauTerm;
import aletheia.model.term.Term;
import aletheia.parser.term.TermParser.ConstantProductionTokenPayloadReducer;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;

@AssociatedProduction(left = "A", right =
{ "tau" })
public class A__tau_TokenReducer extends ConstantProductionTokenPayloadReducer<Term>
{

	public A__tau_TokenReducer()
	{
		super(TauTerm.instance);
	}

}