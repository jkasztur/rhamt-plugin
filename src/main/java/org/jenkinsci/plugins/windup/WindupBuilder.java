package org.jenkinsci.plugins.windup;

import org.jenkinsci.plugins.windup.checking.InputOutputCheck;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;

public class WindupBuilder extends Builder {

	private final String input;
	private final String output;
	private final String altParams;

	@DataBoundConstructor
	public WindupBuilder(String input, String output, String altParams) {
		this.input = input;
		this.output = output;
		this.altParams = altParams;
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

	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		int returnCode = launcher.launch()
				.cmds(CommandOptions.createCommand(this, build.getWorkspace()))
				.stdout(listener)
				.join();

		return returnCode == 0;
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