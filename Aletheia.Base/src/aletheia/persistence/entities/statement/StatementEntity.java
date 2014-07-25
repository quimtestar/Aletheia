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
package aletheia.persistence.entities.statement;

import java.util.Set;
import java.util.UUID;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.persistence.entities.Entity;

/**
 * The generic persistence entity for a {@link Statement} object. Classes
 * implementing this interface will be used by the persistence environment to
 * store the data.
 */
public interface StatementEntity extends Entity
{
	/**
	 * Gets the {@link UUID} of the {@link IdentifiableVariableTerm} associated
	 * to this {@link Statement}. The {@link UUID} that identifies this
	 * statement.
	 *
	 * @return The UUID.
	 */
	public UUID getUuid();

	/**
	 * Sets the {@link UUID} of the {@link IdentifiableVariableTerm} associated
	 * to this {@link Statement}. The {@link UUID} that identifies this
	 * statement.
	 *
	 * @param uuid
	 *            The UUID.
	 */
	public void setUuid(UUID uuid);

	/**
	 * Gets the {@link UUID} of the {@link Context} of this {@link Statement}
	 * (the one returned by
	 * {@link Statement#getContext(aletheia.persistence.Transaction)}). In the
	 * case of a {@link RootContext}, this will be null.
	 *
	 * @return The UUID.
	 */
	public UUID getContextUuid();

	/**
	 * Sets the {@link UUID} of the {@link Context} of this {@link Statement}
	 * (the one returned by
	 * {@link Statement#getContext(aletheia.persistence.Transaction)}). In the
	 * case of a {@link RootContext}, this will be null.
	 *
	 * @param contextUuid
	 *            The UUID.
	 */
	public void setContextUuid(UUID contextUuid);

	/**
	 * Gets the {@link IdentifiableVariableTerm} associated to this
	 * {@link Statement}.
	 *
	 * @return The variable.
	 */
	public IdentifiableVariableTerm getVariable();

	/**
	 * Sets the {@link IdentifiableVariableTerm} associated to this
	 * {@link Statement}.
	 *
	 * @param variable
	 *            The variable.
	 */
	public void setVariable(IdentifiableVariableTerm variable);

	/**
	 * Gets the set of the {@link UUID}s of the {@link Statement}s that this
	 * {@link Statement} depends on.
	 *
	 * @return The UUID set.
	 */
	public Set<UUID> getUuidDependencies();

	/**
	 * Gets the proven status of this {@link Statement}.
	 *
	 * @return The proven status.
	 */
	public boolean isProved();

	/**
	 * Sets the proven status of this {@link Statement}.
	 *
	 * @param proved
	 *            The proven status.
	 */
	public void setProved(boolean proved);

	/**
	 * Gets the {@link Identifier} associated to this {@link Statement} in its
	 * {@link Context}.
	 *
	 * @return The identifier.
	 */
	public Identifier getIdentifier();

	/**
	 * Sets the {@link Identifier} associated to this {@link Statement} in its
	 * {@link Context}. skipUuidVariableTerm
	 *
	 * @param identifier
	 *            The identifier.
	 */
	public void setIdentifier(Identifier identifier);

	/**
	 * Initializes the data of this entity that depends on its {@link Context}'s
	 * entity.
	 *
	 * @param contextEntity
	 *            The context's entity.
	 */
	public void initializeContextData(ContextEntity contextEntity);

}
