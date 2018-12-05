package org.jenkinsci.plugins.rhamt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import hudson.model.FreeStyleBuild;
import hudson.model.Result;

public class PostBuildTestIT extends AbstractRhamtTest {

	@Test
	public void noActionsWithoutStepTest() throws Exception {
		project.getBuildersList().add(getBasicBuilder(rhamtHome));
		final FreeStyleBuild build = testBuildResult(Result.SUCCESS);

		assertNull(build.getAction(RhamtBuildAction.class));
		assertNull(build.getAction(RhamtProjectAction.class));
	}

	@Test
	public void actionsWithStep() throws Exception {
		project.getBuildersList().add(getBasicBuilder(rhamtHome));
		project.getPublishersList().add(getDefaultPublisher());
		final FreeStyleBuild build = testBuildResult(Result.SUCCESS);

		final RhamtBuildAction action = build.getAction(RhamtBuildAction.class);
		assertNotNull(action);
		assertEquals(1, action.getReports().size());
		final RhamtReport report = action.getReports().iterator().next();
		assertEquals(21, report.getStoryPoints());
		assertEquals("Simple_Sample_App", report.getAppName());
	}
}
