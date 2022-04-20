package br.com.convertegdb;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class MyLogHandler extends FileHandler {
	public MyLogHandler() throws IOException, SecurityException {
		//super("/tmp/path-to-log.log");
		super("path-to-log.log");
		setFormatter(new SimpleFormatter());
		setLevel(Level.SEVERE);
	}

	@Override
	public void publish(LogRecord record) {		
		super.publish(record);
	}
}
