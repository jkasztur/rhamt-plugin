package org.jenkinsci.plugins.rhamt;

import org.jboss.windup.util.exception.WindupException;

import org.jenkinsci.plugins.rhamt.util.MetricUtil;

import hudson.FilePath;
import lombok.Getter;

@Getter
public class RhamtReport {
	private final String appName;
	private int storyPoints;

	public RhamtReport(FilePath csv) {
		this.appName = csv.getName().replace(".csv", "");
		try {
			this.storyPoints = MetricUtil.getStoryPoints(csv);
		} catch (Exception e) {
			throw new WindupException(e);
		}
	}
}
