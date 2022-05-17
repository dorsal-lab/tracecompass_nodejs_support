package org.eclipse.tracecompass.incubator.theia.ui.views.data.provider;

import java.util.Comparator;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.incubator.theia.ui.core.data.provider.TheiaBackendXYDataProvider;
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

/**
 * An example of a data provider XY view
 *
 * This class is also in the developer documentation of Trace Compass. If it is
 * modified here, the doc should also be updated.
 *
 * @author Geneviève Bastien
 */
public class TheiaBackendXYDataProviderView extends TmfChartView {

    /** View ID. */
    public static final String ID = "org.eclipse.tracecompass.incubator.theia.ui.backend.dataprovider.xyview"; //$NON-NLS-1$

    /**
     * Constructor
     */
    public TheiaBackendXYDataProviderView() {
        super("Example Tree XY View"); //$NON-NLS-1$
    }

    @Override
    protected TmfXYChartViewer createChartViewer(@Nullable Composite parent) {
        TmfXYChartSettings settings = new TmfXYChartSettings(null, null, null, 1);
        return new TmfFilteredXYChartViewer(parent, settings, TheiaBackendXYDataProvider.ID);
    }

    private static final class TreeXyViewer extends AbstractSelectTreeViewer2 {

        public TreeXyViewer(Composite parent) {
            super(parent, 1, TheiaBackendXYDataProvider.ID);
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
