package aletheia.parser.parameteridentification.semantic;

import aletheia.model.term.Term.ParameterIdentification;
import aletheia.parser.parameteridentification.ParameterIdentificationParser.TrivialProductionTokenPayloadReducer;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;

@AssociatedProduction(left = "T", right =
{ "Ts" })
public class T__Ts_TokenReducer extends TrivialProductionTokenPayloadReducer<ParameterIdentification>
{

}