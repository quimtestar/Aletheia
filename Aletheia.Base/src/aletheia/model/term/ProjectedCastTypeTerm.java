package aletheia.model.term;

import aletheia.model.term.ProjectionTerm.ProjectionTypeException;

public class ProjectedCastTypeTerm extends CastTypeTerm
{
	private static final long serialVersionUID = -36509166031613827L;
	private final static int hashPrime = 2963923;

	public static class ProjectedCastTypeException extends CastTypeException
	{

		private static final long serialVersionUID = -5957505545584755810L;

		protected ProjectedCastTypeException()
		{
			super();
		}

		protected ProjectedCastTypeException(String message, Throwable cause)
		{
			super(message, cause);
		}

		protected ProjectedCastTypeException(String message)
		{
			super(message);
		}

		protected ProjectedCastTypeException(Throwable cause)
		{
			super(cause);
		}

	}

	private static Term computeType(Term term) throws ProjectedCastTypeException
	{
		Term type = term.getType();
		if (!(type instanceof FunctionTerm))
			throw new ProjectedCastTypeException("Type is not a function");
		try
		{
			return new ProjectionTerm((FunctionTerm) type);
		}
		catch (ProjectionTypeException e)
		{
			throw new ProjectedCastTypeException(e);
		}
	}

	public ProjectedCastTypeTerm(Term term) throws ProjectedCastTypeException
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
		if (!(obj instanceof ProjectedCastTypeTerm))
			return false;
		if (!super.equals(obj))
			return false;
		return true;
	}

	@Override
	protected ProjectedCastTypeTerm makeCastTypeTerm(Term term) throws ProjectedCastTypeException
	{
		return new ProjectedCastTypeTerm(term);
	}

	@Override
	protected String symbolOpen()
	{
		return "[";
	}

	@Override
	protected String symbolClose()
	{
		return "]";
	}

}
