package aletheia.parser.term.semantic;

import aletheia.model.term.Term;
import aletheia.parser.term.TermParser.TrivialProductionTokenPayloadReducer;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;

@AssociatedProduction(left = "B", right =
{ "Q" })
public class B__Q_TokenReducer extends TrivialProductionTokenPayloadReducer<Term>
{

}