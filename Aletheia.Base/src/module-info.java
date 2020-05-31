module aletheia.base
{
	requires transitive aletheia.security;
	requires transitive aletheia.utilities;
	requires aletheia.log4j;
	requires aletheia.protocol;
	requires transitive aletheia.parsergenerator;
	requires transitive java.desktop;
	requires transitive java.prefs;
	
	exports aletheia.common;
	exports aletheia.model.term;
	exports aletheia.model.term.protocol;
	exports aletheia.model.identifier;
	exports aletheia.model.identifier.protocol;
	exports aletheia.model.parameteridentification;
	exports aletheia.model.parameteridentification.protocol;
	exports aletheia.model.statement;
	exports aletheia.model.statement.protocol;
	exports aletheia.model.nomenclator;
	exports aletheia.model.catalog;
	exports aletheia.model.authority;
	exports aletheia.model.authority.protocol;
	exports aletheia.model.local;
	exports aletheia.model.peertopeer;
	exports aletheia.model.peertopeer.deferredmessagecontent;
	exports aletheia.model.peertopeer.deferredmessagecontent.protocol;
	exports aletheia.model.peertopeer.protocol;
	exports aletheia.model.misc;
	exports aletheia.parser.term;
	exports aletheia.parser.term.parameterRef; //TODO: rename?
	exports aletheia.persistence;
	exports aletheia.persistence.collections.statement;
	exports aletheia.persistence.collections.authority;
	exports aletheia.persistence.collections.local;
	exports aletheia.persistence.collections.peertopeer;
	exports aletheia.persistence.entities.statement;
	exports aletheia.persistence.entities.authority;
	exports aletheia.persistence.entities.local;
	exports aletheia.persistence.entities.misc;
	exports aletheia.persistence.entities.peertopeer;
	exports aletheia.persistence.exceptions;
	exports aletheia.persistence.gui;
	exports aletheia.persistence.preferences;
	exports aletheia.persistence.protocol;
	exports aletheia.preferences;
	
	exports aletheia.parser.term.semantic to aletheia.parsergenerator;
}