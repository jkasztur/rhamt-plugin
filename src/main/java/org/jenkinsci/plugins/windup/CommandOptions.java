package org.jenkinsci.plugins.windup;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CommandOptions {

	private static List<String> commandList;

	public static List<String> createCommand(WindupBuilder builder) {

		commandList = new ArrayList<>();

		addScript(builder);
		addInput(builder);
		addOutput(builder);
		addAltParams(builder);

		return commandList;
	}

	private static void addScript(WindupBuilder builder) {
		File scriptFile = new File(builder.getDescriptor().getWindupHome(), "bin/windup");
		if (!scriptFile.exists()) {
			scriptFile = new File(builder.getDescriptor().getWindupHome(), "bin/rhamt-cli");
		}

		String script = scriptFile.getAbsolutePath();
		// TODO check if windows

		commandList.add(script);
	}

	private static void addInput(WindupBuilder builder) {
		String input = builder.getInput();
		if (input == null || input.trim().equals("")) {
			return;
		}
		String[] inputs = input.split(",");

		commandList.add("--input");
		commandList.addAll(Arrays.asList(inputs));
	}

	private static void addOutput(WindupBuilder builder) {
		final String output = builder.getOutput();
		if (output == null || output.trim().equals("")) {
			return;
		}

		commandList.add("--output");
		commandList.add(output);
	}

	private static void addAltParams(WindupBuilder builder) {
		final String altParams = builder.getAltParams();
		if (altParams == null || altParams.trim().equals("")) {
			return;
		}
		commandList.addAll(Arrays.asList(altParams.split(" ")));
	}
}
