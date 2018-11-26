package org.jenkinsci.plugins.rhamt.monitor;

import org.jboss.windup.exec.WindupProgressMonitor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import hudson.model.BuildListener;

/**
 * WindupProgressMonitor implementation modified for Jenkins.
 * Source: org.jboss.windup.bootstrap.ConsoleProgressMonitor
 */
public class JenkinsProgressMonitor implements WindupProgressMonitor {
	private int totalWork;
	private int currentWork;
	private boolean cancelled;

	private BuildListener listener;

	public JenkinsProgressMonitor(BuildListener listener) {
		this.listener = listener;
	}

	@Override
	public void beginTask(String name, int totalWork) {
		this.totalWork = totalWork;

		final String message = String.format("%s [%d/%d] %s", getCachedTime(), currentWork, totalWork, name);
		System.out.println(message);
		listener.getLogger().println(message);
	}

	@Override
	public void done() {
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public void setTaskName(String name) {
		final String message = String.format("%s [%d/%d] %s", getCachedTime(), currentWork, totalWork, name);
		System.out.println(message);
		listener.getLogger().println(message);
	}

	@Override
	public void subTask(String subTask) {
		final String message = String.format("%s [%d/%d] %s", getCachedTime(), currentWork, totalWork, subTask);
		if (subTask.endsWith("\r")) {
			System.out.print(message);
		} else {
			System.out.println("\r" + message);
		}
		listener.getLogger().println(message);
	}

	@Override
	public void worked(int work) {
		this.currentWork += work;
	}

	private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static long lastFormatted = 0;
	private static String lastFormattedString = "";

	private static String getCachedTime() {
		final long now = System.currentTimeMillis();
		if (now > lastFormatted + 60_000) {
			Date date = new Date(now);
			final String format;
			// SimpleDateFormat is not thread safe.
			synchronized (DATE_FORMATTER) {
				format = DATE_FORMATTER.format(date);
				lastFormatted = now;
				lastFormattedString = format;
			}
		}
		return lastFormattedString;
	}
}
