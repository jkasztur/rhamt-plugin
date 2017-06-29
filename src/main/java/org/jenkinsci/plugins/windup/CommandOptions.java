package org.jenkinsci.plugins.windup;

import java.io.File;

public final class CommandOptions {

	public static String createCommand(WindupBuilder.Descriptor desc) {
		String cmd = "";
		cmd += getScript(desc);

		return cmd;
	}

	private static String getScript(WindupBuilder.Descriptor desc) {
		String script = new File(desc.getWindupHome(), "bin/windup").getAbsolutePath();
		// TODO check if windows

		return script;
	}
}
