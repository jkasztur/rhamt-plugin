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
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractRhamtTest {
	public static String rhamtHome;
	protected FreeStyleProject project;

	@Rule
	public JenkinsRule jenkinsRule = new JenkinsRule();

	@Before
	public void createTestproject() throws IOException {
		project = jenkinsRule.jenkins.createProject(FreeStyleProject.class, "junitTestproject");
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

	protected void testBuildResult(Result expected) throws Exception {
		project.scheduleBuild(new Cause.UserIdCause());
		jenkinsRule.waitUntilNoActivityUpTo(new Long(TimeUnit.MINUTES.toMillis(5)).intValue());
		assertEquals("Build should have been " + expected, expected, project.getLastBuild().getResult());
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
}
