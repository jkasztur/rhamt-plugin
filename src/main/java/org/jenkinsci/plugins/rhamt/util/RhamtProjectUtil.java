package org.jenkinsci.plugins.rhamt.util;

import org.jenkinsci.plugins.rhamt.RhamtBuildAction;
import org.jenkinsci.plugins.rhamt.RhamtReport;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.util.ChartUtil;

public class RhamtProjectUtil {

	public static Set<String> getExistingAppNames(AbstractProject<?, ?> project) {
		final Set<String> set = new HashSet<>();
		if (project == null) {
			return set;
		}
		final List<? extends AbstractBuild<?, ?>> builds = project.getBuilds();
		for (AbstractBuild<?, ?> build : builds) {
			// check if build has RhamtBuildAction action
			final RhamtBuildAction action = build.getAction(RhamtBuildAction.class);
			if (action == null) {
				continue;
			}
			// get reports for all apps from current build
			final Set<RhamtReport> reports = action.getReports();
			if (reports == null) {
				continue;
			}
			for (RhamtReport report : reports) {
				set.add(report.getAppName());
			}
		}
		return set;
	}

	public static Map<String, Map<ChartUtil.NumberOnlyBuildLabel, Integer>> getExistingReports(AbstractProject<?, ?> project) {
		// Map<appname, List<build, storyPoints>>
		final Map<String, Map<ChartUtil.NumberOnlyBuildLabel, Integer>> existing = new HashMap<>();

		if (project == null) {
			return existing;
		}
		final List<? extends AbstractBuild<?, ?>> builds = project.getBuilds();
		for (AbstractBuild<?, ?> build : builds) {
			// check if build has RhamtBuildAction action
			final RhamtBuildAction action = build.getAction(RhamtBuildAction.class);
			if (action == null) {
				continue;
			}
			// get reports for all apps from current build
			final Set<RhamtReport> reports = action.getReports();
			if (reports == null) {
				continue;
			}
			for (RhamtReport report : reports) {
				Map<ChartUtil.NumberOnlyBuildLabel, Integer> c = existing.get(report.getAppName());
				final ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel((Run<?, ?>) build);

				if (c == null) {
					// if this app is build for the first time
					c = new HashMap<>();
					c.put(label, report.getStoryPoints());
					existing.put(report.getAppName(), c);
				} else {
					// if a report was generated for some previous build
					c.put(label, report.getStoryPoints());
				}
			}
		}
		return existing;
	}

	public static Set<RhamtReport> getLastReports(AbstractProject<?, ?> project) {
		AbstractBuild<?, ?> build = project.getLastBuild();
		while (build != null) {
			final RhamtBuildAction action = build.getAction(RhamtBuildAction.class);
			if (action != null) {
				return action.getReports();
			}
			build = build.getPreviousBuild();
		}
		return null;
	}
}
