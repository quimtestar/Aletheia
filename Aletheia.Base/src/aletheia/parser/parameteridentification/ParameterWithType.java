package aletheia.parser.parameteridentification;

import aletheia.model.identifier.Identifier;
import aletheia.model.term.Term.ParameterIdentification;

public class ParameterWithType
{
	private final Identifier parameter;
	private final ParameterIdentification parameterType;

	public ParameterWithType(Identifier parameter, ParameterIdentification parameterType)
	{
		super();
		this.parameter = parameter;
		this.parameterType = parameterType;
	}

	public Identifier getParameter()
	{
		return parameter;
	}

	public ParameterIdentification getParameterType()
	{
		return parameterType;
	}

}