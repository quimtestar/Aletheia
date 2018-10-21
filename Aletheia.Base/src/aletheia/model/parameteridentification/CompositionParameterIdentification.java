/*******************************************************************************
 * Copyright (c) 2018 Quim Testar
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
package aletheia.model.parameteridentification;

public class CompositionParameterIdentification extends ParameterIdentification
{
	private final CompositionParameterIdentification head;
	private final ParameterIdentification tail;

	public CompositionParameterIdentification(CompositionParameterIdentification head, ParameterIdentification tail)
	{
		super();
		this.head = head;
		this.tail = tail;
	}

	public CompositionParameterIdentification getHead()
	{
		return head;
	}

	public ParameterIdentification getTail()
	{
		return tail;
	}

	@Override
	public String toString()
	{
		String sHead = head == null ? "" : head.toString();
		boolean parentheses = tail == null || (tail instanceof CompositionParameterIdentification);
		String sTail = tail == null ? "" : tail.toString();
		boolean space = !sHead.isEmpty() && (parentheses || !sTail.isEmpty());
		StringBuilder builder = new StringBuilder();
		builder.append(sHead);
		if (space)
			builder.append(" ");
		if (parentheses)
			builder.append("(");
		builder.append(sTail);
		if (parentheses)
			builder.append(")");
		return builder.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((head == null) ? 0 : head.hashCode());
		result = prime * result + ((tail == null) ? 0 : tail.hashCode());
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
		CompositionParameterIdentification other = (CompositionParameterIdentification) obj;
		if (head == null)
		{
			if (other.head != null)
				return false;
		}
		else if (!head.equals(other.head))
			return false;
		if (tail == null)
		{
			if (other.tail != null)
				return false;
		}
		else if (!tail.equals(other.tail))
			return false;
		return true;
	}

}
