package org.jenkinsci.plugins.windup;

import lombok.Getter;

public enum WindupTechnology {
	SOURCE("Source"), TARGET("Target");

	@Getter
	private final String arg;

	WindupTechnology(String arg) {
		this.arg = arg;
	}

}
