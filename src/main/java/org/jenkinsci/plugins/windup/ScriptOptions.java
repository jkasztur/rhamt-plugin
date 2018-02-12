package org.jenkinsci.plugins.windup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import hudson.util.ListBoxModel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScriptOptions {

	private static File scriptFile = null;
	private static String windupHome = null;
	private static String sourceTechPath = null;
	private static String targetTechPath = null;

	private static void setScript(String home) {
		if (windupHome == null) {
			windupHome = home;
		}
		final File pluginFolder = new File(windupHome, "jenkins-plugin");
		if (!pluginFolder.exists()) {
			pluginFolder.mkdirs();
		}

		// TODO check if windows
		scriptFile = new File(windupHome, "bin/windup");
		if (!scriptFile.exists()) {
			scriptFile = new File(windupHome, "bin/rhamt-cli");
		}
	}

	public static List<ListBoxModel.Option> getTechnologies(String home, WindupTechnology arg) throws IOException {
		setScript(home);
		final List<ListBoxModel.Option> options = new ArrayList<>();

		String techPath;
		switch (arg) {
			case SOURCE:
				if (sourceTechPath == null || !new File(sourceTechPath).exists()) {
					reloadTechnology(arg);
				}
				techPath = sourceTechPath;
				break;
			case TARGET:
				if (targetTechPath == null || !new File(targetTechPath).exists()) {
					reloadTechnology(arg);
				}
				techPath = targetTechPath;
				break;
			default:
				log.error("Invalid argument " + arg);
				return options;
		}

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(techPath), "UTF-8"))) {
			String line;
			while ((line = reader.readLine()) != null) {
				options.add(new ListBoxModel.Option(line));
			}
		}

		return options;
	}

	private static void reloadTechnology(WindupTechnology arg) throws IOException {
		final ProcessBuilder pb = new ProcessBuilder(scriptFile.getAbsolutePath(), "--list" + arg.getArg() + "Technologies");

		Process process;
		String result;

		process = pb.start();
		final BufferedReader reader =
				new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()));
		final StringBuilder builder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
			builder.append(System.getProperty("line.separator"));
		}
		result = builder.toString();
		reader.close();

		final String techs = result.split("Available " + arg.getArg().toLowerCase() + " technologies:")[1];

		final File techFile = new File(windupHome, "jenkins-plugin/" + arg.getArg().toLowerCase());

		techFile.createNewFile();
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(techFile, false), "UTF-8")) {
			for (String s : techs.split("\n")) {
				String source = s.trim();
				if ("".equals(source)) {
					continue;
				}
				writer.write(source + System.lineSeparator());
			}
		}

		switch (arg) {
			case SOURCE:
				sourceTechPath = techFile.getAbsolutePath();
				break;
			case TARGET:
				targetTechPath = techFile.getAbsolutePath();
				break;
		}
	}
}
