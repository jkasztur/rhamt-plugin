package org.jenkinsci.plugins.rhamt;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Rule;

import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import hudson.model.Cause;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractRhamtTest {
	public static String rhamtHome;
	protected FreeStyleProject project;

	public static final String TEST_PROJECT_NAME = "junitTestProject";

	@Rule
	public JenkinsRule jenkinsRule = new JenkinsRule();

	@Before
	public void createTestproject() throws IOException {
		project = jenkinsRule.jenkins.createProject(FreeStyleProject.class, TEST_PROJECT_NAME);
	}

	static {
		final Properties properties = new Properties();
		try {
			properties.load(AbstractRhamtTest.class.getClassLoader().getResource("test.properties").openStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		rhamtHome = properties.getProperty("rhamt.home");
	}

	protected FreeStyleBuild testBuildResult(Result expected) throws Exception {
		project.scheduleBuild(new Cause.UserIdCause());
		jenkinsRule.waitUntilNoActivityUpTo(new Long(TimeUnit.MINUTES.toMillis(5)).intValue());
		final FreeStyleBuild build = project.getLastBuild();
		assertEquals("Build should have been " + expected, expected, build.getResult());
		return build;
	}

	/**
	 * Create preconfigured builder with test values.
	 *
	 * @param rhamtHome RHAMT home path
	 * @return fully configured builder
	 */
	public static RhamtBuilder getBasicBuilder(String rhamtHome) {
		final RhamtBuilder builder = getDefaultBuilder();
		builder.getDescriptor().setRhamtHome(rhamtHome);
		builder.setInput(new File(AbstractRhamtTest.class.getClassLoader().getResource("simple-sample-app.ear").getFile()).getAbsolutePath());
		builder.setSource("weblogic");
		builder.setTarget("eap6");
		return builder;
	}

	/**
	 * Create builder with default values.
	 *
	 * @return builder configured with jelly defaults
	 */
	public static RhamtBuilder getDefaultBuilder() {
		final RhamtBuilder builder = new RhamtBuilder();
		builder.setOutput("rhamtReports");
		builder.setExportCsv(true);

		return builder;
	}

	/**
	 * Create post build step builder with default values.
	 *
	 * @return builder configured with jelly defaults
	 */
	public static RhamtPublisher getDefaultPublisher() {
		final RhamtPublisher publisher = new RhamtPublisher();
		publisher.setCsvRegex("rhamtReports/*.csv");
		return publisher;
	}
}
