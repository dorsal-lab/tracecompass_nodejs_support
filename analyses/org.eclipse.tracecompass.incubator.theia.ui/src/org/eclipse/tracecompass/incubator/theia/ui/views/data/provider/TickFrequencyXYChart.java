package org.eclipse.tracecompass.incubator.theia.ui.views.data.provider;

import java.util.Comparator;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.incubator.theia.ui.core.data.provider.TickFrequencyDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractSelectTreeViewer2;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeColumnDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeColumnData;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeViewerEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.linechart.TmfFilteredXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.linechart.TmfXYChartSettings;
import org.eclipse.tracecompass.tmf.ui.views.xychart.TmfChartView;

import com.google.common.collect.ImmutableList;

public class TickFrequencyXYChart extends TmfChartView{
	
	 /** View ID. */
   public static final String ID = "org.eclipse.tracecompass.incubator.theia.ui.views.freq.dataprovider.xyview"; //$NON-NLS-1$

   /**
    * Constructor
    */
   public TickFrequencyXYChart() {
       super("Event Loop Tick Latency"); //$NON-NLS-1$
   }

   @Override
   protected TmfXYChartViewer createChartViewer(@Nullable Composite parent) {
       TmfXYChartSettings settings = new TmfXYChartSettings(null, null, null, 1);
       return new TmfFilteredXYChartViewer(parent, settings, TickFrequencyDataProvider.ID);
   }

   private static final class TreeXyViewer extends AbstractSelectTreeViewer2 {

       public TreeXyViewer(Composite parent) {
           super(parent, 1, TickFrequencyDataProvider.ID);
       }

       @Override
       protected ITmfTreeColumnDataProvider getColumnDataProvider() {
           return () -> ImmutableList.of(createColumn("Name", Comparator.comparing(TmfTreeViewerEntry::getName)), //$NON-NLS-1$
                   new TmfTreeColumnData("Legend")); //$NON-NLS-1$
       }
   }

   @Override
   protected @NonNull TmfViewer createLeftChildViewer(@Nullable Composite parent) {
       return new TreeXyViewer(Objects.requireNonNull(parent));
   }

}
