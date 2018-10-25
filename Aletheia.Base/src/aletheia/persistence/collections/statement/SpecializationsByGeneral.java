package aletheia.persistence.collections.statement;

import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.collections.PersistenceManagerDataStructure;
import aletheia.utilities.collections.CloseableSet;

/**
 * The set of specializations of a given general statement.
 *
 * @see PersistenceManager#specializationsByGeneral(aletheia.persistence.Transaction,
 *      Statement)
 */
public interface SpecializationsByGeneral extends PersistenceManagerDataStructure, CloseableSet<Specialization>
{
	public Statement getGeneral();
}
