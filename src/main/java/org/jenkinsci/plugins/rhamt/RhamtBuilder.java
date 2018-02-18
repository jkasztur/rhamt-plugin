package org.jenkinsci.plugins.rhamt;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.furnace.repositories.AddonRepositoryMode;
import org.jboss.forge.furnace.se.FurnaceFactory;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;

import org.jenkinsci.plugins.rhamt.checking.InputOutputCheck;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;

import java.io.File;
import java.io.IOException;
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
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

@Slf4j
public class RhamtBuilder extends Builder {

	@Getter
	private final String input;
	@Getter
	private final String output;
	@Getter
	private final String altParams;
	@Getter
	private final String source;
	@Getter
	private final String target;
	@Getter
	private final String packages;
	@Getter
	private final String excludedPackages;

	@DataBoundConstructor
	public RhamtBuilder(String input, String output, String altParams, String source, String target, String packages, String excludedPackages) {
		this.input = input;
		this.output = output;
		this.altParams = altParams;
		this.source = source;
		this.target = target;
		this.packages = packages;
		this.excludedPackages = excludedPackages;
	}

	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		Furnace furnace = null;
		try {
			furnace = createAndStartFurnace();
			AddonRegistry addonRegistry = furnace.getAddonRegistry();
			WindupProcessor windupProcessor = addonRegistry.getServices(WindupProcessor.class).get();
			final WindupConfiguration config = ConfigOptions.createCommand(this, build.getWorkspace(), listener);
			windupProcessor.execute(config);
			// TODO: fix link logging
			listener.getLogger().println("See output at: file://" + config.getOutputDirectory().toString() + "/index.html");
		} catch (ExecutionException e) {
			e.printStackTrace();
		} finally {
			if (furnace != null) {
				furnace.close();
			}
		}
		return true;
	}

	private Furnace createAndStartFurnace() throws ExecutionException, InterruptedException {
		final Furnace furnace = FurnaceFactory.getInstance();

		furnace.addRepository(AddonRepositoryMode.MUTABLE, new File(getDescriptor().getRhamtHome(), "addons"));
		// Start Furnace in another thread
		System.setProperty("INTERACTIVE", "false");
		Future<Furnace> future = furnace.startAsync();
		return future.get();
	}

	@Override
	public Descriptor getDescriptor() {
		return (Descriptor) super.getDescriptor();
	}

	@Extension
	public static class Descriptor extends BuildStepDescriptor<Builder> {

		private String rhamtHome;

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
			return "execute RHAMT";
		}

		public String getRhamtHome() {
			return rhamtHome;
		}

		@Override
		public boolean configure(StaplerRequest staplerRequest, JSONObject json) throws FormException {
			json = json.getJSONObject("rhamt");
			rhamtHome = json.getString("rhamtHome");
			save();
			return true;
		}

		private ListBoxModel sourceItems = null;

		public ListBoxModel doFillSourceItems() {
			if (sourceItems != null) {
				return sourceItems;
			}
			ListBoxModel list = new ListBoxModel();
			try {
				list.addAll(TechnologyOptions.getTechnologies(rhamtHome, Technology.SOURCE));
				sourceItems = list;
			} catch (IOException e) {
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

		private ListBoxModel targetItems = null;

		public ListBoxModel doFillTargetItems() {
			if (targetItems != null) {
				return targetItems;
			}
			ListBoxModel list = new ListBoxModel();
			try {
				list.addAll(TechnologyOptions.getTechnologies(rhamtHome, Technology.TARGET));
				targetItems = list;
			} catch (IOException e) {
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

			File ftmp = new File(value);

			if (!ftmp.exists())
				return FormValidation.error("Specified directory not found.");
			if (!ftmp.isDirectory())
				return FormValidation.error("Not a directory.");

			File exec = new File(ftmp, "bin/rhamt-cli");

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

		// TODO(jkasztur): check alt params
		public FormValidation doCheckAltParams(@QueryParameter String value) {
			return FormValidation.ok();
		}
	}
}