/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
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
package aletheia.persistence.berkeleydb.utilities;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import aletheia.utilities.NaturalComparator;

import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;

public class BerkeleyDBKeyComparator<K> implements Comparator<K>
{
	private final Comparator<K> innerComparator;

	public BerkeleyDBKeyComparator(Class<? extends K> keyClass)
	{
		if (keyClass.equals(String.class))
		{
			innerComparator = new Comparator<>()
			{
				private final Charset utfCharSet = Charset.forName("UTF-8");

				@Override
				public int compare(K k1, K k2)
				{
					if (k1 == k2)
						return 0;
					String s1 = (String) k1;
					String s2 = (String) k2;
					byte[] b1 = s1.getBytes(utfCharSet);
					byte[] b2 = s2.getBytes(utfCharSet);
					int i1 = 0;
					int i2 = 0;
					boolean e1 = false;
					boolean e2 = false;
					while (true)
					{
						if ((i1 >= b1.length) || (i2 >= b1.length))
						{
							if (i1 < b1.length)
								return +1;
							else if (i2 < b2.length)
								return -1;
							else
								return 0;
						}
						byte c1;
						if (e1)
						{
							c1 = (byte) 0x80;
							e1 = false;
						}
						else if (b1[i1] == 0)
						{
							c1 = (byte) 0xc0;
							e1 = true;
						}
						else
							c1 = b1[i1];
						byte c2;
						if (e2)
						{
							c2 = (byte) 0x80;
							e2 = false;
						}
						else if (b2[i2] == 0)
						{
							c2 = (byte) 0xc0;
							e2 = true;
						}
						else
							c2 = b2[i2];
						int c = Byte.compare((byte) (c1 ^ 0x80), (byte) (c2 ^ 0x80));
						if (c != 0)
							return c;
						if (!e1)
							i1++;
						if (!e2)
							i2++;
					}
				}
			};
		}
		else if (keyClass.isPrimitive() || Comparable.class.isAssignableFrom(keyClass))
			innerComparator = new NaturalComparator<>();
		else if (keyClass.isAnnotationPresent(Persistent.class))
		{
			final Vector<Field> fields = new Vector<>();
			final Map<Class<?>, BerkeleyDBKeyComparator<Object>> comparatorMap = new HashMap<>();
			for (Field field : keyClass.getDeclaredFields())
			{
				KeyField keyField = field.getAnnotation(KeyField.class);
				if (keyField != null)
				{
					field.setAccessible(true);
					int i = keyField.value();
					if (i >= fields.size())
						fields.setSize(i + 1);
					fields.set(i, field);
					if (!comparatorMap.containsKey(field.getType()))
						comparatorMap.put(field.getType(), new BerkeleyDBKeyComparator<>(field.getType()));
				}
			}
			innerComparator = new Comparator<>()
			{
				@Override
				public int compare(K k1, K k2)
				{
					if (k1 == k2)
						return 0;
					try
					{
						for (Field f : fields)
						{
							if (f != null)
							{
								Object kf1 = f.get(k1);
								Object kf2 = f.get(k2);
								int c = comparatorMap.get(f.getType()).compare(kf1, kf2);
								if (c != 0)
									return c;
							}
						}
						return 0;
					}
					catch (IllegalArgumentException | IllegalAccessException e)
					{
						throw new RuntimeException(e);
					}
					finally
					{
					}
				}
			};

		}
		else
			throw new IllegalArgumentException();
	}

	@Override
	public int compare(K k1, K k2)
	{
		return innerComparator.compare(k1, k2);
	}

}
