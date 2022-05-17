package org.eclipse.tracecompass.incubator.theia.ui.core.data.provider;

import org.eclipse.tracecompass.internal.provisional.tmf.ui.widgets.timegraph.BaseDataProviderTimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.BaseDataProviderTimeGraphView;

/**
 * An example of a data provider time graph view
 *
 * This class is also in the developer documentation of Trace Compass. If it is
 * modified here, the doc should also be updated.
 *
 * @author Geneviève Bastien
 */
@SuppressWarnings("restriction")
public class TheiaTimeGraphDataProviderView extends BaseDataProviderTimeGraphView {

    /** View ID. */
    public static final String ID = "org.eclipse.tracecompass.incubator.theia.ui.dataprovider.tgview"; //$NON-NLS-1$

    /**
     * Default constructor
     */
    public TheiaTimeGraphDataProviderView() {
       super(ID, new BaseDataProviderTimeGraphPresentationProvider(), TheiaTimeGraphDataProvider.ID);
    }

}