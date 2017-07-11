package atsr.controller.transformer;

import java.sql.SQLException;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.runtime.IProgressMonitor;

import atsr.controller.databasewriter.DatabaseWriter;
import atsr.controller.databasewriter.impls.MySQLWriter;
import atsr.model.Settings;

/**
 * The abstract class for all transformations. Transform data into the database.
 * 
 * @author Simon Lehmann
 *
 */
public abstract class Transformer extends Observable {

	private String path;
	private Settings settings;
	private DatabaseWriter writer;
	private IProgressMonitor pmonitor;

	/**
	 * Constructor
	 * 
	 * @param path
	 *            - Path to receive information
	 * @param settings
	 *            - Settings from settingsView
	 * @param obs
	 *            - Observer to notify progress
	 */
	public Transformer(String path, Settings settings, Observer o, IProgressMonitor pmon) throws SQLException {
		this.setPath(path);
		this.setSettings(settings);
		this.addObserver(o);
		this.pmonitor = pmon;

		/*
		 * If the database change and there is a new DatabaseWriter
		 * implementation the only place to change the writer is here.
		 */
		DatabaseWriter writer = new MySQLWriter(settings, pmonitor);
		writer.addObserver(o);
		this.setWriter(writer);
	}

	/**
	 * Notify the observer with an integer value.
	 * 
	 * @param value
	 *            - the progress value between 0 - 100
	 */
	public synchronized void notifyProgress(int value) {
		this.setChanged();
		this.notifyObservers(value);
	}

	/* Getter and setter */

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Settings getSettings() {
		return settings;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	public DatabaseWriter getWriter() {
		return writer;
	}

	public void setWriter(DatabaseWriter writer) {
		this.writer = writer;
	}
}
