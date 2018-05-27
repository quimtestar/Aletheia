package aletheia.parser.term.semantic;

import aletheia.model.term.Term;
import aletheia.parser.term.TermParser.TrivialProductionTokenPayloadReducer;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;

@AssociatedProduction(left = "S_p", right =
{ "S_t" })
public class Sp__St_TokenReducer extends TrivialProductionTokenPayloadReducer<Term>
{

}