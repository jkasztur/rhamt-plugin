package org.jenkinsci.plugins.rhamt;

import java.io.File;

public final class TestHelper {

	public static RhamtBuilder getBasicBuilder(String rhamtHome) {
		RhamtBuilder builder = getBasicBuilderWithoutHome();
		builder.getDescriptor().setRhamtHome(rhamtHome);
		return builder;
	}

	public static RhamtBuilder getBasicBuilderWithoutHome() {
		RhamtBuilder builder = new RhamtBuilder();
		builder.setInput(new File(TestHelper.class.getClassLoader().getResource("simple-sample-app.ear").getFile()).getAbsolutePath());
		builder.setSource("weblogic");
		builder.setTarget("eap6");
		builder.setOutput("rhamtReport");
		return builder;
	}


}
