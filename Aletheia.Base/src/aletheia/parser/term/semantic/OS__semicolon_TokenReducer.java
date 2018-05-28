package aletheia.parser.term.semantic;

import aletheia.parser.term.TermParser.NullProductionTokenPayloadReducer;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;

@AssociatedProduction(left = "OS", right =
{ "semicolon" })
public class OS__semicolon_TokenReducer extends NullProductionTokenPayloadReducer
{

}