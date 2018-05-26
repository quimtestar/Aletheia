package aletheia.parser.term.semantic;

import aletheia.model.term.Term;
import aletheia.parser.term.TermParser.TrivialProductionTokenPayloadReducer;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;

@AssociatedProduction(left = "Q", right =
{ "C" })
public class Q__C_TokenReducer extends TrivialProductionTokenPayloadReducer<Term>
{

}