package org.jenkinsci.plugins.rhamt.util;

import com.opencsv.CSVReaderHeaderAware;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import hudson.FilePath;

/**
 * helper class to extract metric from file.
 * Currently it's used only for story points, can be extended for other metrics.
 */
public final class MetricUtil {

	private MetricUtil() {
	}

	public static int getStoryPoints(FilePath file) throws IOException, InterruptedException {
		final CSVReaderHeaderAware reader = new CSVReaderHeaderAware(
				new InputStreamReader(file.read(), StandardCharsets.UTF_8));
		int sum = 0;
		String[] next;
		while ((next = reader.readNext("Story points")) != null) {
			sum += Integer.parseInt(next[0]);
		}

		return sum;
	}
}
