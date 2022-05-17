package org.eclipse.tracecompass.incubator.theia.ui.core.data.provider;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.incubator.theia.ui.core.analysis.TheiaStateSystemAnalysisModule;
import org.eclipse.tracecompass.internal.tmf.core.model.DataProviderDescriptor;
import org.eclipse.tracecompass.internal.tmf.core.model.xy.TmfTreeXYCompositeDataProvider;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor.ProviderType;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

public class TickFrequencyFactory implements IDataProviderFactory {
	
	   private static final IDataProviderDescriptor DESCRIPTOR = new DataProviderDescriptor.Builder()
	            .setId(TickFrequencyDataProvider.ID)
	            .setName("Event Loop Frequency") //$NON-NLS-1$
	            .setDescription("Thi") //$NON-NLS-1$
	            .setProviderType(ProviderType.TREE_TIME_XY)
	            .build();

	    @Override
	    public @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> createProvider(ITmfTrace trace) {
	        Collection<@NonNull ITmfTrace> traces = TmfTraceManager.getTraceSet(trace);
	        if (traces.size() == 1) {
	            return TickFrequencyDataProvider.create(trace);
	        }
	        return TmfTreeXYCompositeDataProvider.create(traces, "Event Loop freq Factory XY Chart", TickFrequencyDataProvider.ID); //$NON-NLS-1$
	    }

	    @Override
	    public Collection<IDataProviderDescriptor> getDescriptors(@NonNull ITmfTrace trace) {
	        TheiaStateSystemAnalysisModule module = TmfTraceUtils.getAnalysisModuleOfClass(trace, TheiaStateSystemAnalysisModule.class, TheiaStateSystemAnalysisModule.ID);
	        return module != null ? Collections.singletonList(DESCRIPTOR) : Collections.emptyList();
	    }

}
