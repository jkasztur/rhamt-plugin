package org.jenkinsci.plugins.windup;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.tools.DownloadFromUrlInstaller;

// TODO install automatically
public class WindupInstaller extends DownloadFromUrlInstaller {

	@DataBoundConstructor
	public WindupInstaller(String id) {
		super(id);
	}

	@Extension
	public static final class DescriptorImpl extends DownloadFromUrlInstaller.DescriptorImpl<WindupInstaller> {
		public String getDisplayName() {
			return "Install from <give url>";
		}

	}
}
