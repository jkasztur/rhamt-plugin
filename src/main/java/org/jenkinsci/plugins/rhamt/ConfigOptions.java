package org.jenkinsci.plugins.rhamt;

import org.jboss.windup.config.KeepWorkDirsOption;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.configuration.options.ExcludeTagsOption;
import org.jboss.windup.exec.configuration.options.ExplodedAppInputOption;
import org.jboss.windup.exec.configuration.options.ExportCSVOption;
import org.jboss.windup.exec.configuration.options.IncludeTagsOption;
import org.jboss.windup.exec.configuration.options.OnlineModeOption;
import org.jboss.windup.exec.configuration.options.OverwriteOption;
import org.jboss.windup.exec.configuration.options.SourceOption;
import org.jboss.windup.exec.configuration.options.TargetOption;
import org.jboss.windup.rules.apps.java.config.AdditionalClasspathOption;
import org.jboss.windup.rules.apps.java.config.EnableClassNotFoundAnalysisOption;
import org.jboss.windup.rules.apps.java.config.ExcludePackagesOption;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.java.reporting.rules.EnableCompatibleFilesReportOption;
import org.jboss.windup.rules.apps.mavenize.MavenizeGroupIdOption;
import org.jboss.windup.rules.apps.mavenize.MavenizeOption;
import org.jboss.windup.rules.apps.tattletale.EnableTattletaleReportOption;
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

/**
 * Helper class for setting configuration from RhamtBuilder on WindupConfiguration.
 */
@Slf4j
public final class ConfigOptions {

	private ConfigOptions() {
	}

	private static WindupConfiguration config;
	private static FilePath workspace;
	private static BuildListener listener;

	public static WindupConfiguration createCommand(RhamtBuilder builder, FilePath w, BuildListener buildListener) throws IOException, InterruptedException {

		config = new WindupConfiguration();
		workspace = w;
		listener = buildListener;

		listener.getLogger().println("================= SETTINGS =================");
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
		addIncludedTags(builder);
		addExcludedTags(builder);
		addAdditionalClasspath(builder);
		addUserIgnorePath(builder);
		listener.getLogger().println("============================================");
		return config;
	}

	private static void addProgressMonitor() {
		final JenkinsProgressMonitor monitor = new JenkinsProgressMonitor(listener);
		config.setProgressMonitor(monitor);
	}

	private static void addInput(RhamtBuilder builder) {
		final String input = builder.getInput();
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
		final URI outputUri;
		if (output == null || output.trim().equals("")) {
			outputUri = workspace.toURI();
		} else {
			outputUri = new FilePath(workspace, output).toURI();
		}
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
		final File rulesDirFile = new File(rulesDir);
		config.addDefaultUserRulesDirectory(rulesDirFile.toPath());
		listener.getLogger().println("Setting user rules directory: " + rulesDirFile.getAbsolutePath());
	}

	private static void addPackages(RhamtBuilder builder) {
		setArrayParam(ScanPackagesOption.NAME, builder.getPackages());
	}

	private static void addExcludedPackages(RhamtBuilder builder) {
		setArrayParam(ExcludePackagesOption.NAME, builder.getExcludedPackages());
	}

	private static void addAdditionalClasspath(RhamtBuilder builder) {
		setArrayParam(AdditionalClasspathOption.NAME, builder.getAdditionalClasspath());
	}

	private static void addUserIgnorePath(RhamtBuilder builder) {
		// Always adding default user ignore dir
		config.addDefaultUserIgnorePath(PathUtil.getUserIgnoreDir());
		final String ignorePath = builder.getUserIgnorePath();
		if (ignorePath == null || ignorePath.trim().equals("")) {
			return;
		}
		final File ignorePathDir = new File(ignorePath);
		config.addDefaultUserIgnorePath(ignorePathDir.toPath());
		listener.getLogger().println("Setting user rules directory: " + ignorePathDir.getAbsolutePath());
	}

	private static void addBooleanParameters(RhamtBuilder builder) {
		setBoolParam(OnlineModeOption.NAME, builder.isOnline());
		setBoolParam(ExplodedAppInputOption.NAME, builder.isExplodedApp());
		setBoolParam(SourceModeOption.NAME, builder.isSourceMode());
		setBoolParam(EnableTattletaleReportOption.NAME, builder.isTattletale());
		setBoolParam(ExportCSVOption.NAME, builder.isExportCsv());
		setBoolParam(KeepWorkDirsOption.NAME, builder.isKeepWorkDirs());
		setBoolParam(EnableCompatibleFilesReportOption.NAME, builder.isCompatibleFilesReport());
		setBoolParam(EnableClassNotFoundAnalysisOption.NAME, builder.isClassNotFoundAnalysis());
	}

	private static void addIncludedTags(RhamtBuilder builder) {
		setArrayParam(IncludeTagsOption.NAME, builder.getIncludedTags());
	}

	private static void addExcludedTags(RhamtBuilder builder) {
		setArrayParam(ExcludeTagsOption.NAME, builder.getExcludedTags());
	}

	// Helper methods
	private static void addMavenizeParameters(RhamtBuilder builder) {
		setBoolParam(MavenizeOption.NAME, builder.isMavenize());
		setStringParam(MavenizeGroupIdOption.NAME, builder.getMavenizeGroupId());
	}

	private static void setBoolParam(String keyName, boolean value) {
		listener.getLogger().println(String.format("Setting %s: %b", keyName, value));
		config.setOptionValue(keyName, value);
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

		final String[] splitted = raw.split("[,\\s]+");
		listener.getLogger().println(String.format("Setting %s: %s", keyName, Arrays.toString(splitted)));
		config.setOptionValue(keyName, Arrays.asList(splitted));
	}
}
