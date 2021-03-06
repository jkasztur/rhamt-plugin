package org.jenkinsci.plugins.rhamt;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.furnace.repositories.AddonRepositoryMode;
import org.jboss.forge.furnace.se.FurnaceFactory;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.util.PathUtil;
import org.jboss.windup.util.exception.WindupException;

import org.jenkinsci.plugins.rhamt.checking.InputOutputCheck;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

/**
 * Class implementing Jenkins build step.
 */
@Slf4j
@Getter
@Setter
public class RhamtBuilder extends Builder {

	@DataBoundSetter
	private String input;
	@DataBoundSetter
	private String output;
	@DataBoundSetter
	private String source;
	@DataBoundSetter
	private String target;
	@DataBoundSetter
	private String userRulesDir;
	@DataBoundSetter
	private String packages;
	@DataBoundSetter
	private String excludedPackages;
	@DataBoundSetter
	private boolean online;
	@DataBoundSetter
	private boolean sourceMode;
	@DataBoundSetter
	private boolean explodedApp;
	@DataBoundSetter
	private boolean mavenize;
	@DataBoundSetter
	private String mavenizeGroupId;
	@DataBoundSetter
	private boolean tattletale;
	@DataBoundSetter
	private boolean exportCsv;
	@DataBoundSetter
	private boolean keepWorkDirs;
	@DataBoundSetter
	private boolean compatibleFilesReport;
	@DataBoundSetter
	private boolean classNotFoundAnalysis;
	@DataBoundSetter
	private String includedTags;
	@DataBoundSetter
	private String excludedTags;
	@DataBoundSetter
	private String additionalClasspath;
	@DataBoundSetter
	private String userIgnorePath;
	@DataBoundSetter
	private boolean skipReports;

	@DataBoundConstructor
	public RhamtBuilder() {
	}

	/**
	 * The perform method executes RHAMT with given options.
	 *
	 * @param build current build
	 * @param launcher Jenkins launcher
	 * @param listener build listener
	 * @return true if successful, false if a problem occurred.
	 * @throws IOException exception
	 * @throws InterruptedException exception
	 */
	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		final String rhamtHome = getDescriptor().getRhamtHome();
		if (rhamtHome == null || rhamtHome.trim().isEmpty()) {
			throw new WindupException("RHAMT home is not set.");
		}
		// WINDUP_HOME must be set for Furnace
		System.setProperty(PathUtil.WINDUP_HOME, getDescriptor().getRhamtHome());
		Furnace furnace = null;
		try {
			furnace = createAndStartFurnace();
			final AddonRegistry addonRegistry = furnace.getAddonRegistry();
			final WindupProcessor windupProcessor = addonRegistry.getServices(WindupProcessor.class).get();
			final WindupConfiguration config = ConfigOptions.createCommand(this, build.getWorkspace(), listener);

			final GraphContextFactory graphContextFactory = addonRegistry.getServices(GraphContextFactory.class).get();
			final Path graphPath = config.getOutputDirectory().resolve("graph");
			final GraphContext graphContext = graphContextFactory.create(graphPath);

			config.setGraphContext(graphContext);
			windupProcessor.execute(config);
			if (!isSkipReports()) {
				listener.getLogger().println("HTML Report is located at: " + config.getOutputDirectory().toString() + "/index.html");
			}
		} catch (ExecutionException e) {
			e.printStackTrace();
		} finally {
			if (furnace != null) {
				furnace.close();
			}
			System.clearProperty(PathUtil.WINDUP_HOME);
			System.clearProperty("INTERACTIVE");
		}
		return true;
	}

	/**
	 * Prepares Furnace with addons from RHAMT_HOME.
	 *
	 * @return started Furnace object
	 * @throws ExecutionException exception
	 * @throws InterruptedException exception
	 */
	private Furnace createAndStartFurnace() throws ExecutionException, InterruptedException {
		final Furnace furnace = FurnaceFactory.getInstance();
		furnace.addRepository(AddonRepositoryMode.MUTABLE, new File(getDescriptor().getRhamtHome(), "addons"));
		// Start Furnace in another thread
		System.setProperty("INTERACTIVE", "false");
		final Future<Furnace> future = furnace.startAsync();
		return future.get();
	}

	@Override
	public Descriptor getDescriptor() {
		return (Descriptor) super.getDescriptor();
	}

	@Extension
	public static class Descriptor extends BuildStepDescriptor<Builder> {

		private String rhamtHome;

		private ListBoxModel sourceItems = null;
		private ListBoxModel targetItems = null;

		public Descriptor() {
			load();
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Nonnull
		@Override
		public String getDisplayName() {
			return "Execute RHAMT";
		}

		public String getRhamtHome() {
			return rhamtHome;
		}

		protected void setRhamtHome(String home) {
			rhamtHome = home;
		}

		@Override
		public boolean configure(StaplerRequest staplerRequest, JSONObject json) throws FormException {
			json = json.getJSONObject("rhamt");
			rhamtHome = json.getString("rhamtHome");
			save();
			return true;
		}

		public ListBoxModel doFillSourceItems() {
			if (sourceItems != null) {
				return sourceItems;
			}
			final ListBoxModel list = new ListBoxModel();
			try {
				list.addAll(TechnologyOptions.getTechnologies(rhamtHome, Technology.SOURCE));
				sourceItems = list;
			} catch (Exception e) {
				e.printStackTrace();
				return new ListBoxModel();
			}
			return list;
		}

		public FormValidation doReloadSource() {
			try {
				TechnologyOptions.reloadTechnology(rhamtHome, Technology.SOURCE);
				sourceItems = doFillSourceItems();
			} catch (Exception e) {
				return FormValidation.error("Reloading failed: " + e.getMessage());
			}
			return FormValidation.ok("Reload successful");
		}

		public ListBoxModel doFillTargetItems() {
			if (targetItems != null) {
				return targetItems;
			}
			final ListBoxModel list = new ListBoxModel();
			try {
				list.addAll(TechnologyOptions.getTechnologies(rhamtHome, Technology.TARGET));
				targetItems = list;
			} catch (Exception e) {
				e.printStackTrace();
				return new ListBoxModel();
			}
			return list;
		}

		public FormValidation doReloadTarget() {
			try {
				TechnologyOptions.reloadTechnology(rhamtHome, Technology.TARGET);
				targetItems = doFillTargetItems();
			} catch (Exception e) {
				return FormValidation.error("Reloading failed: " + e.getMessage());
			}
			return FormValidation.ok("Reload successful");
		}

		public FormValidation doCheckRhamtHome(@QueryParameter String value) {
			if (value == null || value.trim().equals(""))
				return FormValidation.warning("No directory specified.");

			final File ftmp = new File(value);

			if (!ftmp.exists())
				return FormValidation.error("Specified directory not found.");
			if (!ftmp.isDirectory())
				return FormValidation.error("Not a directory.");

			final File exec = new File(ftmp, "bin/rhamt-cli");

			if (!exec.exists())
				return FormValidation.error("bin/rhamt-cli script does not exist in RHAMT home");
			if (!exec.canExecute())
				return FormValidation.error("rhamt-cli script is not executable.");

			return FormValidation.ok();
		}

		public FormValidation doCheckInput(@QueryParameter String value) {
			return InputOutputCheck.checkInput(value);
		}

		public FormValidation doCheckOutput(@QueryParameter String value) {
			return InputOutputCheck.checkOutput(value);
		}
	}
}