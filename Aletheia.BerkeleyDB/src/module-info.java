module aletheia.persistence.berkeleydb
{
	exports aletheia.persistence.berkeleydb.upgrade;
	exports aletheia.persistence.berkeleydb.preferences;
	exports aletheia.persistence.berkeleydb.proxies.net;
	exports aletheia.persistence.berkeleydb.proxies.parameteridentification;
	exports aletheia.persistence.berkeleydb.gui;
	exports aletheia.persistence.berkeleydb.entities.misc;
	exports aletheia.persistence.berkeleydb.proxies.identifier;
	exports aletheia.persistence.berkeleydb.entities.authority;
	exports aletheia.persistence.berkeleydb.lowlevelbackuprestore;
	exports aletheia.persistence.berkeleydb.entities;
	exports aletheia.persistence.berkeleydb.mutations;
	exports aletheia.persistence.berkeleydb.proxies.term;
	exports aletheia.persistence.berkeleydb.entities.peertopeer;
	exports aletheia.persistence.berkeleydb.entities.local;
	exports aletheia.persistence.berkeleydb.collections.authority;
	exports aletheia.persistence.berkeleydb;
	exports aletheia.persistence.berkeleydb.collections.local;
	exports aletheia.persistence.berkeleydb.collections.peertopeer;
	exports aletheia.persistence.berkeleydb.proxies;
	exports aletheia.persistence.berkeleydb.utilities;
	exports aletheia.persistence.berkeleydb.collections.statement;
	exports aletheia.persistence.berkeleydb.proxies.security;
	exports aletheia.persistence.berkeleydb.entities.statement;
	exports aletheia.persistence.berkeleydb.exceptions;
	exports aletheia.persistence.berkeleydb.proxies.peertopeer.deferredmessagecontent;

	requires transitive aletheia.base;
	requires aletheia.log4j;
	requires aletheia.protocol;
	requires aletheia.security;
	requires transitive aletheia.utilities;
	requires aletheia.version;
	requires java.desktop;
	requires java.prefs;
	requires org.apache.logging.log4j;
	requires transitive je;
}