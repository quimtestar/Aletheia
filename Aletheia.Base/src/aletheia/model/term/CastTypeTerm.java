package aletheia.model.term;

import java.util.Deque;
import java.util.Map;
import java.util.Set;

import aletheia.model.identifier.Identifier;

public abstract class CastTypeTerm extends AtomicTerm
{
	private static final long serialVersionUID = 8780254850341380288L;
	private final static int hashPrime = 2963777;

	private final Term term;

	public static class CastTypeException extends TypeException
	{

		private static final long serialVersionUID = -2663065237021489227L;

		protected CastTypeException()
		{
			super();
		}

		protected CastTypeException(String message, Throwable cause)
		{
			super(message, cause);
		}

		protected CastTypeException(String message)
		{
			super(message);
		}

		protected CastTypeException(Throwable cause)
		{
			super(cause);
		}
	}

	public CastTypeTerm(Term type, Term term)
	{
		super(type);
		this.term = term;
	}

	public Term getTerm()
	{
		return term;
	}

	@Override
	public int hashCode(int hasher)
	{
		int ret = super.hashCode(hasher *= hashPrime);
		ret = ret * hashPrime + term.hashCode(hasher *= hashPrime);
		return ret;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof CastTypeTerm))
			return false;
		if (!super.equals(obj))
			return false;
		CastTypeTerm castTypeTerm = (CastTypeTerm) obj;
		if (!term.equals(castTypeTerm.term))
			return false;
		return true;
	}

	protected abstract CastTypeTerm makeCastTypeTerm(Term term) throws CastTypeException;

	public static class NotCasteableException extends CastTypeException
	{
		private static final long serialVersionUID = 311089198157566761L;

		protected NotCasteableException()
		{
			super("Not casteable");
		}

	}

	public static Term castToType(Term term, Term targetType) throws CastTypeException
	{
		Term type = term.getType();
		if (type.equals(targetType))
			return term;
		else
		{
			if (type instanceof FunctionTerm)
			{
				if (targetType instanceof FunctionTerm)
				{
					ParameterVariableTerm targetParameter = ((FunctionTerm) targetType).getParameter();
					Term targetBody = ((FunctionTerm) targetType).getBody();
					try
					{
						Term parType = ((FunctionTerm) type).getParameter().getType();
						Term castedPar = castToType(targetParameter, parType);
						Term composedPar = term.compose(castedPar);
						Term castedBody = castToType(composedPar, targetBody);
						Term casted = new FunctionTerm(targetParameter, castedBody);
						return casted;
						//return new FunctionTerm(targetParameter,castToType(term.compose(castToType(targetParameter,((FunctionTerm) type).getParameter().getType())),targetBody));
					}
					catch (ComposeTypeException e)
					{
						throw new CastTypeException(e);
					}

				}
				else if (targetType instanceof ProjectionTerm)
					return new ProjectedCastTypeTerm(castToType(term, ((ProjectionTerm) targetType).getFunction()));
				else
					throw new NotCasteableException();
			}
			else if (type instanceof ProjectionTerm)
			{
				if (targetType instanceof FunctionTerm)
				{
					ParameterVariableTerm targetParameter = ((FunctionTerm) targetType).getParameter();
					Term targetBody = ((FunctionTerm) targetType).getBody();
					try
					{
						Term unprojected = new UnprojectedCastTypeTerm(term);
						Term composedPar = unprojected.compose(targetParameter);
						Term castedBody = castToType(composedPar, targetBody);
						Term casted = new FunctionTerm(targetParameter, castedBody);
						return casted;
					}
					catch (ComposeTypeException e)
					{
						throw new CastTypeException(e);
					}
				}
				else if (targetType instanceof ProjectionTerm)
					return new ProjectedCastTypeTerm(castToType(new UnprojectedCastTypeTerm(term), ((ProjectionTerm) targetType).getFunction()));
				else
					throw new NotCasteableException();
			}
			else
				throw new NotCasteableException();
		}

	}

	@Override
	public int size()
	{
		return term.size();
	}

	@Override
	protected void freeVariables(Set<VariableTerm> freeVars, Set<VariableTerm> localVars)
	{
		term.freeVariables(freeVars, localVars);
	}

	@Override
	public boolean isFreeVariable(VariableTerm variable)
	{
		return term.isFreeVariable(variable);
	}

	@Override
	protected Term replace(Deque<Replace> replaces, Set<VariableTerm> exclude) throws ReplaceTypeException
	{
		try
		{
			return makeCastTypeTerm(term.replace(replaces, exclude));
		}
		catch (CastTypeException e)
		{
			throw new ReplaceTypeException(e);
		}
	}

	@Override
	public Term replace(Map<VariableTerm, Term> replaces) throws ReplaceTypeException
	{
		try
		{
			return makeCastTypeTerm(term.replace(replaces));
		}
		catch (CastTypeException e)
		{
			throw new ReplaceTypeException(e);
		}
	}

	protected abstract String symbolOpen();

	protected abstract String symbolClose();

	@Override
	public String toString(Map<? extends VariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator,
			ParameterIdentification parameterIdentification)
	{
		return symbolOpen() + term.toString(variableToIdentifier, parameterNumerator, parameterIdentification) + symbolClose();
	}

}
