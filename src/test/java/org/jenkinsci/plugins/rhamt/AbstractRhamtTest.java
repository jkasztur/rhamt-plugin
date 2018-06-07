package org.jenkinsci.plugins.rhamt;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Rule;

import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractRhamtTest {
	private boolean isInitialized = false;
	protected String rhamtHome;
	protected FreeStyleProject project;

	@Rule
	public JenkinsRule jenkinsRule = new JenkinsRule();

	@Before
	public void createTestproject() throws IOException {
		project = jenkinsRule.jenkins.createProject(FreeStyleProject.class, "junitTestproject");
	}

	@Before
	public void setProperties() throws IOException {
		if (!isInitialized) {
			final Properties properties = new Properties();
			properties.load(AbstractRhamtTest.class.getClassLoader().getResource("test.properties").openStream());
			rhamtHome = properties.getProperty("rhamt.home");
			log.info("RHAMT_HOME: " + rhamtHome);
		}
		isInitialized = true;
	}

	protected void testBuildResult(Result expected) throws Exception {
		project.scheduleBuild(new Cause.UserIdCause());
		jenkinsRule.waitUntilNoActivityUpTo(new Long(TimeUnit.MINUTES.toMillis(5)).intValue());
		project.getLastBuild().keepLog();
		assertEquals("Build should have been " + expected, expected, project.getLastBuild().getResult());
	}
}
