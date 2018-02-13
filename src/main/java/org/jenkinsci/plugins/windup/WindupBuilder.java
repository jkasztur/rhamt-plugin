package org.jenkinsci.plugins.windup;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.furnace.repositories.AddonRepositoryMode;
import org.jboss.forge.furnace.se.FurnaceFactory;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;

import org.jenkinsci.plugins.windup.checking.InputOutputCheck;
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
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

@Slf4j
public class WindupBuilder extends Builder {

	private final String input;
	private final String output;
	private final String altParams;
	private final String source;
	private final String target;

	@DataBoundConstructor
	public WindupBuilder(String input, String output, String altParams, String source, String target) {
		this.input = input;
		this.output = output;
		this.altParams = altParams;
		this.source = source;
		this.target = target;
	}

	public String getInput() {
		return input;
	}

	public String getOutput() {
		return output;
	}

	public String getAltParams() {
		return altParams;
	}

	public String getSource() {
		return source;
	}

	public String getTarget() {
		return target;
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

		furnace.addRepository(AddonRepositoryMode.MUTABLE, new File(getDescriptor().getWindupHome(), "addons"));
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

		private String windupHome;

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
			return "execute Windup";
		}

		public String getWindupHome() {
			return windupHome;
		}

		@Override
		public boolean configure(StaplerRequest staplerRequest, JSONObject json) throws FormException {
			json = json.getJSONObject("windup");
			windupHome = json.getString("windupHome");
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
				list.addAll(TechnologyOptions.getTechnologies(windupHome, WindupTechnology.SOURCE));
				sourceItems = list;
			} catch (IOException e) {
				e.printStackTrace();
				return new ListBoxModel();
			}
			return list;
		}

		public FormValidation doReloadSource() {
			try {
				TechnologyOptions.reloadTechnology(windupHome, WindupTechnology.SOURCE);
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
				list.addAll(TechnologyOptions.getTechnologies(windupHome, WindupTechnology.TARGET));
				targetItems = list;
			} catch (IOException e) {
				e.printStackTrace();
				return new ListBoxModel();
			}
			return list;
		}

		public FormValidation doReloadTarget() {
			try {
				TechnologyOptions.reloadTechnology(windupHome, WindupTechnology.TARGET);
				targetItems = doFillTargetItems();
			} catch (Exception e) {
				return FormValidation.error("Reloading failed: " + e.getMessage());
			}
			return FormValidation.ok("Reload successful");
		}

		public FormValidation doCheckWindupHome(@QueryParameter String value) {
			if (value == null || value.trim().equals(""))
				return FormValidation.warning("No directory specified.");

			File ftmp = new File(value);

			if (!ftmp.exists())
				return FormValidation.error("Specified directory not found.");
			if (!ftmp.isDirectory())
				return FormValidation.error("Not a directory.");

			File exec = new File(ftmp, "bin/windup");

			if (!exec.exists())
				return FormValidation.error("bin/windup script does not exist in Windup directory");
			if (!exec.canExecute())
				return FormValidation.error("windup script is not executable.");

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