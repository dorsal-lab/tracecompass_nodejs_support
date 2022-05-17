package org.eclipse.tracecompass.incubator.theia.ui.core.analysis;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;

/**
 * An example of a simple state system analysis module.
 *
 * This module is also in the developer documentation of Trace Compass. If it is
 * modified here, the doc should also be updated.
 *
 * @author Geneviève Bastien
 */
public class TheiaStateSystemAnalysisModule extends TmfStateSystemAnalysisModule {

    /**
     * Module ID
     */
    public static final String ID = "org.eclipse.tracecompass.incubator.theia.ui.state.system.module"; //$NON-NLS-1$

    @Override
    protected @NonNull ITmfStateProvider createStateProvider() {
        return new TheiaStateProvider(Objects.requireNonNull(getTrace()));
    }

}
