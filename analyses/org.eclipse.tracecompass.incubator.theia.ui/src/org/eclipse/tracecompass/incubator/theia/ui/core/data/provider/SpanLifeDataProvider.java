/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.theia.ui.core.data.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.incubator.internal.opentracing.core.Activator;
import org.eclipse.tracecompass.incubator.internal.opentracing.core.analysis.spanlife.SpanLifeAnalysis;
import org.eclipse.tracecompass.incubator.internal.opentracing.core.analysis.spanlife.SpanLifeEntryModel;
import org.eclipse.tracecompass.incubator.internal.opentracing.core.analysis.spanlife.SpanLifeEntryModel.LogEvent;
import org.eclipse.tracecompass.incubator.internal.opentracing.core.analysis.spanlife.SpanLifeStateProvider;
import org.eclipse.tracecompass.incubator.internal.opentracing.core.event.IOpenTracingConstants;
import org.eclipse.tracecompass.incubator.theia.ui.core.analysis.TheiaStateSystemAnalysisModule;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.Annotation;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.AnnotationCategoriesModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.AnnotationModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.IOutputAnnotationProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.internal.tmf.core.model.timegraph.AbstractTimeGraphDataProvider;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils.QuarkIterator;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.dataprovider.X11ColorUtils;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.IOutputStyleProvider;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.OutputStyleModel;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties.SymbolType;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphArrow;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;

import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

/**
 * Data provider that will show the object lifespans.
 *
 * @author Katherine Nadeau
 *
 */
@SuppressWarnings("restriction")
public class SpanLifeDataProvider extends AbstractTimeGraphDataProvider<@NonNull TheiaStateSystemAnalysisModule, @NonNull TimeGraphEntryModel> implements IOutputStyleProvider {

    /** The data provider ID */
    public static String ID = "org.eclipse.tracecompass.incubator.theia.ui.timegraph.async.dataprovider"; //$NON-NLS-1$

    private static final int MARKER_SIZE = 500;

    private static final String ERROR = "error"; //$NON-NLS-1$
    private static final String EVENT = "event"; //$NON-NLS-1$
    private static final String MESSAGE = "message"; //$NON-NLS-1$
    private static final String STACK = "stack"; //$NON-NLS-1$
    private static final String OTHER = "other"; //$NON-NLS-1$
    
    //public static final String ID = "org.eclipse.tracecompass.incubator.theia.ui.timegraph.dataprovider"; //$NON-NLS-1$
    private static final AtomicLong sfAtomicId = new AtomicLong();
    private static final String STYLE0_NAME = "style0"; //$NON-NLS-1$
    private static final String STYLE1_NAME = "style1"; //$NON-NLS-1$
    private static final String STYLE2_NAME = "style2"; //$NON-NLS-1$
    private static final String STYLE3_NAME = "style3"; 
    private static final String STYLE4_NAME = "style4"; 
    private static final String STYLE5_NAME = "style5"; 
    private static final String STYLE6_NAME = "style6"; 
    
    /* The map of basic styles */
    private static final Map<String, OutputElementStyle> STATE_MAP;
    /*
     * A map of styles names to a style that has the basic style as parent, to
     * avoid returning complete styles for each state
     */
    private static final Map<String, OutputElementStyle> STYLE_MAP;
    
    
    static {
        /* Build three different styles to use as examples */
        ImmutableMap.Builder<String, OutputElementStyle> builder = new ImmutableMap.Builder<>();

        builder.put(STYLE0_NAME, new OutputElementStyle(null, ImmutableMap.of(StyleProperties.STYLE_NAME, STYLE0_NAME,
                StyleProperties.BACKGROUND_COLOR, String.valueOf(X11ColorUtils.toHexColor("green")), //$NON-NLS-1$
                StyleProperties.HEIGHT, 1.0f,
                StyleProperties.OPACITY, 1.0f)));
        builder.put(STYLE1_NAME, new OutputElementStyle(null, ImmutableMap.of(StyleProperties.STYLE_NAME, STYLE1_NAME,
                StyleProperties.BACKGROUND_COLOR, String.valueOf(X11ColorUtils.toHexColor("magenta")), //$NON-NLS-1$
                StyleProperties.HEIGHT, 1.0f,
                StyleProperties.OPACITY, 1.0f)));
        builder.put(STYLE2_NAME, new OutputElementStyle(null, ImmutableMap.of(StyleProperties.STYLE_NAME, STYLE2_NAME,
                StyleProperties.BACKGROUND_COLOR, String.valueOf(X11ColorUtils.toHexColor("red")), //$NON-NLS-1$
                StyleProperties.HEIGHT, 1.0f,
                StyleProperties.OPACITY, 1.0f)));
        builder.put(STYLE3_NAME, new OutputElementStyle(null, ImmutableMap.of(StyleProperties.STYLE_NAME, STYLE3_NAME,
                StyleProperties.BACKGROUND_COLOR, String.valueOf(X11ColorUtils.toHexColor("red")), //$NON-NLS-1$
                StyleProperties.HEIGHT, 1.0f,
                StyleProperties.OPACITY, 1.0f)));
        builder.put(STYLE4_NAME, new OutputElementStyle(null, ImmutableMap.of(StyleProperties.STYLE_NAME, STYLE4_NAME,
                StyleProperties.BACKGROUND_COLOR, String.valueOf(X11ColorUtils.toHexColor("orange")), //$NON-NLS-1$
                StyleProperties.HEIGHT, 1.0f,
                StyleProperties.OPACITY, 1.0f)));
        builder.put(STYLE5_NAME, new OutputElementStyle(null, ImmutableMap.of(StyleProperties.STYLE_NAME, STYLE5_NAME,
                StyleProperties.BACKGROUND_COLOR, String.valueOf(X11ColorUtils.toHexColor("blue")), //$NON-NLS-1$
                StyleProperties.HEIGHT, 1.0f,
                StyleProperties.OPACITY, 1.0f)));
        builder.put(STYLE6_NAME, new OutputElementStyle(null, ImmutableMap.of(StyleProperties.STYLE_NAME, STYLE6_NAME,
                StyleProperties.BACKGROUND_COLOR, String.valueOf(X11ColorUtils.toHexColor("cyan")), //$NON-NLS-1$
                StyleProperties.HEIGHT, 1.0f,
                StyleProperties.OPACITY, 1.0f)));
        STATE_MAP = builder.build();

        /* build the style map too */
        builder = new ImmutableMap.Builder<>();
        builder.put(STYLE0_NAME, new OutputElementStyle(STYLE0_NAME));
        builder.put(STYLE1_NAME, new OutputElementStyle(STYLE1_NAME));
        builder.put(STYLE2_NAME, new OutputElementStyle(STYLE2_NAME));
        builder.put(STYLE3_NAME, new OutputElementStyle(STYLE3_NAME));
        builder.put(STYLE4_NAME, new OutputElementStyle(STYLE4_NAME));
        builder.put(STYLE5_NAME, new OutputElementStyle(STYLE5_NAME));
        builder.put(STYLE6_NAME, new OutputElementStyle(STYLE6_NAME));
        STYLE_MAP = builder.build();
    }


    /**
     * Suffix for dataprovider ID
     */
    public static final String SUFFIX = ".dataprovider"; //$NON-NLS-1$

    /**
     * Constructor
     *
     * @param trace
     *            the trace this provider represents
     * @param analysisModule
     *            the analysis encapsulated by this provider
     */
    public SpanLifeDataProvider(ITmfTrace trace, TheiaStateSystemAnalysisModule analysisModule) {
        super(trace, analysisModule);
    }

    @Override
    public @NonNull TmfModelResponse<@NonNull List<@NonNull ITimeGraphArrow>> fetchArrows(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        return new TmfModelResponse<>(null, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    @Override
    public @NonNull TmfModelResponse<@NonNull Map<@NonNull String, @NonNull String>> fetchTooltip(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        ITmfStateSystem ss = getAnalysisModule().getStateSystem();
        SelectionTimeQueryFilter filter = FetchParametersUtils.createSelectionTimeQuery(fetchParameters);
        if (filter == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.INCORRECT_QUERY_PARAMETERS);
        }
        Map<@NonNull Long, @NonNull Integer> entries = getSelectedEntries(filter);
        Collection<@NonNull Integer> quarks = entries.values();
        long startTime = filter.getStart();
        long hoverTime = startTime;
        if (filter.getTimesRequested().length > 1) {
            hoverTime = filter.getTimesRequested()[1];
        }
        long endTime = filter.getEnd();
        if (ss == null || quarks.size() != 1 || !getAnalysisModule().isQueryable(hoverTime)) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }

        int traceLogsQuark = ITmfStateSystem.INVALID_ATTRIBUTE;
        try {
            String traceId = ss.getFullAttributePathArray(quarks.iterator().next())[0];
            int quark_abs= ss.getQuarkAbsolute("THEIA");
            traceLogsQuark = ss.getQuarkRelative(quark_abs, "spans");
        } catch (AttributeNotFoundException e) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
        }

        int spanLogQuark = getLogQuark(ss, ss.getAttributeName(quarks.iterator().next()), ss.getSubAttributes(traceLogsQuark, false));

        try {
            Map<@NonNull String, @NonNull String> retMap = new HashMap<>();
            if (spanLogQuark != ITmfStateSystem.INVALID_ATTRIBUTE) {
                Long ssStartTime = startTime == Long.MIN_VALUE ? ss.getStartTime() : startTime;
                Long ssEndTime = endTime == Long.MIN_VALUE ? ss.getCurrentEndTime() : endTime;
                Long deviationAccepted = (ssEndTime - ssStartTime) / MARKER_SIZE;
                for (ITmfStateInterval state : ss.query2D(Collections.singletonList(spanLogQuark), Math.max(hoverTime - deviationAccepted, ssStartTime), Math.min(hoverTime + deviationAccepted, ssEndTime))) {
                    Object object = state.getValue();
                    if (object instanceof String) {
                        String logs = (String) object;
                        String timestamp = TmfTimestamp.fromNanos(state.getStartTime()).toString();
                        if (timestamp != null) {
                            retMap.put("log timestamp", timestamp); //$NON-NLS-1$
                        }
                        String[] fields = logs.split("~"); //$NON-NLS-1$
                        for (String field : fields) {
                            retMap.put(field.substring(0, field.indexOf(':')), field.substring(field.indexOf(':') + 1));
                        }
                        return new TmfModelResponse<>(retMap, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
                    }
                }
            }
            return new TmfModelResponse<>(retMap, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        } catch (StateSystemDisposedException e) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
        }
    }

    @Override
    public @NonNull String getId() {
        return ID;
    }

    @Override
    protected @Nullable TimeGraphModel getRowModel(@NonNull ITmfStateSystem ss, Map<String, Object> parameters, @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {
        TreeMultimap<Integer, ITmfStateInterval> intervals = TreeMultimap.create(Comparator.naturalOrder(),
                Comparator.comparing(ITmfStateInterval::getStartTime));
        SelectionTimeQueryFilter filter = FetchParametersUtils.createSelectionTimeQuery(parameters);
        Map<@NonNull Long, @NonNull Integer> entries = getSelectedEntries(filter);
        Collection<Long> times = getTimes(filter, ss.getStartTime(), ss.getCurrentEndTime());
        /* Do the actual query */
        for (ITmfStateInterval interval : ss.query2D(entries.values(), times)) {
            if (monitor != null && monitor.isCanceled()) {
                return new TimeGraphModel(Collections.emptyList());
            }
            intervals.put(interval.getAttribute(), interval);
        }
        Map<@NonNull Integer, @NonNull Predicate<@NonNull Multimap<@NonNull String, @NonNull Object>>> predicates = new HashMap<>();
        Multimap<@NonNull Integer, @NonNull String> regexesMap = DataProviderParameterUtils.extractRegexFilter(parameters);
        if (regexesMap != null) {
            predicates.putAll(computeRegexPredicate(regexesMap));
        }
        List<@NonNull ITimeGraphRowModel> rows = new ArrayList<>();
        for (Map.Entry<@NonNull Long, @NonNull Integer> entry : entries.entrySet()) {
            if (monitor != null && monitor.isCanceled()) {
                return new TimeGraphModel(Collections.emptyList());
            }

            List<ITimeGraphState> eventList = new ArrayList<>();
            for (ITmfStateInterval interval : intervals.get(entry.getValue())) {
                long startTime = interval.getStartTime();
                long duration = interval.getEndTime() - startTime + 1;
                Object state = interval.getValue();
                String styleName;
                if(String.valueOf(state).startsWith("fs")) {
                	
                		styleName = "style1"; //$NON-NLS-1$ 
                	}
                else
                	styleName = "style2";
                
                TimeGraphState value = state == null ? new TimeGraphState(startTime, duration, Integer.MIN_VALUE) : new TimeGraphState(startTime, duration, String.valueOf(state), STYLE_MAP.get(styleName));
                applyFilterAndAddState(eventList, value, entry.getKey(), predicates, monitor);
            }
            rows.add(new TimeGraphRowModel(entry.getKey(), eventList));

        }
        return new TimeGraphModel(rows);
    }

    @Override
    protected boolean isCacheable() {
        return true;
    }

    @Override
    protected @NonNull TmfTreeModel<@NonNull TimeGraphEntryModel> getTree(@NonNull ITmfStateSystem ss, Map<String, Object> parameters, @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {
        Builder<@NonNull TimeGraphEntryModel> builder = new Builder<>();
        long rootId = getId(ITmfStateSystem.ROOT_ATTRIBUTE);
        builder.add(new TimeGraphEntryModel(rootId, -1, Collections.singletonList(String.valueOf(getTrace().getName())), ss.getStartTime(), ss.getCurrentEndTime()));
        
        for (int traceQuark : ss.getSubAttributes(ITmfStateSystem.ROOT_ATTRIBUTE, false)) {
            addTrace(ss, builder, traceQuark, rootId);
        }

        return new TmfTreeModel<>(Collections.emptyList(), builder.build());
    }

    private void addTrace(ITmfStateSystem ss, Builder<@NonNull TimeGraphEntryModel> builder, int quark, long parentId) {
        List<@NonNull Integer> logsQuarks;
        try {
            int logsQuark = ss.getQuarkRelative(quark, "spans");
            logsQuarks = ss.getSubAttributes(logsQuark, false);
        } catch (AttributeNotFoundException e) {
            logsQuarks = new ArrayList<>();
        }

        int openTracingSpansQuark;
        try {
            openTracingSpansQuark = ss.getQuarkRelative(quark, "nodejs", "requests");
        } catch (AttributeNotFoundException e) {
            return;
        }

        long traceQuarkId = getId(quark);
        builder.add(new TimeGraphEntryModel(traceQuarkId, parentId, Collections.singletonList(ss.getAttributeName(quark)), ss.getStartTime(), ss.getCurrentEndTime()));
        String h = ss.getAttributeName(quark);
        int ustSpansQuark;
        try {
            ustSpansQuark = ss.getQuarkRelative(quark, SpanLifeStateProvider.UST_ATTRIBUTE);
        } catch (AttributeNotFoundException e) {
            addChildren(ss, builder, openTracingSpansQuark, traceQuarkId, logsQuarks);
            return;
        }
        addUstChildren(ss, builder, openTracingSpansQuark, ustSpansQuark, traceQuarkId, logsQuarks);
    }

    private void addChildren(ITmfStateSystem ss, Builder<@NonNull TimeGraphEntryModel> builder, int quark, long parentId, List<Integer> logsQuarks) {
        for (Integer child : ss.getSubAttributes(quark, false)) {
            long childId = getId(child);
            String childName = ss.getAttributeName(child);
            
            if (!childName.equals(IOpenTracingConstants.LOGS)) {
                List<LogEvent> logs = new ArrayList<>();
                int logQuark = getLogQuark(ss, childName, logsQuarks);
                try {
                    for (ITmfStateInterval interval : ss.query2D(Collections.singletonList(logQuark), ss.getStartTime(), ss.getCurrentEndTime())) {
                        Object value = interval.getValue();
                        if (value != null) {
                            logs.add(new LogEvent(interval.getStartTime(), getLogType(String.valueOf(value))));
                        }
                    }
                } catch (IndexOutOfBoundsException | TimeRangeException | StateSystemDisposedException e) {
                }
                //builder.add(new SpanLifeEntryModel(childId, parentId, Collections.singletonList(getSpanName(childName)), ss.getStartTime(), ss.getCurrentEndTime(), logs, getErrorTag(childName), getProcessName(childName)));
                builder.add(new TimeGraphEntryModel(childId, parentId, Collections.singletonList(childName), ss.getStartTime(), ss.getCurrentEndTime()));

                addChildren(ss, builder, child, childId, logsQuarks);
            }
        }
    }

    private void addUstChildren(ITmfStateSystem ss, Builder<@NonNull TimeGraphEntryModel> builder, int openTracingQuark, int ustQuark, long parentId, List<Integer> logsQuarks) {
        for (Integer child : ss.getSubAttributes(openTracingQuark, false)) {
            String childName = ss.getAttributeName(child);

            List<LogEvent> logs = new ArrayList<>();
            int logQuark = getLogQuark(ss, childName, logsQuarks);
            try {
                for (ITmfStateInterval interval : ss.query2D(Collections.singletonList(logQuark), ss.getStartTime(), ss.getCurrentEndTime())) {
                    if (!interval.getStateValue().isNull()) {
                        logs.add(new LogEvent(interval.getStartTime(), getLogType(String.valueOf(interval.getValue()))));
                    }
                }
            } catch (IndexOutOfBoundsException | TimeRangeException | StateSystemDisposedException e) {
            }

            String spanId = getSpanId(childName);

            int ustSpan;
            try {
                ustSpan = ss.getQuarkRelative(ustQuark, spanId);
            } catch (AttributeNotFoundException e) {
                return;
            }
            long childId = getId(ustSpan);
            builder.add(new SpanLifeEntryModel(childId, parentId, Collections.singletonList(childName), ss.getStartTime(), ss.getCurrentEndTime(), logs, getErrorTag(childName), getProcessName(childName)));
            addUstChildren(ss, builder, child, ustQuark, childId, logsQuarks);
        }
    }

    private static int getLogQuark(ITmfStateSystem ss, String spanName, List<Integer> logsQuarks) {
        for (int logsQuark : logsQuarks) {
            if (ss.getAttributeName(logsQuark).equals(getSpanId(spanName))) {
            	int x = 3;
                return logsQuark;
            }
        }
        return ITmfStateSystem.INVALID_ATTRIBUTE;
    }

    private static String getSpanName(String attributeName) {
        int slashPos = attributeName.lastIndexOf('/');
        slashPos = attributeName.lastIndexOf('/', slashPos);
        slashPos = attributeName.lastIndexOf('/', slashPos);
        return attributeName.substring(0, Math.max(0, slashPos));
    }

    private static String getSpanId(String attributeName) {
        //String[] attributeInfo = attributeName.split("/"); //$NON-NLS-1$
        return attributeName;
    }

    private static Boolean getErrorTag(String attributeName) {
        return false;
    }

    private static String getProcessName(String attributeName) {
        return attributeName.substring(attributeName.lastIndexOf('/') + 1);
    }

    private static String getLogType(String logs) {
        String[] logsArray = logs.split("~"); //$NON-NLS-1$
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < logsArray.length; i++) {
            builder.append(logsArray[i].substring(0, logsArray[i].indexOf(':')));
        }
        String types = builder.toString();

        if (types.contains(ERROR)) {
            return ERROR;
        } else if (types.contains(EVENT)) {
            return EVENT;
        } else if (types.contains(MESSAGE)) {
            return MESSAGE;
        } else if (types.contains(STACK)) {
            return STACK;
        } else {
            return OTHER;
        }
    }
    @Override
    public TmfModelResponse<OutputStyleModel> fetchStyle(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        return new TmfModelResponse<>(new OutputStyleModel(STATE_MAP), Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }


//    @Override
//    public TmfModelResponse<AnnotationCategoriesModel> fetchAnnotationCategories(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
//        ITmfStateSystem ss = getAnalysisModule().getStateSystem();
//        if (ss == null) {
//            return new TmfModelResponse<>(null, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
//        }
//        List<Integer> quarks = ss.getQuarks("*", IOpenTracingConstants.LOGS); //$NON-NLS-1$
//        if (quarks.isEmpty()) {
//            return new TmfModelResponse<>(null, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
//        }
//        return new TmfModelResponse<>(new AnnotationCategoriesModel(Arrays.asList(IOpenTracingConstants.LOGS)), Status.COMPLETED, IOpenTracingConstants.LOGS);
//    }
//
//    @Override
//    public TmfModelResponse<AnnotationModel> fetchAnnotations(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
//        ITmfStateSystem ss = getAnalysisModule().getStateSystem();
//        SelectionTimeQueryFilter filter = FetchParametersUtils.createSelectionTimeQuery(fetchParameters);
//        if (filter == null) {
//            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.INCORRECT_QUERY_PARAMETERS);
//        }
//
//        @Nullable Set<@NonNull String> selectedCategories = DataProviderParameterUtils.extractSelectedCategories(fetchParameters);
//        if ((selectedCategories != null && !selectedCategories.contains(IOpenTracingConstants.LOGS))) {
//            return new TmfModelResponse<>(null, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
//        }
//
//        Map<Long, Integer> entries = getSelectedEntries(filter);
//        Collection<@NonNull Integer> quarks = entries.values();
//        if (ss == null || quarks.isEmpty()) {
//            return new TmfModelResponse<>(null, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
//        }
//        Map<Integer, Long> lookup = new HashMap<>();
//        for (Entry<Long, Integer> entry : entries.entrySet()) {
//            lookup.put(entry.getValue(), entry.getKey());
//        }
//
//        int traceQuark = ITmfStateSystem.INVALID_ATTRIBUTE;
//        int traceLogsQuark = ITmfStateSystem.INVALID_ATTRIBUTE;
//        try {
//            String traceId = ss.getFullAttributePathArray(quarks.iterator().next())[0];
//            traceQuark = ss.getQuarkAbsolute(traceId);
//            traceLogsQuark = ss.getQuarkRelative(traceQuark, IOpenTracingConstants.LOGS);
//        } catch (AttributeNotFoundException e) {
//            return new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
//        }
//
//        Map<Integer, Long> spanLookup = new HashMap<>();
//        List<Integer> spanLogQuarks = new ArrayList<>();
//        quarks.remove(traceQuark);
//        for (int quark : quarks) {
//            int spanLogQuark = getLogQuark(ss, ss.getAttributeName(quark), ss.getSubAttributes(traceLogsQuark, false));
//            if (spanLogQuark != ITmfStateSystem.INVALID_ATTRIBUTE) {
//                spanLogQuarks.add(spanLogQuark);
//                Long value = lookup.get(quark);
//                spanLookup.put(spanLogQuark, Objects.requireNonNull(value));
//            }
//        }
//        if (spanLogQuarks.isEmpty()) {
//            return new TmfModelResponse<>(null, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
//        }
//        List<Annotation> annotations = new ArrayList<>();
//        OutputElementStyle style = new OutputElementStyle(null, ImmutableMap.of(
//                StyleProperties.COLOR, "#7f0000", //$NON-NLS-1$
//                StyleProperties.VERTICAL_ALIGN, "top", //$NON-NLS-1$
//                StyleProperties.HEIGHT, 0.5f,
//                StyleProperties.SYMBOL_TYPE, SymbolType.INVERTED_TRIANGLE));
//        try {
//            long[] timesRequested = filter.getTimesRequested();
//            for (int logQuark : spanLogQuarks) {
//                QuarkIterator iter = new StateSystemUtils.QuarkIterator(ss, logQuark, timesRequested[0], timesRequested[timesRequested.length - 1]);
//                while (iter.hasNext()) {
//                    ITmfStateInterval interval = iter.next();
//                    if (interval.getValue() != null) {
//                        Long entryId = spanLookup.get(interval.getAttribute());
//                        if (entryId == null) {
//                            entryId = -1L;
//                        }
//                        annotations.add(new Annotation(interval.getStartTime(), interval.getEndTime() - interval.getStartTime(), entryId, "", style));
//                    }
//                }
//            }
//        } catch (IndexOutOfBoundsException | TimeRangeException e) {
//            Activator.getInstance().logError(e.getMessage());
//        }
//        return new TmfModelResponse<>(new AnnotationModel(Collections.singletonMap(IOpenTracingConstants.LOGS, annotations)), Status.COMPLETED, "");
//    }

}