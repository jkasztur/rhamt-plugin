package org.jenkinsci.plugins.windup;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hudson.FilePath;

public final class CommandOptions {

	private static List<String> commandList;
	private static FilePath workspace;

	public static List<String> createCommand(WindupBuilder builder, FilePath w) throws IOException, InterruptedException {

		commandList = new ArrayList<>();
		workspace = w;

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

	private static void addOutput(WindupBuilder builder) throws IOException, InterruptedException {
		final String output = builder.getOutput();
		if (output == null || output.trim().equals("")) {
			return;
		}

		URI outputUri = new FilePath(workspace, output).toURI();
		commandList.add("--output");
		commandList.add(outputUri.getPath());

		if (new File(outputUri).exists()) {
			// If directory exists, Windup asks if it should overwrite
			commandList.add("--overwrite");
		}
	}

	private static void addAltParams(WindupBuilder builder) {
		final String altParams = builder.getAltParams();
		if (altParams == null || altParams.trim().equals("")) {
			return;
		}
		commandList.addAll(Arrays.asList(altParams.split(" ")));
	}
}
