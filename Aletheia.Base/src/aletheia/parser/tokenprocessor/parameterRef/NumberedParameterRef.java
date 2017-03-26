package aletheia.parser.tokenprocessor.parameterRef;

public class NumberedParameterRef extends ParameterRef
{
	private final String atParam;

	public NumberedParameterRef(String atParam)
	{
		super();
		this.atParam = atParam;
	}

	public String getAtParam()
	{
		return atParam;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((atParam == null) ? 0 : atParam.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NumberedParameterRef other = (NumberedParameterRef) obj;
		if (atParam == null)
		{
			if (other.atParam != null)
				return false;
		}
		else if (!atParam.equals(other.atParam))
			return false;
		return true;
	}

}