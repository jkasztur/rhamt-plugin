package org.jenkinsci.plugins.windup.checking;

import java.io.File;

import hudson.util.FormValidation;

public class InputOutputCheck {

	public static FormValidation checkInput(String value) {
		if (value == null || value.trim().equals(""))
			return FormValidation.error("No input specified");

		String[] inputs = value.split(",");
		for (String input : inputs) {
			final File inputFile = new File(input);
			if (!inputFile.exists()) {
				return FormValidation.warning(input + " does not exist.");
			}
		}

		return FormValidation.ok();
	}

	public static FormValidation checkOutput(String value) {
		if (value == null || value.trim().equals(""))
			return FormValidation.warning("No output directory specified");

		final File output = new File(value);
		if (output.exists()) {
			return FormValidation.warning("Output directory will be overwritten. (" + output.getAbsolutePath() + ")");
		}

		return FormValidation.ok();
	}
}
