package org.jenkinsci.plugins.rhamt;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Rule;

import org.jvnet.hudson.test.JenkinsRule;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractRhamtTest {

	// TODO(jaksztur): change to value taken from properties
	protected String rhamtHome = "/home/jkasztur/programs/rhamt-cli-4.0.0.Final";
	FreeStyleProject project;

	@Rule
	public JenkinsRule jenkinsRule = new JenkinsRule();

	@Before
	public void createTestproject() throws IOException {
		project = jenkinsRule.jenkins.createProject(FreeStyleProject.class, "junitTestproject");
	}

	protected void testBuildResult(Result expected) throws Exception {
		project.scheduleBuild(new Cause.UserIdCause());
		jenkinsRule.waitUntilNoActivityUpTo(new Long(TimeUnit.MINUTES.toMillis(5)).intValue());
		project.getLastBuild().keepLog();
		File logFile = project.getLastBuild().getLogFile();
		Files.copy(logFile, new File("/home/jkasztur/jenkinsTestLogs/" + logFile.getName()));
		log.info(project.getLastBuild().getLog());
		log.info(logFile.getAbsolutePath());
		log.info("HOLAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		assertEquals("Build should have been " + expected, expected, project.getLastBuild().getResult());
	}
}
