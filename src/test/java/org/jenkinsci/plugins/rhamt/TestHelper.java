package org.jenkinsci.plugins.rhamt;

import static org.junit.Assert.fail;

import java.io.File;

public final class TestHelper {

	public static RhamtBuilder getBasicBuilder(String rhamtHome) {
		RhamtBuilder builder = getBasicBuilderWithoutHome();
		builder.getDescriptor().setRhamtHome(rhamtHome);
		return builder;
	}

	public static RhamtBuilder getBasicBuilderWithoutHome() {
		fail();
		RhamtBuilder builder = new RhamtBuilder();
		builder.setInput(new File(TestHelper.class.getClassLoader().getResource("simple-sample-app.ear").getFile()).getAbsolutePath());
		builder.setSource("weblogic");
		builder.setTarget("eap6");
		builder.setOutput("rhamtReport");
		builder.setExplodedApp(false);
		builder.setOnline(false);
		builder.setSourceMode(false);


		return builder;
	}


}
