module aletheia.peertopeer
{
	requires transitive aletheia.base;
	requires transitive aletheia.persistence.berkeleydb;
	requires aletheia.log4j;
	requires aletheia.protocol;
	requires aletheia.version;

	exports aletheia.peertopeer;
	exports aletheia.peertopeer.base;
	exports aletheia.peertopeer.base.phase;
	exports aletheia.peertopeer.base.message;
	exports aletheia.peertopeer.conjugal.phase;
	exports aletheia.peertopeer.network;
	exports aletheia.peertopeer.network.phase;
	exports aletheia.peertopeer.network.dialog;
	exports aletheia.peertopeer.network.message;
	exports aletheia.peertopeer.network.message.routeablesubmessage;
	exports aletheia.peertopeer.resource;
}