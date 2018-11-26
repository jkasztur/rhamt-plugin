package org.jenkinsci.plugins.rhamt;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import hudson.util.ListBoxModel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class TechnologyOptions {

	private TechnologyOptions() {
	}

	private static final String helpFilePath = "cache/help/help.xml";

	private static String rhamtHome = null;
	private static String sourceTechPath = null;
	private static String targetTechPath = null;

	public static List<ListBoxModel.Option> getTechnologies(String home, Technology arg) throws Exception {
		rhamtHome = home;
		final List<ListBoxModel.Option> options = new ArrayList<>();

		final String techPath;

		switch (arg) {
			case SOURCE:
				sourceTechPath = new File(rhamtHome, "jenkins-plugin/source").getAbsolutePath();
				if (!new File(sourceTechPath).exists()) {
					reloadTechnology(home, arg);
				}
				techPath = sourceTechPath;
				break;
			case TARGET:
				targetTechPath = new File(rhamtHome, "jenkins-plugin/target").getAbsolutePath();
				if (!new File(targetTechPath).exists()) {
					reloadTechnology(home, arg);
				}
				techPath = targetTechPath;
				break;
			default:
				log.error("Invalid argument " + arg);
				return options;
		}

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(techPath), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				options.add(new ListBoxModel.Option(line));
			}
		}
		options.sort(Comparator.comparing(o -> o.value));

		return options;
	}

	public static void reloadTechnology(String home, Technology arg) throws Exception {
		// Find the supported technologies
		final File helpFile = new File(home, helpFilePath);
		final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder builder = domFactory.newDocumentBuilder();
		final Document domFile = builder.parse(helpFile);

		final XPathExpression xp = XPathFactory.newInstance().newXPath()
				.compile("//help/option[@name='" + arg.getArg() + "']/available-options/option");
		final NodeList list = (NodeList) xp.evaluate(domFile, XPathConstants.NODESET);

		// Write technologies to file for easier access
		final File techFile = new File(rhamtHome, "jenkins-plugin/" + arg.getArg().toLowerCase());
		if (techFile.exists()) {
			final boolean ret = techFile.delete();
			if (ret) {
				log.info("Old " + arg.getArg().toLowerCase() + " was deleted.");
			} else {
				log.warn("Old " + arg.getArg().toLowerCase() + " could not be deleted.");
			}
		}
		final boolean fileResult = techFile.createNewFile();
		if (!fileResult) {
			log.error(techFile.getAbsolutePath() + " was not created.");
			throw new IOException(techFile.getAbsolutePath() + " was not created.");
		}
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(techFile, false), StandardCharsets.UTF_8)) {
			for (int i = 0; i < list.getLength(); i++) {
				writer.write(list.item(i).getFirstChild().getNodeValue() + System.lineSeparator());
			}
		}

		switch (arg) {
			case SOURCE:
				sourceTechPath = techFile.getAbsolutePath();
				break;
			case TARGET:
				targetTechPath = techFile.getAbsolutePath();
				break;
		}
	}
}
