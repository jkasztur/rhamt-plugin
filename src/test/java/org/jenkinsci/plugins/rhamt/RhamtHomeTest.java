package org.jenkinsci.plugins.rhamt;

import org.junit.Test;

import hudson.model.Result;

public class RhamtHomeTest extends AbstractRhamtTest {
	@Test
	public void emptyRhamtHomeTest() throws Exception {
		project.getBuildersList().add(TestHelper.getBasicBuilderWithoutHome());
		testBuildResult(Result.FAILURE);
	}

	@Test
	public void filledRhamtHomeTest() throws Exception {
		project.getBuildersList().add(TestHelper.getBasicBuilder(rhamtHome));
		testBuildResult(Result.SUCCESS);
	}
}
