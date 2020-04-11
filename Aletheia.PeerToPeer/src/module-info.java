module aletheia.peertopeer
{
	exports aletheia.peertopeer.spliced.message;
	exports aletheia.peertopeer.spliced.dialog;
	exports aletheia.peertopeer.conjugal.message;
	exports aletheia.peertopeer.ephemeral;
	exports aletheia.peertopeer.resource;
	exports aletheia.peertopeer.spliced.phase;
	exports aletheia.peertopeer.network.dialog;
	exports aletheia.peertopeer.network.protocol;
	exports aletheia.peertopeer.conjugal.phase;
	exports aletheia.peertopeer.io;
	exports aletheia.peertopeer.base.dialog;
	exports aletheia.peertopeer;
	exports aletheia.peertopeer.conjugal;
	exports aletheia.peertopeer.network.message.routeablesubmessage;
	exports aletheia.peertopeer.statement.phase;
	exports aletheia.peertopeer.conjugal.dialog;
	exports aletheia.peertopeer.statement.dialog;
	exports aletheia.peertopeer.ephemeral.message;
	exports aletheia.peertopeer.spliced;
	exports aletheia.peertopeer.base.protocol;
	exports aletheia.peertopeer.ephemeral.phase;
	exports aletheia.peertopeer.statement.message;
	exports aletheia.peertopeer.base.message;
	exports aletheia.peertopeer.ephemeral.dialog;
	exports aletheia.peertopeer.network.phase;
	exports aletheia.peertopeer.statement;
	exports aletheia.peertopeer.standalone;
	exports aletheia.peertopeer.network.message;
	exports aletheia.peertopeer.base;
	exports aletheia.peertopeer.network;
	exports aletheia.peertopeer.base.phase;

	requires transitive aletheia.base;
	requires aletheia.log4j;
	requires aletheia.persistence.berkeleydb;
	requires aletheia.protocol;
	requires aletheia.security;
	requires aletheia.utilities;
	requires aletheia.version;
	requires org.apache.logging.log4j;
}