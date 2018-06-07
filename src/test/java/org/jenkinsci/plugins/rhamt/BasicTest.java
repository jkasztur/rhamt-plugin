package org.jenkinsci.plugins.rhamt;

import org.junit.Test;

import hudson.model.Result;

public class BasicTest extends AbstractRhamtTest {

	@Test
	public void failWithNullConfig() throws Exception {
		RhamtBuilder builder = new RhamtBuilder();
		builder.getDescriptor().setRhamtHome(rhamtHome);

		project.getBuildersList().add(builder);
		testBuildResult(Result.FAILURE);
	}

	@Test
	public void passWithBasicConfig() throws Exception {
		project.getBuildersList().add(TestHelper.getBasicBuilder(rhamtHome));
		testBuildResult(Result.SUCCESS);
	}
}
