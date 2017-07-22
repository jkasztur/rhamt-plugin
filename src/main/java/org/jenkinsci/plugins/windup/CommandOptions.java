package org.jenkinsci.plugins.windup;

import java.io.File;

public final class CommandOptions {

	public static String createCommand(WindupBuilder builder) {

		StringBuilder strBuilder = new StringBuilder();

		strBuilder.append(getScript(builder));
		strBuilder.append(getInput(builder));
		strBuilder.append(getOutput(builder));

		return strBuilder.toString();
	}

	private static String getScript(WindupBuilder builder) {
		String script = new File(builder.getDescriptor().getWindupHome(), "bin/windup").getAbsolutePath();
		// TODO check if windows

		script += " ";
		return script;
	}

	private static String getInput(WindupBuilder builder) {
		String input = builder.getInput();
		if (input == null || input.trim().equals("")) {
			return "";
		}
		String[] inputs = input.split(",");

		StringBuilder strBuilder = new StringBuilder("--input ");
		for (String s : inputs) {
			strBuilder.append(s);
			strBuilder.append(' ');
		}
		return strBuilder.toString();
	}

	private static String getOutput(WindupBuilder builder) {
		if (builder.getOutput() == null || builder.getOutput().trim().equals("")) {
			return "";
		}
		final String output = "--output " + builder.getOutput();
		return output;
	}
}
