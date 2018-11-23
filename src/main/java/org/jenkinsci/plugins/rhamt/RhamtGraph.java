package org.jenkinsci.plugins.rhamt;

import static org.jfree.chart.ChartFactory.createLineChart;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

import java.util.Map;

import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;

/**
 * Graph implementation that shows metric based on RHAMT Story Points
 */
public class RhamtGraph extends Graph {
	private String name;
	private Map<ChartUtil.NumberOnlyBuildLabel, Integer> reports;

	protected RhamtGraph(String name, Map<ChartUtil.NumberOnlyBuildLabel, Integer> reports) {
		super(-1, 400, 300); // cannot use timestamp, since ranges may change
		this.name = name;
		this.reports = reports;
	}

	private DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> createDataSet() {
		final DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder =
				new DataSetBuilder<>();
		if (reports == null) {
			return dataSetBuilder;
		}
		for (Map.Entry<ChartUtil.NumberOnlyBuildLabel, Integer> entry : reports.entrySet()) {
			dataSetBuilder.add(entry.getValue(), name, entry.getKey());
		}
		return dataSetBuilder;
	}

	@Override
	protected JFreeChart createGraph() {
		return createLineChart(name,
				null, null,
				createDataSet().build(),
				PlotOrientation.VERTICAL,
				false, false, false);
	}
}
