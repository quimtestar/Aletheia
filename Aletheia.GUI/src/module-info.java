module aletheia.gui
{
	exports aletheia.gui.app;
	exports aletheia.gui.app.splash;
	exports aletheia.gui.cli.command;
	exports aletheia.gui.cli.command.statement;
	exports aletheia.gui.cli.command.authority;
	exports aletheia.gui.contextjtree;
	exports aletheia.gui.contextjtree.node;
	exports aletheia.gui.contextjtree.sorter;
	exports aletheia.gui.contextjtree.renderer;
	exports aletheia.gui.signaturerequestjtree;
	exports aletheia.gui.cli;
	exports aletheia.gui.person;
	exports aletheia.gui.menu;
	exports aletheia.gui.menu.configuration;
	exports aletheia.gui.menu.security;
	exports aletheia.gui.menu.data;
	exports aletheia.gui.menu.window;
	exports aletheia.gui.menu.help;
	exports aletheia.gui.catalogjtree;
	exports aletheia.gui.common;

	requires transitive aletheia.base;
	requires transitive aletheia.peertopeer;
	requires transitive aletheia.prooffinder;
	requires transitive aletheia.persistence.berkeleydb;
	requires transitive aletheia.parsergenerator;
	requires aletheia.utilities;
	requires aletheia.log4j;
	requires aletheia.version;
	requires aletheia.pdfexport;
	requires java.desktop;
	requires java.management;
}