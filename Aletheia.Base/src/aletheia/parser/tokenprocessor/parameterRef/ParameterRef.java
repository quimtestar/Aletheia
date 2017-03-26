package aletheia.parser.tokenprocessor.parameterRef;

public abstract class ParameterRef
{

	@Override
	public int hashCode()
	{
		return 1;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}

}