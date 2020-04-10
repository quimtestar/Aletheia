module aletheia.utilities
{
	exports aletheia.utilities.aborter;
	exports aletheia.utilities.io;
	exports aletheia.utilities;
	exports aletheia.utilities.gui;
	exports aletheia.graph;
	exports aletheia.utilities.collections;

	requires aletheia.log4j;
	requires transitive java.desktop;
	requires java.management;
	requires java.xml;
	requires org.apache.logging.log4j;
}