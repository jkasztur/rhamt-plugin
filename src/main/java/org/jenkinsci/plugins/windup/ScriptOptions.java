package org.jenkinsci.plugins.windup;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import hudson.util.ListBoxModel;

public class ScriptOptions {

	private static File scriptFile = null;

	private static void setScript(WindupBuilder.Descriptor decriptor) {
		if (scriptFile != null) {
			return;
		}

		// TODO check if windows
		scriptFile = new File(decriptor.getWindupHome(), "bin/windup");
		if (!scriptFile.exists()) {
			scriptFile = new File(decriptor.getWindupHome(), "bin/rhamt-cli");
		}
	}

	public static List<ListBoxModel.Option> getSourceTechnologies(WindupBuilder.Descriptor descriptor) {
		return getTechnologies(descriptor, "Source");
	}

	public static List<ListBoxModel.Option> getTargetTechnologies(WindupBuilder.Descriptor descriptor) {
		return getTechnologies(descriptor, "Target");
	}

	private static List<ListBoxModel.Option> getTechnologies(WindupBuilder.Descriptor descriptor, String arg) {
		setScript(descriptor);
		List<ListBoxModel.Option> l = new ArrayList<>();

		ProcessBuilder pb = new ProcessBuilder(scriptFile.getAbsolutePath(), "--list" + arg + "Technologies");

		Process process;
		String result = "";
		try {
			process = pb.start();
			BufferedReader reader =
					new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()));
			StringBuilder builder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty("line.separator"));
			}
			result = builder.toString();
			reader.close();
		} catch (IOException e) {
			l.add(new ListBoxModel.Option("Failed to retrieve " + arg));
			return l;
		}

		String source = result.split("Available " + arg.toLowerCase() + " technologies:")[1];
		for (String s : source.split("\n")) {
			if ("".equals(s.trim())) {
				continue;
			}
			l.add(new ListBoxModel.Option(s.trim()));
		}
		l.add(new ListBoxModel.Option("<custom>"));

		return l;
	}
}
