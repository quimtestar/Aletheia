package aletheia.parser.parameteridentification.semantic;

import aletheia.model.term.Term.ParameterIdentification;
import aletheia.parser.parameteridentification.ParameterIdentificationParser.TrivialProductionTokenPayloadReducer;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;

@AssociatedProduction(left = "T", right =
{ "T_" })
public class T__T__TokenReducer extends TrivialProductionTokenPayloadReducer<ParameterIdentification>
{

}