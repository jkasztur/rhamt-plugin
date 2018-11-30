package org.jenkinsci.plugins.rhamt;

import org.junit.Test;

import hudson.model.Result;

public class RhamtHomeTestIT extends AbstractRhamtTest {
	@Test
	public void emptyRhamtHomeTest() throws Exception {
		project.getBuildersList().add(getDefaultBuilder());
		testBuildResult(Result.FAILURE);
	}

	@Test
	public void wrongRhamtHomeTest() throws Exception {
		project.getBuildersList().add(getBasicBuilder(rhamtHome + "_failed"));
		testBuildResult(Result.FAILURE);
	}
}
