/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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

import java.util.UUID;

import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.model.term.Term;

/**
 * The persistence entity for a {@link Specialization} statement.
 */
public interface SpecializationEntity extends StatementEntity
{

	/**
	 * Gets the UUID of the general {@link Statement} of this
	 * {@link Specialization}.
	 *
	 * @return The UUID.
	 */
	public UUID getGeneralUuid();

	/**
	 * Sets the UUID of the general {@link Statement} of this
	 * {@link Specialization}.
	 *
	 * @param uuidGeneral
	 *            The UUID.
	 */
	public void setGeneralUuid(UUID uuidGeneral);

	/**
	 * Gets the instance {@link Term} of this {@link Specialization}.
	 *
	 * @return The instance.
	 */
	public Term getInstance();

	/**
	 * Sets the instance {@link Term} of this {@link Specialization}.
	 *
	 * @param instance
	 *            The instance.
	 */
	public void setInstance(Term instance);

	/**
	 * Gets the {@link ParameterIdentification} associated to the instance term
	 */
	public ParameterIdentification getInstanceParameterIdentification();

	/**
	 * Sets the {@link ParameterIdentification} associated to the instance term
	 */
	public void setInstanceParameterIdentification(ParameterIdentification instanceParameterIdentification);

	/**
	 * Gets the UUID of the instance proof {@link Statement} of this
	 * {@link Specialization}.
	 *
	 * @return The UUID.
	 */
	public UUID getInstanceProofUuid();

	/**
	 * Sets the UUID of the instance proof {@link Statement} of this
	 * {@link Specialization}.
	 *
	 * @param uuidInstanceProof
	 *            The UUID.
	 */
	public void setInstanceProofUuid(UUID uuidInstanceProof);

}
