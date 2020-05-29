module aletheia.persistence.berkeleydb
{
	exports aletheia.persistence.berkeleydb;
	exports aletheia.persistence.berkeleydb.entities;
	exports aletheia.persistence.berkeleydb.entities.statement;
	exports aletheia.persistence.berkeleydb.entities.authority;
	exports aletheia.persistence.berkeleydb.entities.local;
	exports aletheia.persistence.berkeleydb.entities.peertopeer;
	exports aletheia.persistence.berkeleydb.entities.misc;
	exports aletheia.persistence.berkeleydb.collections.statement;
	exports aletheia.persistence.berkeleydb.collections.authority;
	exports aletheia.persistence.berkeleydb.gui;
	exports aletheia.persistence.berkeleydb.preferences;
	exports aletheia.persistence.berkeleydb.exceptions;
	
	requires transitive aletheia.utilities;
	requires transitive aletheia.base;
	requires aletheia.log4j;
	requires aletheia.protocol;
	requires aletheia.version;
	requires transitive je;  //TODO is this necessary?
	requires jdk.unsupported;
	requires java.transaction.xa;
	requires java.desktop;
	
	opens aletheia.persistence.berkeleydb.entities to je;
	opens aletheia.persistence.berkeleydb.entities.authority to je;
	opens aletheia.persistence.berkeleydb.entities.statement to je;
	opens aletheia.persistence.berkeleydb.entities.local to je;
	opens aletheia.persistence.berkeleydb.entities.peertopeer to je;
	opens aletheia.persistence.berkeleydb.entities.misc to je;
	opens aletheia.persistence.berkeleydb.proxies to je;
	opens aletheia.persistence.berkeleydb.proxies.term to je;
	opens aletheia.persistence.berkeleydb.proxies.parameteridentification to je;
	opens aletheia.persistence.berkeleydb.proxies.security to je;
	opens aletheia.persistence.berkeleydb.proxies.identifier to je;
	opens aletheia.persistence.berkeleydb.proxies.net to je;
	opens aletheia.persistence.berkeleydb.proxies.peertopeer.deferredmessagecontent to je;
	
	
}