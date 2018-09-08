package org.jenkinsci.plugins.rhamt;

import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.configuration.options.ExplodedAppInputOption;
import org.jboss.windup.exec.configuration.options.OverwriteOption;
import org.jboss.windup.exec.configuration.options.SourceOption;
import org.jboss.windup.exec.configuration.options.TargetOption;
import org.jboss.windup.rules.apps.java.config.ExcludePackagesOption;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.mavenize.MavenizeGroupIdOption;
import org.jboss.windup.rules.apps.mavenize.MavenizeOption;
import org.jboss.windup.util.PathUtil;

import org.jenkinsci.plugins.rhamt.monitor.JenkinsProgressMonitor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;

import hudson.FilePath;
import hudson.model.BuildListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
		addProgressMonitor();
		addUserRulesDir(builder);
		addPackages(builder);
		addExcludedPackages(builder);
		addBooleanParameters(builder);
		addMavenizeParameters(builder);
		return config;
	}

	private static void addProgressMonitor() {
		JenkinsProgressMonitor monitor = new JenkinsProgressMonitor(listener);
		config.setProgressMonitor(monitor);
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
			listener.getLogger().println("Adding output path: " + outputPath.toString());
		}
	}

	// TODO(jkasztur): resolve 'custom' source and target
	// TODO(jkasztur): resolve multiple source and target
	private static void addSource(RhamtBuilder builder) {
		setArrayParam(SourceOption.NAME, builder.getSource());
	}

	private static void addTarget(RhamtBuilder builder) {
		setArrayParam(TargetOption.NAME, builder.getTarget());
	}

	private static void addUserRulesDir(RhamtBuilder builder) {
		// Always adding default user rules dir
		config.addDefaultUserRulesDirectory(PathUtil.getWindupRulesDir());
		final String rulesDir = builder.getUserRulesDir();
		if (rulesDir == null || rulesDir.trim().equals("")) {
			return;
		}
		File rulesDirFile = new File(rulesDir);
		config.addDefaultUserRulesDirectory(rulesDirFile.toPath());
		listener.getLogger().println("Setting user rules directory: " + rulesDirFile.getAbsolutePath());
	}

	private static void addPackages(RhamtBuilder builder) {
		setArrayParam(ScanPackagesOption.NAME, builder.getPackages());
	}

	private static void addExcludedPackages(RhamtBuilder builder) {
		setArrayParam(ExcludePackagesOption.NAME, builder.getExcludedPackages());
	}

	private static void addBooleanParameters(RhamtBuilder builder) {
		config.setOnline(builder.isOnline());
		config.setOptionValue(ExplodedAppInputOption.NAME, builder.isExplodedApp());
		config.setOptionValue(SourceModeOption.NAME, builder.isSourceMode());
	}

	private static void addMavenizeParameters(RhamtBuilder builder) {
		config.setOptionValue(MavenizeOption.NAME, builder.isMavenize());
		setStringParam(MavenizeGroupIdOption.NAME, builder.getMavenizeGroupId());
	}

	private static void setStringParam(String keyName, String value) {
		if (value == null || value.trim().equals("")) {
			return;
		}
		listener.getLogger().println(String.format("Setting %s: %s", keyName, value));
		config.setOptionValue(keyName, value);
	}

	private static void setArrayParam(String keyName, String raw) {
		if (raw == null || raw.trim().isEmpty()) {
			return;
		}

		String[] splitted = raw.split(",");
		listener.getLogger().println(String.format("Setting %s: %s", keyName, Arrays.toString(splitted)));
		config.setOptionValue(keyName, Arrays.asList(splitted));
	}
}
