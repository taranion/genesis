module genesis {
	exports org.prelle.genesis;
	exports org.prelle.genesis.jfx.common;
	exports org.prelle.genesis.jobs;
	exports org.prelle.genesis.screens;
	exports org.prelle.genesis.page;
	exports org.prelle.genesis.print;

	requires de.rpgframework.chars;
	requires de.rpgframework.core;
	requires de.rpgframework.javafx;
	requires de.rpgframework.print;
	requires java.desktop;
	requires java.prefs;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.extensions;
	requires javafx.graphics;
	requires org.apache.logging.log4j;
	requires org.apache.logging.log4j.core;
}