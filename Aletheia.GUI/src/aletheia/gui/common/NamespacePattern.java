/*******************************************************************************
 * Copyright (c) 2016, 2020 Quim Testar.
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
package aletheia.gui.common;

import java.util.regex.Pattern;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.identifier.RootNamespace;

public abstract class NamespacePattern
{

	public static NamespacePattern instantiate(String expression)
	{
		try
		{
			return new ExpressionNamespacePattern(expression);
		}
		catch (InvalidNameException e)
		{
			return new VoidNamespacePattern();
		}
	}

	public abstract Identifier fromKey();

	public abstract boolean isPrefix(Identifier identifier);

	public abstract boolean matches(Namespace namespace);

	private static class VoidNamespacePattern extends NamespacePattern
	{

		private VoidNamespacePattern()
		{

		}

		@Override
		public Identifier fromKey()
		{
			return RootNamespace.instance.terminator();
		}

		@Override
		public boolean isPrefix(Identifier identifier)
		{
			return false;
		}

		@Override
		public boolean matches(Namespace namespace)
		{
			return false;
		}

	}

	private static class ExpressionNamespacePattern extends NamespacePattern
	{
		private final Namespace prefix;
		private final boolean dottedPrefix;
		private final Pattern pattern;

		private ExpressionNamespacePattern(String expression) throws InvalidNameException
		{
			int p = expression.length();

			int iq = expression.indexOf('?');
			if ((iq >= 0) && (iq < p))
				p = iq;

			int ia = expression.indexOf('*');
			if ((ia >= 0) && (ia < p))
				p = ia;

			int ih = expression.indexOf('#');
			if ((ih >= 0) && (ih < p))
				p = ih;
			String sPrefix = expression.substring(0, p);
			this.prefix = Namespace.parse(sPrefix);
			this.dottedPrefix = sPrefix.endsWith(".");

			StringBuilder regex = new StringBuilder();
			int i = 0;
			for (String s : expression.split("[\\*\\?#]"))
			{
				regex.append(Pattern.quote(s));
				i += s.length();
				loop: while (i < expression.length())
				{
					switch (expression.charAt(i))
					{
					case '?':
						regex.append(".");
						break;
					case '*':
						regex.append(".*");
						break;
					case '#':
						regex.append("[0-9]");
						break;
					default:
						break loop;
					}
					i++;
				}
			}
			loop: while (i < expression.length())
			{
				switch (expression.charAt(i))
				{
				case '?':
					regex.append(".");
					break;
				case '*':
					regex.append(".*");
					break;
				case '#':
					regex.append("[0-9]");
					break;
				default:
					break loop;
				}
				i++;
			}
			this.pattern = Pattern.compile(regex.toString());
		}

		@Override
		public Identifier fromKey()
		{
			if (prefix instanceof NodeNamespace)
				return ((NodeNamespace) prefix).asIdentifier();
			else if (prefix instanceof RootNamespace)
				return ((RootNamespace) prefix).initiator();
			else
				throw new Error();
		}

		@Override
		public boolean isPrefix(Identifier identifier)
		{
			if (dottedPrefix || prefix instanceof RootNamespace)
				return prefix.isPrefixOf(identifier);
			else if (prefix instanceof NodeNamespace)
			{
				NodeNamespace nPrefix = (NodeNamespace) prefix;
				if (!nPrefix.getParent().isPrefixOf(identifier))
					return false;
				Namespace suffix = identifier.makeSuffix(nPrefix.getParent());
				if (suffix instanceof NodeNamespace)
					if (!((NodeNamespace) suffix).headName().startsWith(nPrefix.getName()))
						return false;
				return true;
			}
			else
				throw new Error();
		}

		@Override
		public boolean matches(Namespace namespace)
		{
			return pattern.matcher(namespace.qualifiedName()).matches();
		}

	}

}
