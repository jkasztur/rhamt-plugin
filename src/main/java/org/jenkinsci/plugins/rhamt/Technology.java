package org.jenkinsci.plugins.rhamt;

import lombok.Getter;

public enum Technology {
	SOURCE("source"), TARGET("target");

	@Getter
	private final String arg;

	Technology(String arg) {
		this.arg = arg;
	}

}
