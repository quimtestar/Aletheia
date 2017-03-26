package aletheia.parser.tokenprocessor.parameterRef;

import aletheia.model.identifier.Identifier;

public class IdentifierParameterRef extends ParameterRef
{
	private final Identifier identifier;

	public IdentifierParameterRef(Identifier identifier)
	{
		super();
		this.identifier = identifier;
	}

	public Identifier getIdentifier()
	{
		return identifier;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
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
		IdentifierParameterRef other = (IdentifierParameterRef) obj;
		if (identifier == null)
		{
			if (other.identifier != null)
				return false;
		}
		else if (!identifier.equals(other.identifier))
			return false;
		return true;
	}
}