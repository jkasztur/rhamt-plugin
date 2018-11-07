package org.jenkinsci.plugins.rhamt;

import javax.annotation.CheckForNull;

import java.util.Set;

import hudson.model.Action;
import lombok.Getter;

public class RhamtBuildAction implements Action {

	@Getter
	private final Set<RhamtReport> reports;

	public RhamtBuildAction(Set<RhamtReport> reports) {
		this.reports = reports;
	}

	@CheckForNull
	@Override
	public String getIconFileName() {
		return null;
	}

	@CheckForNull
	@Override
	public String getDisplayName() {
		return null;
	}

	@CheckForNull
	@Override
	public String getUrlName() {
		return null;
	}
}
