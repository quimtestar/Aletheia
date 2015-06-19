package aletheia.utilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import aletheia.utilities.MiscUtilities.NoConstructorException;

public class ObjectConstructor<C>
{
	private final Constructor<C> constructor;
	private final Object[] args;

	public ObjectConstructor(Class<C> clazz, Object... args) throws NoConstructorException
	{
		this.constructor = MiscUtilities.matchingConstructor(clazz, args);
		if (this.constructor == null)
			throw new NoConstructorException();
		this.constructor.setAccessible(true);
		this.args = args;
	}

	public C construct() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		return constructor.newInstance(args);
	}

}
