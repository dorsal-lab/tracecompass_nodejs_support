package org.eclipse.tracecompass.incubator.theia.ui.views;

import org.eclipse.tracecompass.incubator.theia.ui.core.data.provider.TheiaTimeGraphDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.ui.widgets.timegraph.BaseDataProviderTimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.BaseDataProviderTimeGraphView;

public class HybridTimeGraphView extends BaseDataProviderTimeGraphView{
	
	 /** View ID. */
    public static final String ID = "org.eclipse.tracecompass.incubator.theia.ui.dataprovider.hybrid"; //$NON-NLS-1$

    /**
     * Default constructor
     */
    public HybridTimeGraphView() {
       super(ID, new BaseDataProviderTimeGraphPresentationProvider(), HybridDataProvider.ID);
    }

}
