package org.jenkinsci.plugins.rhamt;

import org.jboss.windup.util.exception.WindupException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import lombok.Getter;
import lombok.Setter;

/**
 * Class processing and storing RHAMT reports.
 */
public class RhamtPublisher extends Recorder {

	@DataBoundSetter
	@Setter
	@Getter
	private String csvRegex;

	@DataBoundConstructor
	public RhamtPublisher() {
	}

	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Override
	public BuildStepDescriptor<Publisher> getDescriptor() {
		return DESCRIPTOR;
	}

	@Override
	public Action getProjectAction(AbstractProject<?, ?> project) {
		return new RhamtProjectAction(project);
	}

	/**
	 * Scans workspace with given regex and extracts Story Points count from found reports.
	 *
	 * @param build current build
	 * @param launcher Jenkins launcher
	 * @param listener build listener
	 * @return true if successful, false if some problem occurred
	 * @throws IOException exception
	 * @throws InterruptedException exception
	 */
	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		final FilePath workspace = build.getWorkspace();
		if (workspace == null) {
			return true;
		}
		final FilePath[] files = workspace.list(csvRegex);

		final Set<RhamtReport> reports = new HashSet<>();
		for (FilePath file : files) {
			try {
				final RhamtReport report = new RhamtReport(file);
				listener.getLogger().println(String.format("app %s: %d story points", report.getAppName(), report.getStoryPoints()));
				reports.add(report);
			} catch (WindupException e) {
				e.printStackTrace();
			}
		}

		final RhamtBuildAction action = new RhamtBuildAction(reports);
		build.addAction(action);
		return true;
	}

	public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		@Override
		public String getDisplayName() {
			return "Display RHAMT metric";
		}

		public DescriptorImpl() {
			load();
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}
	}
}
