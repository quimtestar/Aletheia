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
package aletheia.persistence.berkeleydb.entities.authority;

import aletheia.persistence.entities.authority.PrivatePersonEntity;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Persistent(version = 0)
public class BerkeleyDBPrivatePersonEntity extends BerkeleyDBPersonEntity implements PrivatePersonEntity
{
	public static final String mark_FieldName = "mark";
	@SecondaryKey(name = mark_FieldName, relate = Relationship.MANY_TO_ONE)
	private boolean mark;

	public static final String nick__FieldName = "nick_";
	@SecondaryKey(name = nick__FieldName, relate = Relationship.ONE_TO_ONE)
	private String nick_;

	public BerkeleyDBPrivatePersonEntity()
	{
		super();
		this.mark = true;
	}

	@Override
	public void setNick(String nick)
	{
		super.setNick(nick);
		this.nick_ = nick;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (mark ? 1231 : 1237);
		result = prime * result + ((nick_ == null) ? 0 : nick_.hashCode());
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
		BerkeleyDBPrivatePersonEntity other = (BerkeleyDBPrivatePersonEntity) obj;
		if (mark != other.mark)
			return false;
		if (nick_ == null)
		{
			if (other.nick_ != null)
				return false;
		}
		else if (!nick_.equals(other.nick_))
			return false;
		return true;
	}

}
