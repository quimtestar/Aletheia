package aletheia.parser.parameteridentification.semantic;

import aletheia.parser.parameteridentification.ParameterIdentificationParser.ConstantProductionTokenPayloadReducer;
import aletheia.parser.parameteridentification.ParameterWithType;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;

@AssociatedProduction(left = "P", right = {})
public class P___TokenReducer extends ConstantProductionTokenPayloadReducer<ParameterWithType>
{
	public P___TokenReducer()
	{
		super(new ParameterWithType(null, null));
	}
}