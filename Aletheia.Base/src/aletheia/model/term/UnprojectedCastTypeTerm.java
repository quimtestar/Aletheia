package aletheia.model.term;

public class UnprojectedCastTypeTerm extends CastTypeTerm
{
	private static final long serialVersionUID = 8127173784394913752L;
	private final static int hashPrime = 2963951;

	public static class UnprojectedCastTypeException extends CastTypeException
	{
		private static final long serialVersionUID = -5354349922979085668L;

		protected UnprojectedCastTypeException()
		{
			super();
		}

		protected UnprojectedCastTypeException(String message, Throwable cause)
		{
			super(message, cause);
		}

		protected UnprojectedCastTypeException(String message)
		{
			super(message);
		}

		protected UnprojectedCastTypeException(Throwable cause)
		{
			super(cause);
		}

	}

	private static Term computeType(Term term) throws UnprojectedCastTypeException
	{
		Term type = term.getType();
		if (!(type instanceof ProjectionTerm))
			throw new UnprojectedCastTypeException("Type is not a projection");
		return ((ProjectionTerm) type).getFunction();
	}

	public UnprojectedCastTypeTerm(Term term) throws UnprojectedCastTypeException
	{
		super(computeType(term), term);
	}

	@Override
	public int hashCode(int hasher)
	{
		int ret = super.hashCode(hasher *= hashPrime);
		return ret;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof UnprojectedCastTypeTerm))
			return false;
		if (!super.equals(obj))
			return false;
		return true;
	}

	@Override
	protected UnprojectedCastTypeTerm makeCastTypeTerm(Term term) throws UnprojectedCastTypeException
	{
		return new UnprojectedCastTypeTerm(term);
	}

	@Override
	protected String symbolOpen()
	{
		return "{";
	}

	@Override
	protected String symbolClose()
	{
		return "}";
	}

}
