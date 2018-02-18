package org.jenkinsci.plugins.rhamt;

import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.configuration.options.OverwriteOption;
import org.jboss.windup.exec.configuration.options.SourceOption;
import org.jboss.windup.exec.configuration.options.TargetOption;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import hudson.FilePath;
import hudson.model.BuildListener;

public final class ConfigOptions {

	private static WindupConfiguration config;
	private static FilePath workspace;
	private static BuildListener listener;

	public static WindupConfiguration createCommand(RhamtBuilder builder, FilePath w, BuildListener buildListener) throws IOException, InterruptedException {

		config = new WindupConfiguration();
		workspace = w;
		listener = buildListener;

		addInput(builder);
		addOutput(builder);
		addSource(builder);
		addTarget(builder);

		return config;
	}

	private static void addInput(RhamtBuilder builder) {
		String input = builder.getInput();
		if (input == null || input.trim().equals("")) {
			return;
		}
		final String[] inputs = input.split(",");

		for (String s : inputs) {
			config.addInputPath(new File(s).toPath());
		}
	}

	private static void addOutput(RhamtBuilder builder) throws IOException, InterruptedException {
		final String output = builder.getOutput();
		if (output == null || output.trim().equals("")) {
			// TODO if null set to workspace
			return;
		}
		final URI outputUri = new FilePath(workspace, output).toURI();
		final Path outputPath = new File(outputUri).toPath();
		config.setOutputDirectory(outputPath);

		if (new File(outputUri).exists()) {
			config.setOptionValue(OverwriteOption.NAME, true);
			listener.getLogger().println("Adding input path: " + outputPath.toString());
		}
	}

	private static void addSource(RhamtBuilder builder) {
		final String source = builder.getSource();
		if ("<custom>".equals(source)) {
			return;
		}
		List<String> sources = new ArrayList<>();
		sources.add(source);
		config.setOptionValue(SourceOption.NAME, sources);
		listener.getLogger().println("Setting source: " + source);
	}

	private static void addTarget(RhamtBuilder builder) {
		final String target = builder.getTarget();
		if ("<custom>".equals(target)) {
			return;
		}
		List<String> targets = new ArrayList<>();
		targets.add(target);
		config.setOptionValue(TargetOption.NAME, targets);
		listener.getLogger().println("Setting target: " + target);
	}
}
