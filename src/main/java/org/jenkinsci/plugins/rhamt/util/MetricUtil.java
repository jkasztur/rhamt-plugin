package org.jenkinsci.plugins.rhamt.util;

import com.opencsv.CSVReaderHeaderAware;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import hudson.FilePath;

public final class MetricUtil {

	private MetricUtil() {
	}

	public static int getStoryPoints(FilePath file) throws IOException, InterruptedException {
		final CSVReaderHeaderAware reader = new CSVReaderHeaderAware(
				new InputStreamReader(file.read(), Charset.defaultCharset()));
		int sum = 0;
		String[] next;
		while ((next = reader.readNext("Story points")) != null) {
			sum += Integer.parseInt(next[0]);
		}

		return sum;
	}
}
