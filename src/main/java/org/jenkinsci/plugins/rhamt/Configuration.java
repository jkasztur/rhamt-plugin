package org.jenkinsci.plugins.rhamt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hudson.FilePath;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Configuration {

	private String[] input = null;
	private String output = null;
	private String source = null;
	private String target = null;
	private String[] packages = null;
	private String[] excludedPackages = null;
	private boolean shouldOverwrite = true;
	private String workspace = null;

	public List<String> generateCommand(String script) throws IOException, InterruptedException {
		final List<String> finalCommand = new ArrayList<>();

		finalCommand.add(script);

		// Set input
		if (input != null) {
			finalCommand.add("--input");
			Collections.addAll(finalCommand, input);
		}

		//Set output
		if (output != null) {
			finalCommand.add("--output");
			if (workspace != null) {
				finalCommand.add(workspace + output);
			} else {
				finalCommand.add(output);
			}
		}
		if (output == null && workspace != null) {
			finalCommand.add("--output");
			finalCommand.add(output);
		}

		// Set source
		if (source != null) {
			finalCommand.add("--source");
			finalCommand.add(source);
		}

		// Set target
		if (target != null) {
			finalCommand.add("--target");
			finalCommand.add(target);
		}

		// Set packages
		if (packages != null) {
			finalCommand.add("--packages");
			Collections.addAll(finalCommand, packages);
		}

		// Set excluded packages
		if (excludedPackages != null) {
			finalCommand.add("--excludedPackages");
			Collections.addAll(finalCommand, excludedPackages);
		}

		// Set mandatory overwrite
		if (shouldOverwrite) {
			finalCommand.add("--overwrite");
		}
		return finalCommand;
	}

	public void setInput(String param) {
		if (param == null || param.trim().equals("")) {
			return;
		}
		input = param.split(",");
	}

	public void setOutput(String param) {
		if (param == null || param.trim().equals("")) {
			return;
		}
		output = param;
	}

	public void setSource(String param) {
		if ("<custom>".equals(param)) {
			return;
		}
		source = param;
	}

	public void setTarget(String param) {
		if ("<custom>".equals(param)) {
			return;
		}
		target = param;
	}

	public void setPackages(String param) {
		if (param == null || param.trim().equals("")) {
			return;
		}
		packages = param.split(" ");
	}

	public void setExcludedPackages(String param) {
		if (param == null || param.trim().equals("")) {
			return;
		}
		excludedPackages = param.split(" ");
	}

	public void setWorkspace(FilePath path) {
		if (path == null) {
			return;
		}
		try {
			workspace = path.toURI().toString().replace("file:", "");
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
