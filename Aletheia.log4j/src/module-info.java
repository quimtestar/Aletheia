module aletheia.log4j
{
	requires aletheia.version;
	requires transitive org.apache.logging.log4j;
	requires org.apache.logging.log4j.core;

	exports aletheia.log4j;
}