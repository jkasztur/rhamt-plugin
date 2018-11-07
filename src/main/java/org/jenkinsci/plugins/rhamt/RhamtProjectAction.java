package org.jenkinsci.plugins.rhamt;

import org.jenkinsci.plugins.rhamt.util.RhamtProjectUtil;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.annotation.CheckForNull;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.util.ChartUtil;
import hudson.util.Graph;
import lombok.Getter;

public class RhamtProjectAction implements Action {

	private final AbstractProject<?, ?> project;
	@Getter
	private final Set<String> allApps;

	@CheckForNull
	@Override
	public String getIconFileName() {
		return "/plugin/rhamt/images/rhamt-icon-128.png";
	}

	@CheckForNull
	@Override
	public String getDisplayName() {
		return "Migration Metric Trend";
	}

	@CheckForNull
	@Override
	public String getUrlName() {
		return "rhamtMetricTrend";
	}

	public RhamtProjectAction(final AbstractProject<?, ?> project) {
		this.project = project;
		allApps = RhamtProjectUtil.getExistingAppNames(project);
	}

	public AbstractProject<?, ?> getProject() {
		return this.project;
	}

	public Set<RhamtReport> getLastBuildReports() {
		return RhamtProjectUtil.getLastReports(project);
	}

	public void doGraphForApp(StaplerRequest request, StaplerResponse response) throws IOException {
		final String app = request.getParameter("appName");

		final Map<ChartUtil.NumberOnlyBuildLabel, Integer> reports = RhamtProjectUtil.getExistingReports(project).get(app);
		if (!reports.isEmpty()) {
			final Graph g = new RhamtGraph(app, reports);
			g.doPng(request, response);
		}
	}
}
