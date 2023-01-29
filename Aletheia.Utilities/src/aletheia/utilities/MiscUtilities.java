/*******************************************************************************
 * Copyright (c) 2014, 2023 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.UUID;

import aletheia.utilities.collections.CloseableIterable;
import aletheia.utilities.collections.CloseableIterator;

/**
 * Miscellaneous utilities implemented as static methods.
 *
 * @author Quim Testar
 */
public class MiscUtilities
{

	private MiscUtilities()
	{
	}

	/**
	 * Dummy method just to make the class loader to load this class. Necessary
	 * for the Eclipse IDE to use it in the detail formatters.
	 */
	public static void dummy()
	{
	}

	/**
	 * Wrap a text with a number of columns.
	 *
	 * Copied from
	 *
	 * <a href=
	 * "http://progcookbook.blogspot.com/2006/02/text-wrapping-function-for-java.html"
	 * >http://progcookbook.blogspot.com/2006/02/text-wrapping-function-for-java
	 * .html</a>.
	 *
	 *
	 * @param text
	 *            Text to be wrapped.
	 * @param len
	 *            Column width.
	 * @return Wrapped text.
	 */
	public static String wrapText(String text, int len)
	{
		// return empty array for null text
		if (text == null)
			return null;

		// return text if len is zero or less
		// return text if less than length
		if ((len <= 0) || (text.length() <= len))
			return text;

		char[] chars = text.toCharArray();
		StringBuffer lines = new StringBuffer();
		StringBuffer line = new StringBuffer();
		StringBuffer word = new StringBuffer();

		for (int i = 0; i < chars.length; i++)
		{
			word.append(chars[i]);

			if (chars[i] == ' ')
			{
				if ((line.length() + word.length()) > len)
				{
					lines.append(line).append("\n");
					line.delete(0, line.length());
				}

				line.append(word);
				word.delete(0, word.length());
			}
			else if (chars[i] == '\n')
			{
				line.append(word);
				word.delete(0, word.length());
				lines.append(line);
				line.delete(0, line.length());
			}
		}

		// handle any extra chars in current word
		if (word.length() > 0)
		{
			if ((line.length() + word.length()) > len)
			{
				lines.append(line).append("\n");
				line.delete(0, line.length());
			}
			line.append(word);
		}

		// handle extra line
		if (line.length() > 0)
		{
			lines.append(line).append("\n");
		}

		return lines.toString();
	}

	/**
	 * Byte array to hexadecimal string.
	 */
	public static String toHexString(byte[] a)
	{
		StringBuffer buffer = new StringBuffer();
		boolean first = true;
		for (byte b : a)
		{
			if (!first)
				buffer.append(":");
			else
				first = false;
			buffer.append(String.format("%02x", b));
		}
		return buffer.toString();
	}

	public static class ParseHexStringException extends RuntimeException
	{

		private static final long serialVersionUID = 6806512770857680317L;

		public ParseHexStringException()
		{
			super("Parse error");
		}

	}

	/**
	 * Hexadecimal string to byte array. Inverse function of
	 * {@link MiscUtilities#toHexString(byte[])}.
	 */
	public static byte[] parseHexString(String s)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream(s.length() / 3 + 10);
		Scanner scanner = new Scanner(s);
		try
		{
			scanner.useDelimiter(":");
			while (scanner.hasNextInt(16))
				baos.write(scanner.nextInt(16));
			if (scanner.hasNext())
				throw new ParseHexStringException();
			return baos.toByteArray();
		}
		finally
		{
			scanner.close();
		}
	}

	/**
	 * The wrapper class corresponding to a Java primitive class name.
	 */
	public static Class<?> resolvePrimitiveTypeWrapperClass(String type)
	{
		switch (type)
		{
		case "int":
			return Integer.class;
		case "long":
			return Long.class;
		case "double":
			return Double.class;
		case "boolean":
			return Boolean.class;
		case "char":
			return Character.class;
		case "byte":
			return Byte.class;
		case "short":
			return Short.class;
		default:
			return null;
		}
	}

	/**
	 * Java's primitive class by name.
	 */
	public static Class<?> resolvePrimitiveTypeClass(String type)
	{
		switch (type)
		{
		case "int":
			return int.class;
		case "long":
			return long.class;
		case "double":
			return double.class;
		case "boolean":
			return boolean.class;
		case "char":
			return char.class;
		case "byte":
			return byte.class;
		case "short":
			return short.class;
		default:
			return null;
		}
	}

	/**
	 * Primitive class to its wrapper class
	 */
	public static Class<?> resolvePrimitiveTypeWrapperClass(Class<?> primitiveType)
	{
		return resolvePrimitiveTypeWrapperClass(primitiveType.getName());
	}

	/**
	 * Code copied from {@link AbstractCollection#toString()}
	 */
	public static <E> String toString(Iterable<E> iterable)
	{
		Iterator<E> it = iterable.iterator();
		if (!it.hasNext())
			return "[]";

		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (;;)
		{
			E e = it.next();
			sb.append(e == iterable ? "(this Iterable)" : e);
			if (!it.hasNext())
				return sb.append(']').toString();
			sb.append(',').append(' ');
		}
	}

	/**
	 * Code copied from {@link AbstractMap#toString()}
	 */
	public static <K, V> String toString(Map<K, V> map)
	{
		Iterator<Entry<K, V>> i = map.entrySet().iterator();
		if (!i.hasNext())
			return "{}";

		StringBuilder sb = new StringBuilder();
		sb.append('{');
		for (;;)
		{
			Entry<K, V> e = i.next();
			K key = e.getKey();
			V value = e.getValue();
			sb.append(key == map ? "(this Map)" : key);
			sb.append('=');
			sb.append(value == map ? "(this Map)" : value);
			if (!i.hasNext())
				return sb.append('}').toString();
			sb.append(',').append(' ');
		}
	}

	/**
	 * The value of a particular bit of a long integer as a boolean. Bit zero
	 * being the least significant.
	 */
	private static boolean bitAt(long x, int i)
	{
		return ((x >> (Long.SIZE - i - 1)) & 0x01l) != 0;
	}

	/**
	 * The value of a particular bit of a {@link UUID} as a boolean. Bit zero
	 * being the least significant.
	 */
	public static boolean bitAt(UUID uuid, int i)
	{
		if ((i < 0) || (i > uuidBitLength))
			throw new IndexOutOfBoundsException();
		if (i < Long.SIZE)
			return bitAt(uuid.getMostSignificantBits(), i);
		else
			return bitAt(uuid.getLeastSignificantBits(), i - Long.SIZE);
	}

	/**
	 * First bit position that differs between two {@link UUID}s. Bit zero being
	 * the least significant.
	 */
	public static int firstDifferentBit(UUID uuid1, UUID uuid2)
	{
		if (uuid1.equals(uuid2))
			throw new IllegalArgumentException();
		for (int i = 0;; i++)
			if (bitAt(uuid1, i) != bitAt(uuid2, i))
				return i;
	}

	/**
	 * Returns the index of the closest {@link UUID} from a list to a given one.
	 *
	 * The distance is defined to be the absolute difference of the two
	 * {@link UUID}s interpreted as a single 128-bit number each.
	 *
	 * @param uuid
	 *            The given {@link UUID}.
	 * @param list
	 *            The list.
	 */
	public static int closestUUIDIndex(UUID uuid, List<UUID> list)
	{
		class Distance implements Comparable<Distance>
		{
			private final long mostSigBits;
			private final long leastSigBits;

			private Distance(UUID uuid1, UUID uuid2)
			{
				long l = uuid1.getLeastSignificantBits() - uuid2.getLeastSignificantBits();
				long m = uuid1.getMostSignificantBits() - uuid2.getMostSignificantBits()
						- (uuid2.getLeastSignificantBits() > uuid1.getLeastSignificantBits() ? 1 : 0);
				if (m < 0)
				{
					m = ~m;
					l = (~l) + 1;
					if (l == 0)
						m++;
				}
				mostSigBits = m;
				leastSigBits = l;
			}

			@Override
			public int compareTo(Distance o)
			{
				int c = 0;
				c = Long.compare(mostSigBits, o.mostSigBits);
				if (c != 0)
					return c;
				c = Long.compare(leastSigBits, o.leastSigBits);
				if (c != 0)
					return c;
				return c;
			}
		}

		Distance minDistance = null;
		int minIndex = -1;
		int i = 0;
		for (UUID uuid_ : list)
		{
			Distance d = new Distance(uuid, uuid_);
			if (minDistance == null || d.compareTo(minDistance) < 0)
			{
				minDistance = d;
				minIndex = i;
			}
			i++;
		}
		return minIndex;
	}

	/**
	 * The bit length of an UUID. That is, 128.
	 */
	public static int uuidBitLength = 2 * Long.SIZE;

	/**
	 * Human-readable string representation of a size of digital information in
	 * bytes.
	 */
	public static String byteSizeToString(int size)
	{
		NumberFormat nf = new DecimalFormat("###0.##");
		String[] units = new String[]
		{ "B", "KiB", "MiB", "GiB" };
		float value = size;
		int m = (int) (Math.log(value) / Math.log(1 << 10));
		if (m >= units.length)
			m = units.length - 1;
		value /= 1 << (10 * m);
		return nf.format(value) + " " + units[m];
	}

	/**
	 * Nth element from the actual position (the next is the zeroth) of an
	 * iterator or null it there is no nth element.
	 */
	private static <E> E nthFromIterator(Iterator<E> iterator, int n)
	{
		for (int i = 0; i < n && iterator.hasNext(); i++)
			iterator.next();
		if (iterator.hasNext())
			return iterator.next();
		else
			return null;
	}

	/**
	 * Next element of an iterator or null it there is no next element.
	 */
	private static <E> E nextFromIterator(Iterator<E> iterator)
	{
		return nthFromIterator(iterator, 0);
	}

	/**
	 * Nth element of an {@link Iterable} object (the first is the zeroth) of
	 * null if there are not so many elements.
	 */
	public static <E> E nthFromIterable(Iterable<E> iterable, int n)
	{
		Iterator<E> iterator = iterable.iterator();
		return nthFromIterator(iterator, n);
	}

	/**
	 * Nth element of a {@link CloseableIterable} object (the first is the
	 * zeroth) of null if there are not so many elements.
	 */
	public static <E> E nthFromCloseableIterable(CloseableIterable<E> iterable, int n)
	{
		CloseableIterator<E> iterator = iterable.iterator();
		try
		{
			return nthFromIterator(iterator, n);
		}
		finally
		{
			iterator.close();
		}
	}

	/**
	 * First element of an {@link Iterable} object.
	 */
	public static <E> E firstFromIterable(Iterable<E> iterable)
	{
		Iterator<E> iterator = iterable.iterator();
		return nextFromIterator(iterator);
	}

	/**
	 * First element of a {@link CloseableIterable} objects. Closes the
	 * generated iterator.
	 */
	public static <E> E firstFromCloseableIterable(CloseableIterable<E> iterable)
	{
		CloseableIterator<E> iterator = iterable.iterator();
		try
		{
			return nextFromIterator(iterator);
		}
		finally
		{
			iterator.close();
		}
	}

	public static <E> E lastFromList(List<E> list)
	{
		ListIterator<E> iterator = list.listIterator(list.size());
		if (iterator.hasPrevious())
			return iterator.previous();
		else
			return null;
	}

	/**
	 * Appends elements to an array.
	 */
	public static Object[] arrayAppend(Object... elements)
	{
		return arrayAppend(new Object[0], elements);
	}

	/**
	 * Appends elements to an array.
	 */
	@SafeVarargs
	public static <T> T[] arrayAppend(T[] array, T... elements)
	{
		Array.newInstance(array.getClass().getComponentType(), array.length + elements.length);
		T[] result = Arrays.copyOf(array, array.length + elements.length);
		for (int i = 0; i < elements.length; i++)
			result[array.length + i] = elements[i];
		return result;
	}

	/**
	 * Returns a constructor of a class that matches the given arguments.
	 */
	@SuppressWarnings("unchecked")
	public static <C> Constructor<C> matchingConstructor(Class<C> clazz, Object... initargs)
	{
		for (Constructor<?> constructor : clazz.getDeclaredConstructors())
		{
			if (constructor.getParameterTypes().length == initargs.length)
			{
				boolean match = true;
				for (int i = 0; i < initargs.length; i++)
				{
					Class<?> parameterClass = constructor.getParameterTypes()[i];
					if (parameterClass.isPrimitive())
					{
						Class<?> primitiveTypeWrapperClass = MiscUtilities.resolvePrimitiveTypeWrapperClass(parameterClass);
						if (!primitiveTypeWrapperClass.isInstance(initargs[i]))
						{
							match = false;
							break;
						}
					}
					else
					{
						if (initargs[i] != null && !parameterClass.isInstance(initargs[i]))
						{
							match = false;
							break;
						}
					}
				}
				if (match)
					return (Constructor<C>) constructor;
			}
		}
		return null;
	}

	public static class NoConstructorException extends Exception
	{

		private static final long serialVersionUID = 8394614511945086194L;

	}

	/**
	 * Constructs an object of a given class using a constructor that matches
	 * the given arguments.
	 *
	 * @see #matchingConstructor(Class, Object...)
	 */
	public static <C> C construct(Class<C> clazz, Object... initArgs)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoConstructorException
	{
		Constructor<C> constructor = matchingConstructor(clazz, initArgs);
		if (constructor == null)
			throw new NoConstructorException();
		constructor.setAccessible(true);
		return constructor.newInstance(initArgs);
	}

	/**
	 * The one's complement of the bit representation of a {@link UUID} as a
	 * {@link UUID}
	 */
	public static UUID complementUuid(UUID uuid)
	{
		return new UUID(~uuid.getMostSignificantBits(), ~uuid.getLeastSignificantBits());
	}

	/**
	 * List of stack trace elements to the precise call of this method.
	 *
	 * Warning: Expensive method. Use only in debug mode.
	 */
	public static List<StackTraceElement> stackTraceList(int depth)
	{
		List<StackTraceElement> list = Arrays.asList(Thread.currentThread().getStackTrace());
		return list.subList(depth + 2, list.size());
	}

	/**
	 * A particular element of the {@link #stackTraceList(int)}.
	 *
	 * Warning: Expensive method. Use only in debug mode.
	 */
	public static StackTraceElement stackTraceElement(int depth)
	{
		return stackTraceList(depth + 1).get(0);
	}

	/**
	 * Puts the elements of returned by an iterator object into an array until
	 * there's no more elements left.
	 *
	 * @param <E>
	 *            The type of the array to generate.
	 * @param a
	 *            The array to fill if there's enough space on it.
	 */
	public static <E> E[] iteratorToArray(Iterator<?> iterator, E[] a)
	{
		@SuppressWarnings("unchecked")
		Class<? extends E> componentType = (Class<? extends E>) a.getClass().getComponentType();
		boolean rellocated = false;
		int i = 0;
		while (iterator.hasNext())
		{
			Object o = iterator.next();
			if (i >= a.length)
			{
				int length = a.length + (a.length >> 1);
				if (length < 10)
					length = 10;
				if (length < 0 || length > Integer.MAX_VALUE - 8)
					length = Integer.MAX_VALUE - 8;
				if (i >= length)
					throw new OutOfMemoryError();
				a = Arrays.copyOf(a, length);
				rellocated = true;
			}
			E e = componentType.cast(o);
			a[i] = e;
			i++;
		}
		if (rellocated)
		{
			if (i < a.length)
				a = Arrays.copyOf(a, i);
		}
		else
			while (i < a.length)
				a[i++] = null;
		return a;
	}

	/**
	 * Puts the elements of an iterable object into an array.
	 *
	 * @param <E>
	 *            The type of the array to generate.
	 * @param a
	 *            The array to fill if there's enough space on it.
	 *
	 * @see Collection#toArray(Object[])
	 */
	public static <E> E[] iterableToArray(Iterable<?> iterable, E[] a)
	{
		synchronized (iterable)
		{
			return iteratorToArray(iterable.iterator(), a);
		}
	}

	/**
	 * Puts the elements of an {@link Iterable} object into an array.
	 *
	 * @see MiscUtilities#iterableToArray(Iterable, Object[])
	 * @see Collection#toArray(Object[])
	 *
	 */
	public static Object[] iterableToArray(Iterable<?> iterable)
	{
		return iterableToArray(iterable, new Object[0]);
	}

	/**
	 * Iterate across an iterator counting its elements.
	 */
	public static int countIterator(Iterator<?> iterator)
	{
		int count = 0;
		while (iterator.hasNext())
		{
			iterator.next();
			count++;
		}
		return count;
	}

	/**
	 * Iterable's element count.
	 */
	public static int countIterable(Iterable<?> iterable)
	{
		return countIterator(iterable.iterator());
	}

	/**
	 * The remote {@link InetAddress} associated to a {@link SocketChannel}.
	 *
	 * @see SocketChannel#getRemoteAddress()
	 * @see InetSocketAddress#getAddress()
	 */
	public static InetAddress socketChannelRemoteInetAddress(SocketChannel socketChannel) throws IOException
	{
		return ((InetSocketAddress) socketChannel.getRemoteAddress()).getAddress();
	}

	/**
	 * Detail formatter just for eclipse debugging. Just the default collection
	 * toString method with a time limit of two seconds.
	 */
	public static String collectionDetailFormatter(Collection<?> collection)
	{
		Iterator<?> it = collection.iterator();
		if (!it.hasNext())
			return "[]";

		StringBuilder sb = new StringBuilder();
		sb.append('[');
		long t0 = System.currentTimeMillis();
		for (;;)
		{
			if (System.currentTimeMillis() - t0 >= 2000)
				return sb.append("...").toString();
			Object e = it.next();
			sb.append(e == collection ? "(this Collection)" : e);
			if (!it.hasNext())
				return sb.append(']').toString();
			sb.append(',').append(' ');
		}
	}

	/**
	 * Detail formatter just for eclipse debugging. Just the default map
	 * toString method with a time limit of two seconds.
	 */
	public static String mapDetailFormatter(Map<?, ?> map)
	{
		Iterator<? extends Entry<?, ?>> i = map.entrySet().iterator();
		if (!i.hasNext())
			return "{}";

		StringBuilder sb = new StringBuilder();
		sb.append('{');
		long t0 = System.currentTimeMillis();
		for (;;)
		{
			if (System.currentTimeMillis() - t0 >= 2000)
				return sb.append("...").toString();
			Entry<?, ?> e = i.next();
			Object key = e.getKey();
			Object value = e.getValue();
			sb.append(key == map ? "(this Map)" : key);
			sb.append('=');
			sb.append(value == map ? "(this Map)" : value);
			if (!i.hasNext())
				return sb.append('}').toString();
			sb.append(',').append(' ');
		}

	}

	public static String commonPrefix(String string1, String string2)
	{
		int minLength = Math.min(string1.length(), string2.length());
		for (int i = 0; i < minLength; i++)
			if (string1.charAt(i) != string2.charAt(i))
				return string1.substring(0, i);
		return string1.substring(0, minLength);
	}

	public static String commonPrefix(Iterable<String> strings)
	{
		String prefix = null;
		for (String s : strings)
			if (prefix == null)
				prefix = s;
			else
				prefix = commonPrefix(prefix, s);
		return prefix;
	}

}
