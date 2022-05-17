package org.eclipse.tracecompass.incubator.theia.ui.core.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.LinuxPidAspect;
import org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.LinuxTidAspect;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * An example of a simple state provider for a simple state system analysis
 *
 * This module is also in the developer documentation of Trace Compass. If it is
 * modified here, the doc should also be updated.
 *
 * @author Alexandre Montplaisir
 * @author Geneviève Bastien
 */
public class TheiaStateProvider extends AbstractTmfStateProvider {

	private static final @NonNull String PROVIDER_ID = "org.eclipse.tracecompass.incubator.theia.ui.state.provider"; //$NON-NLS-1$
	private static final int VERSION = 0;

	public static final String OPEN_TRACING_ATTRIBUTE = "openTracingSpans"; //$NON-NLS-1$

	public static final String NODE_ATTRIBUTE = "nodejs";
	public static final String REQUEST_ATTRIBUTE = "requests";
	public static final String RPC_ATTRIBUTE = "RPC";

	/**
	 * Quark name for ust spans
	 */
	public static final String UST_ATTRIBUTE = "ustSpans"; //$NON-NLS-1$

	Integer id_kept;
	Integer ch_kept;
	Integer nexttick=0;
	Integer microtask=0;
	int mutex = 0;
	private String type_freq="";
	private long start_time;
	private Map<String, Integer> timer_tr = new HashMap<String, Integer>();
	private Map<String, Long> timer_latency = new HashMap<String, Long>();
	private Map<String, Long> worker_latency = new HashMap<String, Long>();
	private Map<String, String> worker_tid = new HashMap<String, String>();
	private  Map<String, Integer> thread_tr = new HashMap<String, Integer>();
	private  Map<String, Long> tick_ts = new HashMap<String, Long>();
	private  Map<String, String> cont_async = new HashMap<String, String>();
	private  Map<String, String> cont_async2 = new HashMap<String, String>();
	private  Map<String, Long> promise = new HashMap<String, Long>();

	// private static Map<String, Integer> fReqs= new HashMap<String, Integer>();
	private  Map<String, Map<String, Integer>> fReqs = new HashMap<>();
	private  Map<String, Integer> async_capability = new HashMap<String, Integer>();
	private  Map<String, Integer> context_tid_parent = new HashMap<String, Integer>();
	private  Map<String, Integer> context_tid_status = new HashMap<String, Integer>();
	private  Map<Integer, Integer> context_pid_parent = new HashMap<Integer, Integer>();
	private  Map<Integer, Integer> context_pid_status = new HashMap<Integer, Integer>();
	private  Map<Integer, String> tid_cpu = new HashMap<Integer, String>();
	private  Map<String, Integer> map_nexttick = new HashMap<String, Integer>();
	private  Map<String, Integer> map_microtask = new HashMap<String, Integer>();

	/**
	 * Constructor
	 *
	 * @param trace The trace for this state provider
	 */
	public TheiaStateProvider(@NonNull ITmfTrace trace) {
		super(trace, PROVIDER_ID);

		// fHandlers =
		// fHandlers.put("OpenTracingSpan", this::handleSpan); //$NON-NLS-1$
		// fHandlers.put("jaeger_ust:start_span", this::handleStart); //$NON-NLS-1$
		// fHandlers.put("jaeger_ust:end_span", this::handleEnd); //$NON-NLS-1$

	}

	@Override
	public int getVersion() {
		return VERSION;
	}

	@Override
	public @NonNull ITmfStateProvider getNewInstance() {
		return new TheiaStateProvider(getTrace());
	}

	@Override
	protected void eventHandle(ITmfEvent event) {

		/**
		 * Do what needs to be done with this event, here is an example that updates the
		 * CPU state and TID after a sched_switch
		 *
		 */

		ITmfStateSystemBuilder ss = getStateSystemBuilder();
		// System.out.println(event.getName());

		// ArrayList tab = new ArrayList();

		// System.out.println(event.getContent().getFieldValue(String.class,
		// IOpenTracingConstants.OPERATION_NAME));

		final long ts = event.getTimestamp().getValue();

		String trace_ev = event.getName();
		int q_theia = ss.getQuarkAbsoluteAndAdd("THEIA");
		int comp = 0;
		//Integer cpu = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event);
		//Integer pid = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), LinuxTidAspect.class, event);

		try {
//
//			if (event.getName().equals("uv_provider:uv_workerq_remove_event")) {
//
////				mutex++;
////
////				if (id_kept != null || ch_kept != null) {
////					int q_context_store = ss.getQuarkRelativeAndAdd(q_theia, "Context");
////					int q_context_cpu_store = ss.getQuarkRelativeAndAdd(q_context_store, String.valueOf(cpu));
////					ss.modifyAttribute(ts, "0", q_context_cpu_store);
////
////					int q_cpu_chid_store = ss.getQuarkRelativeAndAdd(q_theia, "cpu_store");
////					int q_chid_store = ss.getQuarkRelativeAndAdd(q_cpu_chid_store, String.valueOf(cpu));
////					int q_chid_id_store = ss.getQuarkRelativeAndAdd(q_chid_store, "id");
////					ss.modifyAttribute(ts, id_kept, q_chid_id_store);
////
////					int q_chid_ch_store = ss.getQuarkRelativeAndAdd(q_chid_store, "ch");
////					ss.modifyAttribute(ts, ch_kept, q_chid_ch_store);
////
////				}
////
////				if (event.getName().equals("uv_provider:uv_write_stream")) {
////
////				}
//
//			}
//
			if (event.getName().equals("uv_provider:uv_exit_out_iopoll_event")) {

				String tid = event.getContent().getFieldValue(String.class, "context._pthread_id");
				int q_phases = ss.getQuarkRelativeAndAdd(q_theia, "Event Loop");
				int q_el_tid = ss.getQuarkRelativeAndAdd(q_phases, tid);
				ss.modifyAttribute(ts, null, q_el_tid);

			}

			if (event.getName().equals("uv_provider:uv_out_iopoll_event")) {

				String tid = event.getContent().getFieldValue(String.class, "context._pthread_id");
				int q_phases = ss.getQuarkRelativeAndAdd(q_theia, "Event Loop");
				int q_el_tid = ss.getQuarkRelativeAndAdd(q_phases, tid);
				ss.modifyAttribute(ts, "I/O Polling Phase", q_el_tid);

			}

			if (event.getName().equals("uv_provider:uv_exit_closinghandle_event")) {

				String tid = event.getContent().getFieldValue(String.class, "context._pthread_id");
				int q_phases = ss.getQuarkRelativeAndAdd(q_theia, "Event Loop");
				int q_el_tid = ss.getQuarkRelativeAndAdd(q_phases, tid);
				ss.modifyAttribute(ts, null, q_el_tid);

			}

			if (event.getName().equals("uv_provider:uv_closinghandle_event")) {

				String tid = event.getContent().getFieldValue(String.class, "context._pthread_id");
				int q_phases = ss.getQuarkRelativeAndAdd(q_theia, "Event Loop");
				int q_el_tid = ss.getQuarkRelativeAndAdd(q_phases, tid);
				ss.modifyAttribute(ts, "Closing handle Phase", q_el_tid);

			}

			if (event.getName().equals("uv_provider:uv_runcheck_event")) {

				String tid = event.getContent().getFieldValue(String.class, "context._pthread_id");
				int q_phases = ss.getQuarkRelativeAndAdd(q_theia, "Event Loop");
				int q_el_tid = ss.getQuarkRelativeAndAdd(q_phases, tid);
				ss.modifyAttribute(ts, "Checking Phase", q_el_tid);

			}

			if (event.getName().equals("uv_provider:uv_exit_runcheck_event")) {

				String tid = event.getContent().getFieldValue(String.class, "context._pthread_id");
				int q_phases = ss.getQuarkRelativeAndAdd(q_theia, "Event Loop");
				int q_el_tid = ss.getQuarkRelativeAndAdd(q_phases, tid);
				ss.modifyAttribute(ts, null, q_el_tid);

			}

			if (event.getName().equals("uv_provider:uv_preparephase_event")) {

				String tid = event.getContent().getFieldValue(String.class, "context._pthread_id");
				int q_phases = ss.getQuarkRelativeAndAdd(q_theia, "Event Loop");
				int q_el_tid = ss.getQuarkRelativeAndAdd(q_phases, tid);
				ss.modifyAttribute(ts, "Prepare Phase", q_el_tid);

			}

			if (event.getName().equals("uv_provider:uv_exit_preparephase_event")) {

				String tid = event.getContent().getFieldValue(String.class, "context._pthread_id");
				int q_phases = ss.getQuarkRelativeAndAdd(q_theia, "Event Loop");
				int q_el_tid = ss.getQuarkRelativeAndAdd(q_phases, tid);
				ss.modifyAttribute(ts, null, q_el_tid);

			}

			if (event.getName().equals("uv_provider:uv_runpending_event")) {

				String tid = event.getContent().getFieldValue(String.class, "context._pthread_id");
				int q_phases = ss.getQuarkRelativeAndAdd(q_theia, "Event Loop");
				int q_el_tid = ss.getQuarkRelativeAndAdd(q_phases, tid);
				ss.modifyAttribute(ts, "Pending Phase", q_el_tid);

			}

			if (event.getName().equals("uv_provider:uv_exit_runpending_event")) {

				String tid = event.getContent().getFieldValue(String.class, "context._pthread_id");
				int q_phases = ss.getQuarkRelativeAndAdd(q_theia, "Event Loop");
				int q_el_tid = ss.getQuarkRelativeAndAdd(q_phases, tid);
				ss.modifyAttribute(ts, null, q_el_tid);

			}

			if (event.getName().equals("uv_provider:uv_exit_timerphase_event")) {

				String tid = event.getContent().getFieldValue(String.class, "context._pthread_id");
				int q_phases = ss.getQuarkRelativeAndAdd(q_theia, "Event Loop");
				int q_el_tid = ss.getQuarkRelativeAndAdd(q_phases, tid);
				ss.modifyAttribute(ts, null, q_el_tid);

			}

			if (event.getName().equals("uv_provider:uv_timerPhase_event")) {

				String tid = event.getContent().getFieldValue(String.class, "context._pthread_id");

				// Building the entry for the event loop phases
				int q_phases = ss.getQuarkRelativeAndAdd(q_theia, "Event Loop");
				int q_el_tid = ss.getQuarkRelativeAndAdd(q_phases, tid);
				ss.modifyAttribute(ts, "Timer Phase", q_el_tid);

				// Building the XY graph entries for timers view
				int q_metrics = ss.getQuarkRelativeAndAdd(q_theia, "Metrics");
				int q_nextick = ss.getQuarkRelativeAndAdd(q_metrics, "Tick_latency");
				int q_freq = ss.getQuarkRelativeAndAdd(q_metrics, "Tick_frequency");
				int q_thread_freq = ss.getQuarkRelativeAndAdd(q_freq, tid);
				int q_thread = ss.getQuarkRelativeAndAdd(q_nextick, tid);
				if (thread_tr.get(tid) == null) {
					start_time = ss.getStartTime();
					thread_tr.put(tid, 0);
				}

				long rts = ts - start_time;

				if (rts <= 1000000000) {
					thread_tr.put(tid, thread_tr.get(tid) + 1);

				} else {

					ss.modifyAttribute(ts, thread_tr.get(tid), q_thread_freq);
					thread_tr.put(tid, 0);
					start_time = ts;

				}

				if (tick_ts.get(tid) == null) {

					// ss.modifyAttribute(ts, tid, q_thread);

					tick_ts.put(tid, event.getTimestamp().toNanos());

				} else {
					// String state = String.valueOf(ss.queryOngoing(q_thread));
					if (tick_ts.get(tid) != null) {
						ss.modifyAttribute(ts, (event.getTimestamp().toNanos() - tick_ts.get(tid)) / 1000000, q_thread);
						// if(!state.equals("null")||!state.equals("nullValue")||state!=null) {
						// long cur_ts= ss.getOngoingStartTime(q_thread);
						// ss.modifyAttribute(ts, (ts-cur_ts)/1000000, q_thread);
						// ss.modifyAttribute(ts, null, q_thread);
						tick_ts.remove(tid);

					}
				}

			}

			if (event.getName().equals("uv_provider:uv_timersq_remove_event")) {

				String tid = event.getContent().getFieldValue(String.class, "context._pthread_id");

				int q_metrics = ss.getQuarkRelativeAndAdd(q_theia, "Metrics");
				int q_timer = ss.getQuarkRelativeAndAdd(q_metrics, "Timers_queue");

				int q_thread_timer = ss.getQuarkRelativeAndAdd(q_timer, tid);

				if (timer_tr.get(tid) != null)
					timer_tr.put(tid, timer_tr.get(tid) - 1);
				else
					timer_tr.put(tid, 0);

				ss.modifyAttribute(ts, timer_tr.get(tid), q_thread_timer);

			}

			if (event.getName().equals("uv_provider:uv_timersq_insert_event")) {
				String id_timer = event.getContent().getFieldValue(String.class, "data");

				timer_latency.put(id_timer, ts);

			}

			if (event.getName().equals("uv_provider:uv_run_timers_event")) {
				String id_timer = event.getContent().getFieldValue(String.class, "backend_fd");
				String tid = event.getContent().getFieldValue(String.class, "context._pthread_id");
				int q_metrics = ss.getQuarkRelativeAndAdd(q_theia, "Metrics");
				int q_timer = ss.getQuarkRelativeAndAdd(q_metrics, "Timers_latency");

				int q_thread_timer = ss.getQuarkRelativeAndAdd(q_timer, tid);
				if (timer_latency.get(id_timer) != null) {

					ss.modifyAttribute(ts, (ts - timer_latency.get(id_timer)) / 1000000, q_thread_timer);
					timer_latency.remove(id_timer);
				}

			}

			if (event.getName().equals("uv_provider:uv_work_submit_event")) {
				String id_worker = event.getContent().getFieldValue(String.class, "backend_fd");
				String tid = event.getContent().getFieldValue(String.class, "context._pthread_id");

				worker_latency.put(id_worker, ts);
				worker_tid.put(id_worker, tid);

			}

			if (event.getName().equals("uv_provider:uv_worker_event")) {
				String id_worker = event.getContent().getFieldValue(String.class, "backend_fd");
				String tid = event.getContent().getFieldValue(String.class, "context._pthread_id");
				int q_metrics = ss.getQuarkRelativeAndAdd(q_theia, "Metrics");
				int q_workers = ss.getQuarkRelativeAndAdd(q_metrics, "Workers_latency");

				if (worker_latency.get(id_worker) != null) {
					int q_thread_worker = ss.getQuarkRelativeAndAdd(q_workers, worker_tid.get(id_worker));
					ss.modifyAttribute(ts, (ts - worker_latency.get(id_worker)) / 1000, q_thread_worker);
					worker_latency.remove(id_worker);
					worker_tid.remove(id_worker);
				}

			}
						
			if (event.getName().equals("uv_provider:uv_workerq_remove_event")) {
				String ptid = event.getContent().getFieldValue(String.class, "add_wq");
				String id = event.getContent().getFieldValue(String.class, "backend_fd");
				int q_async_pid_to_req;
				if(!id.equals("0")) {
				q_async_pid_to_req = ss.getQuarkRelativeAndAdd(q_theia, NODE_ATTRIBUTE, "memory", "async", "ReqPids", ptid);		
				//we save the id of the async request based on the running tied
				ss.modifyAttribute(ts, id, q_async_pid_to_req);
				int q_async_reqid_to_pid = ss.getQuarkRelativeAndAdd(q_theia, NODE_ATTRIBUTE, "memory", "async", "ReqIds_to_pid", id);
				ss.modifyAttribute(ts, ptid, q_async_reqid_to_pid);
				}
				
			}

			if (event.getName().equals("uv_provider:uv_async_file_event")) {
				Integer cpu = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event);
				//Integer pid = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), LinuxTidAspect.class, event);
				String tid = event.getContent().getFieldValue(String.class, "context._pthread_id");
				// String cpu_id=event.getContent().getFieldValue(String.class,
				// "context._cpu_id");
				String id = event.getContent().getFieldValue(String.class, "id");
				String uv_pid = event.getContent().getFieldValue(String.class, "backend_fd");
				Integer pid = Integer.parseInt(uv_pid);
				// Integer pid =
				// TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(),
				// LinuxPidAspect.class, event);
				// System.out.println(pid);
				String oper = event.getContent().getFieldValue(String.class, "oper");

				int q_states = ss.getQuarkRelativeAndAdd(q_theia, "states");
				
				
//				int q_async_reqid_to_pid = ss.getQuarkRelativeAndAdd(q_theia, NODE_ATTRIBUTE, "memory", "async", "ReqIds_to_pid", id);
//				int q_async_pid_to_req = ss.getQuarkRelativeAndAdd(q_theia, NODE_ATTRIBUTE, "memory", "async", "ReqPids", String.valueOf(pid));
//				int q_pid_quarkParent = ss.getQuarkRelativeAndAdd(q_theia, NODE_ATTRIBUTE, "memory", "state", "UvPid_"+id, uv_pid, "Parent");
				
				

				if (oper.equals("exit")) {

					
					if(!id.equals("0")) {
						int q_async_state = ss.getQuarkRelativeAndAdd(q_theia, NODE_ATTRIBUTE, "memory", "async", id, "state");
						ss.modifyAttribute(ts,  "0",q_async_state);
						int q_async_reqid_to_pid = ss.getQuarkRelativeAndAdd(q_theia, NODE_ATTRIBUTE, "memory", "async", "ReqIds_to_pid", id);
						String state = (String) GetStateFromQuark(ss, q_async_reqid_to_pid);
						//if(state!=null)
								//ss.modifyAttribute(ts, null, q_async_reqid_to_pid);	
					}

					int q_spans = ss.getQuarkRelative(q_theia, "spans");

					String state = cont_async.get(id);
					if (!(state == null)) {
						int q_chid = ss.getQuarkRelativeAndAdd(q_spans, state);
						int q_async = ss.getQuarkRelativeAndAdd(q_chid, id);
						ss.modifyAttribute(ts, null, q_async);

						cont_async.remove(id);
					}

				} else {
					
					if(!id.equals("0")) {
						int q_async_state = ss.getQuarkRelativeAndAdd(q_theia, NODE_ATTRIBUTE, "memory", "async", id, "state");
						ss.modifyAttribute(ts,  "1",q_async_state);
						int q_async_tid = ss.getQuarkRelativeAndAdd(q_theia, NODE_ATTRIBUTE, "memory", "async", id, "tid");
						ss.modifyAttribute(ts,  tid,q_async_tid);					
					}

					Integer quarkStatus = context_tid_status.get(tid);

					if (quarkStatus != null && quarkStatus != 0) {

						try {
						Integer quarkParent = context_tid_parent.get(tid);
						if(quarkParent != null) {
						ss.modifyAttribute(ts, oper, quarkParent);
						//ss.modifyAttribute(ts, "1", q_pid_state);
						//ss.modifyAttribute(ts, quarkParent, q_pid_quarkParent);
						
						if(tid_cpu.get(tid)!=null)
							tid_cpu.put(cpu, tid);
						else
							tid_cpu.replace(cpu, tid);
						
						 }
						}
						
						catch(Exception e) {
							
							
						}

					}
					int q_spans = ss.getQuarkRelativeAndAdd(q_theia, "spans");
					int test_cont = ss.optQuarkRelative(q_states, "context_id");
					if (test_cont > 0) {
						int q_cont_id = ss.getQuarkRelativeAndAdd(q_states, "context_id");
						String state = String.valueOf(ss.queryOngoing(q_cont_id));

						if (!state.equals("null")) {
							cont_async.put(id, state);
							int q_chid = ss.getQuarkRelativeAndAdd(q_spans, state);
							int q_async = ss.getQuarkRelativeAndAdd(q_chid, id);
							ss.modifyAttribute(ts, oper, q_async);

							int q_cont_as = ss.getQuarkRelativeAndAdd(q_states, "context_async");
							ss.modifyAttribute(ts, id + " " + state, q_cont_as);
						}
					}
				}
			}

			if (event.getName().startsWith("syscall_entry")) {
				Integer pid = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), LinuxTidAspect.class, event);
				Integer cpu = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event);
				
				
				
				
                if(ss.optQuarkRelative(q_theia, NODE_ATTRIBUTE, "memory", "async", "ReqPids", String.valueOf(pid))>0) {
                	
                	int q_async_pid_to_req = ss.getQuarkRelativeAndAdd(q_theia, NODE_ATTRIBUTE, "memory", "async", "ReqPids", String.valueOf(pid));
                	String state = (String) GetStateFromQuark(ss, q_async_pid_to_req);
                	
                	if(ss.optQuarkRelative(q_theia, NODE_ATTRIBUTE, "memory", "async", state, "state")>0) {
                		
                		int q_st = ss.getQuarkRelative(q_theia, NODE_ATTRIBUTE, "memory", "async", state, "state");
                		String req_state=(String) GetStateFromQuark(ss,q_st);
                		if(req_state.equals("1")) {
                			
                			if(ss.optQuarkRelative(q_theia, NODE_ATTRIBUTE, "memory", "async", state, "tid")>0) {
                				
                				int q_state_tid = ss.getQuarkRelative(q_theia, NODE_ATTRIBUTE, "memory", "async", state, "tid");
                				String state_tid = (String) GetStateFromQuark(ss,q_state_tid);
                				Integer quarkParent = context_tid_parent.get(state_tid);
                				if(quarkParent!=null) {
                					
                					int q_sys = ss.getQuarkRelativeAndAdd(quarkParent, ss.getAttributeName(quarkParent)+"_syscall");
                					ss.modifyAttribute(ts, event.getName(), q_sys);
                				}
                			}
                		}
                		
                	}
                	
                }
				
				
				//Integer quarkStatus = context_tid_status.get(tid);
				//System.out.println(context_tid_parent.get(tid));

//				if (quarkStatus != null && quarkStatus != 0) {
//					
//					Integer quarkParent = context_tid_parent.get(tid);
//					
//					if(quarkParent != null)
//						ss.modifyAttribute(ts, event.getName(), quarkParent);
//					
//				
//				}

			}

			if (event.getName().startsWith("syscall_exit")) {
				Integer pid = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), LinuxTidAspect.class, event);
				Integer cpu = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event);
				// System.out.println(pid);
				
				
				
                if(ss.optQuarkRelative(q_theia, NODE_ATTRIBUTE, "memory", "async", "ReqPids", String.valueOf(pid))>0) {
                	
                	int q_async_pid_to_req = ss.getQuarkRelativeAndAdd(q_theia, NODE_ATTRIBUTE, "memory", "async", "ReqPids", String.valueOf(pid));
                	String state = (String) GetStateFromQuark(ss, q_async_pid_to_req);
                	
                	if(ss.optQuarkRelative(q_theia, NODE_ATTRIBUTE, "memory", "async", state, "state")>0) {
                		
                		int q_st = ss.getQuarkRelative(q_theia, NODE_ATTRIBUTE, "memory", "async", state, "state");
                		String req_state=(String) GetStateFromQuark(ss,q_st);
                		if(req_state.equals("1")) {
                			
                			if(ss.optQuarkRelative(q_theia, NODE_ATTRIBUTE, "memory", "async", state, "tid")>0) {
                				int q_state_tid = ss.getQuarkRelative(q_theia, NODE_ATTRIBUTE, "memory", "async", state, "tid");
                				String state_tid = (String) GetStateFromQuark(ss,q_state_tid);
                				Integer quarkParent = context_tid_parent.get(state_tid);
                				if(quarkParent!=null) {
                					System.out.println(ss.getAttributeName(quarkParent)+"_"+state_tid+"_"+pid);
                					int q_sys = ss.getQuarkRelativeAndAdd(quarkParent, ss.getAttributeName(quarkParent)+"_syscall");
                					ss.modifyAttribute(ts, null, q_sys);
                				}
                			}
                		}
                		
                	}
                	
                }
				

			}

			if (trace_ev.equals("uv_provider:uv_send_event")) {

				String met = event.getContent().getFieldValue(String.class, "method");
				String seq = event.getContent().getFieldValue(String.class, "context.packet_seq_num");
				String ch = event.getContent().getFieldValue(String.class, "channel");
				// String cpu_id=event.getContent().getFieldValue(String.class,
				// "context._cpu_id");

				String tid = event.getContent().getFieldValue(String.class, "context._pthread_id");
				String id = event.getContent().getFieldValue(String.class, "id");

				if (!(met.startsWith("ELDHISTOGRAM") || met.startsWith("PerformanceObserver")
						|| met.startsWith("DNSCHANNEL") || met.startsWith("FSEVENTWRAP")
						|| met.startsWith("GETADDRINFOREQWRAP") || met.startsWith("GETNAMEINFOREQWRAP")
						|| met.startsWith("HTTPCLIENTREQUEST")
						|| met.startsWith("JSSTREAM") || met.startsWith("PIPECONNECTWRAP") || met.startsWith("PIPEWRAP")
						|| met.startsWith("PROCESSWRAP") || met.startsWith("QUERYWRAP")
						|| met.startsWith("SHUTDOWNWRAP") || met.startsWith("SIGNALWRAP")
						|| met.startsWith("STATWATCHER") || met.startsWith("TCPCONNECTWRAP")
						|| met.startsWith("TCPSERVERWRAP") ||  met.startsWith("TTYWRAP")
						|| met.startsWith("UDPSENDWRAP") || met.startsWith("UDPWRAP") || met.startsWith("WRITEWRAP")
						|| met.startsWith("ZLIB") || met.startsWith("SSLCONNECTION") || met.startsWith("PBKDF2REQUEST")
						|| met.startsWith("RANDOMBYTESREQUEST") || met.startsWith("RANDOMBYTESREQUEST")
						|| met.startsWith("TLSWRAP") || met.startsWith("Timeout")
						|| met.startsWith("Immediate") || met.startsWith("destoy")
						|| met.startsWith("undefined"))) {

					if (met.startsWith("PROMISE")) {

						int q_nodejs = ss.getQuarkRelativeAndAdd(q_theia, "nodejs");
						int test_cont = ss.optQuarkRelative(q_theia, "context_id");
						
						
						int q_promise = ss.getQuarkRelativeAndAdd(q_theia, "Metrics","Promises","states");
						promise.put(id, ts);
						microtask++;
						ss.modifyAttribute(ts, microtask, q_promise);

						Integer quark = async_capability.get(ch + tid);

						if (quark != null) {

							int quarkNode = ss.getQuarkRelativeAndAdd(q_theia, NODE_ATTRIBUTE);
							int quarkRequests = ss.getQuarkRelativeAndAdd(quarkNode, REQUEST_ATTRIBUTE);
							//int kept_prom_id = ss.optQuarkRelative(q_theia, "context_id", id+tid);
							
							try {
								Integer quarkParent = ss.getQuarkRelativeAndAdd(quark, id);
								
								ss.modifyAttribute(ts, "PROMISE", quarkParent);
								async_capability.put(id + tid, quarkParent);
								//ss.modifyAttribute(ts, quarkParent, kept_prom_id);
								promise.put(id, ts);
								}
								
								catch(IndexOutOfBoundsException e) {}
								
							
							
							
							

						}

						else {
							
							
							if(type_freq.equals("single")) {

								int quarkNode = ss.getQuarkRelativeAndAdd(q_theia, NODE_ATTRIBUTE);
								int quarkRequests = ss.getQuarkRelativeAndAdd(quarkNode, REQUEST_ATTRIBUTE);
								//int kept_prom_id = ss.optQuarkRelative(q_theia, "context_id", id+tid);
								

								int quarkParent = ss.getQuarkRelativeAndAdd(quarkRequests, id);
								
								ss.modifyAttribute(ts, met, quarkParent);
								async_capability.put(id + tid, quarkParent);
								promise.put(id, ts);
							}
								
						
						}

						if (test_cont > 0) {

							int q_cont_id = ss.getQuarkRelativeAndAdd(q_nodejs, "context_id");
							String state = String.valueOf(ss.queryOngoing(q_cont_id));

							if (!state.equals("null")) {

								cont_async2.put(id, state);
								int q_chid = ss.getQuarkRelativeAndAdd(q_nodejs, state);
								int q_async = ss.getQuarkRelativeAndAdd(q_chid, id);
								ss.modifyAttribute(ts, met, q_async);

								// ch.Keep the context timestamp for the jsx object
								promise.put(id, ts);

								// int a_promise = ss.getQuarkRelativeAndAdd(q_theia, "Time stamp");
								// int a_promise_2 = ss.getQuarkRelativeAndAdd(a_promise, id);
								// ss.modifyAttribute(ts, ts, a_promise_2);

								int test_xy = ss.optQuarkRelative(q_theia, "XY_promise", "Promise Queue");
								if (test_xy > 0) {

									int a_xy = ss.getQuarkRelativeAndAdd(q_theia, "XY_promise");
									int a_xy_2 = ss.getQuarkRelativeAndAdd(a_xy, "Promise Queue");
									String state_xy = String.valueOf(ss.queryOngoing(a_xy_2));
									int pr_state = Integer.parseInt(state_xy) + 1;

									ss.modifyAttribute(ts, pr_state, a_xy_2);

								}

								else {

									int a_xy = ss.getQuarkRelativeAndAdd(q_theia, "XY_promise");
									int a_xy_2 = ss.getQuarkRelativeAndAdd(a_xy, "Promise Queue");
									ss.modifyAttribute(ts, 1, a_xy_2);

								}

							}

						}
						
						
						
						
						

					}

					else {

						if (met.startsWith("jsx")) {

							int q_nodejs = ss.getQuarkRelativeAndAdd(q_theia, "nodejs");
							int test_cont = ss.optQuarkRelative(q_theia, "context_id");
							//int id_prom_kept = ss.optQuarkRelative(q_theia, "context_id", id+tid);

							int quarkNode = ss.getQuarkRelative(q_theia, NODE_ATTRIBUTE);
							int quarkReq = ss.getQuarkRelative(quarkNode, REQUEST_ATTRIBUTE);
						 	met = met.replace("jsx_","");						 	
						 	Integer quarkParent = async_capability.get(id + tid);
						 	
						 	if(quarkParent != null) {
						 		
						 		Long p_ts = promise.get(id);
						 		
						 		if(p_ts !=null)
						 			ss.modifyAttribute(p_ts, met, quarkParent);
						 		
						 		
						 	}
						 	

							//ss.modifyAttribute(promise.get(id), met, quarkParent);
								
				 	

						}

						else {

							if (met.startsWith("resolve")) {

								int q_nodejs = ss.getQuarkRelativeAndAdd(q_theia, "nodejs");
								int test_cont = ss.optQuarkRelative(q_nodejs, "context_id");
								//int id_prom_kept = ss.optQuarkRelative(q_theia, "context_id", id+tid);
							    Integer quarkParent = async_capability.get(id + tid);
							    
							    int q_promise = ss.getQuarkRelativeAndAdd(q_theia, "Metrics","Promises","states");
								promise.remove(id, ts);
								microtask--;
								ss.modifyAttribute(ts, microtask, q_promise);
								
							 	

								if (quarkParent != null) {

									try{
										ss.modifyAttribute(ts, null, quarkParent);
										//async_capability.remove(id + tid);
										}
										catch(IndexOutOfBoundsException e) {}

								}

								if (test_cont > 0) {

									int q_cont_id = ss.getQuarkRelativeAndAdd(q_nodejs, "context_id");
									// String state = String.valueOf(ss.queryOngoing(q_cont_id));
									String state = cont_async2.get(id);

									// if(!state.equals("null")) {
									if (ss.optQuarkRelative(q_nodejs, state) > 0) {

										int q_chid = ss.getQuarkRelativeAndAdd(q_nodejs, state);
										int q_async = ss.getQuarkRelativeAndAdd(q_chid, id);
										ss.modifyAttribute(ts, null, q_async);
										cont_async2.remove(id);
										// }

										int a_xy = ss.getQuarkRelativeAndAdd(q_theia, "XY_promise");
										int a_xy_2 = ss.getQuarkRelativeAndAdd(a_xy, "Promise Queue");
										String state_xy = String.valueOf(ss.queryOngoing(a_xy_2));
										int pr_state = Integer.parseInt(state_xy) - 1;

										ss.modifyAttribute(ts, pr_state, a_xy_2);

									}
								}

							}

							else {

								if (met.startsWith("js_open_")) {

									context_tid_parent.replace(tid, 0);

									// System.out.println("bonjour");
									int quarkNode = ss.getQuarkRelativeAndAdd(q_theia, NODE_ATTRIBUTE);
									int quarkRequests = ss.getQuarkRelativeAndAdd(quarkNode, REQUEST_ATTRIBUTE);
									int quarkRpc = ss.getQuarkRelativeAndAdd(quarkNode, RPC_ATTRIBUTE);
									// int q_cont = ss.getQuarkRelativeAndAdd(q_req, ch+"_"+id);
									met = met.replace("js_open_", "");
									// Integer quarkSpan = fReqs.get(id);

									if (!met.contains("$")) {

										type_freq = "single";
										int quarkParent = ss.getQuarkRelativeAndAdd(quarkRequests, tid,ch);

										Map<String, Integer> single_capability = fReqs.get("single");

										if (single_capability == null)

										{
											single_capability = new HashMap();
											single_capability.put(id + tid, quarkParent);
											async_capability.putIfAbsent(ch + tid, quarkParent);
											fReqs.put("single", single_capability);
											ss.modifyAttribute(ts, met, quarkParent);

										}

										else {

											// System.out.println("Aurevoir");
											single_capability.put(id + tid, quarkParent);
											async_capability.putIfAbsent(ch + tid, quarkParent);
											fReqs.replace("single", single_capability);
											ss.modifyAttribute(ts, met, quarkParent);

										}

									}

									else {

										// System.out.println("Maintenant");
										// int quarkParent = ss.getQuarkRelativeAndAdd(quarkRequests,
										// met.split("\\$")[1], ch);
										Map<String, Integer> multi_capability = fReqs.get(met.split("\\$")[1]);

										int quarkParent = ss.getQuarkRelativeAndAdd(quarkRequests, tid, met.split("\\$")[1],
												ch);

										type_freq = "multiple";

										if (multi_capability == null) {

											multi_capability = new HashMap();

											multi_capability.put(id + tid, quarkParent);
											async_capability.putIfAbsent(ch + tid, quarkParent);

											fReqs.put(met.split("\\$")[1], multi_capability);
											int quark_id = ss.getQuarkRelativeAndAdd(quarkRpc, id, met.split("\\$")[1]);
											ss.modifyAttribute(ts, met.split("\\$")[0], quarkParent);
											ss.modifyAttribute(ts,met.split("\\$")[0],quark_id);

										} else {

											multi_capability.put(id + tid, quarkParent);
											async_capability.putIfAbsent(ch + tid, quarkParent);
											fReqs.replace(met.split("\\$")[1], multi_capability);
											int quark_id = ss.getQuarkRelativeAndAdd(quarkRpc, id, met.split("\\$")[1]);
											ss.modifyAttribute(ts, met.split("\\$")[0], quarkParent);
											ss.modifyAttribute(ts,met.split("\\$")[0],quark_id);

											// System.out.println("demain");

										}

										// ss.modifyAttribute(ts, met, q_cont);

										// int q_context = ss.getQuarkRelativeAndAdd(q_nodejs, "context_id");
										// ss.modifyAttribute(ts, ch+"_"+id, q_context);
									}

								}

								else {

									if (met.startsWith("js_exit")) {

										int quarkNode = ss.getQuarkRelativeAndAdd(q_theia, NODE_ATTRIBUTE);
										int quarkRequests = ss.getQuarkRelativeAndAdd(quarkNode, REQUEST_ATTRIBUTE);
										int quarkRpc = ss.getQuarkRelativeAndAdd(quarkNode, RPC_ATTRIBUTE);
										// int q_cont = ss.getQuarkRelativeAndAdd(q_req, ch+"_"+id);
										// met=met.replace("js_open_", "");
										// Integer quarkSpan = fReqs.get(id);

										if (met.contains("$")) {

											Map<String, Integer> multi_capability = fReqs.get(met.split("\\$")[1]);

											if (multi_capability != null) {

												Integer quarkParent = multi_capability.get((id + tid));
												
												int quark_id = ss.getQuarkRelativeAndAdd(quarkRpc, id, met.split("\\$")[1]);
												
												//ss.modifyAttribute(ts,null,quark_id);


												if (quarkParent != null) {

													 try {
															//ss.modifyAttribute(ts, null, quarkParent);
		                                                    }
		                                                    
		                                                    catch(IndexOutOfBoundsException e) {}


												}

											}

										}

										else {

											Map<String, Integer> single_capability = fReqs.get("single");

											if (single_capability != null)

											{

												Integer quark = single_capability.get(id + tid);

												//ss.modifyAttribute(ts, null, quark);

											}

										}

									}

									else {

										if (met.startsWith("destroy")) {
											
											

												int q_nodejs = ss.getQuarkRelativeAndAdd(q_theia, "nodejs");

												Integer quarkParent = async_capability.get(id + tid);

												if (quarkParent != null) {

													// Integer quarkParent = ss.getQuarkRelativeAndAdd(quark, id);
													ss.modifyAttribute(ts, null, quarkParent);
													//async_capability.remove(id + tid);

												}

										}

										else {

											if (met.startsWith("FSREQCALLBACK")) {

												int q_nodejs = ss.getQuarkRelativeAndAdd(q_theia, "nodejs");

												Integer quark = async_capability.get(ch + tid);

												if (quark != null) {

													Integer quarkParent;
													try {
													quarkParent= ss.getQuarkRelativeAndAdd(quark, id);
													// ss.modifyAttribute(ts, "FSREQCALLBACK", quarkParent);
													async_capability.put(id + tid, quarkParent);
													context_tid_status.put(tid, 1);
													context_tid_parent.put(tid, quarkParent);
												}
													catch(Exception e) { return; }
												}
												else {
													
													//System.out.println(id);
													return;
												}

											}

											else {

												if (met.startsWith("run")) {
													
													int q_nextick = ss.getQuarkRelativeAndAdd(q_theia, "Metrics","NextTick","states");
													int q_microtask = ss.getQuarkRelativeAndAdd(q_theia, "Metrics","Microtasks","states");
													Integer map_n =map_nexttick.get(id);
													Integer map_mic =map_microtask.get(id);
													if(map_n != null) {
														map_nexttick.remove(id);
													    ss.modifyAttribute(ts, map_nexttick.size(), q_nextick);
													
													}
													
													if(map_microtask != null) {
														map_microtask.remove(id);
													    ss.modifyAttribute(ts, map_microtask.size(), q_microtask);
													
													}

													int q_nodejs = ss.getQuarkRelativeAndAdd(q_theia, "nodejs");

													Integer quarkParent = async_capability.get(id + tid);

													if (quarkParent != null) {

														// Integer quarkParent = ss.getQuarkRelativeAndAdd(quark, id);
														ss.modifyAttribute(ts, null, quarkParent);
														async_capability.remove(id + tid);

													}

												}

												else {
													
													
													
													
													if (met.startsWith("FSREQPROMISE")) {

														Integer PID=null;
														
														try {
															
															PID = Integer.parseInt(met.split(".")[1]);
														}
														
														catch(Exception e) {}
														

														Integer quark = async_capability.get(ch + tid);

														if (quark != null) {

															Integer quarkParent = ss.getQuarkRelativeAndAdd(quark, id);
															// ss.modifyAttribute(ts, "FSREQCALLBACK", quarkParent);
															async_capability.put(id + tid, quarkParent);
															context_tid_status.put(tid, 1);
															
															context_tid_parent.put(tid, quarkParent);
															
															if( PID != null) {
																
																context_pid_status.put(PID, 1);
																context_pid_parent.put(PID, quarkParent);
																
															}

														}
														else {
															
															//System.out.println(id);
															return;
														}


													}
													
													else {

														if (met.startsWith("TickObject")) {
															
															nexttick=nexttick+1;
															int q_nextick = ss.getQuarkRelativeAndAdd(q_theia, "Metrics","NextTick","states");
															map_nexttick.put(id, nexttick);
															ss.modifyAttribute(ts, map_nexttick.size(), q_nextick);
															
														
														}
														
														else {
															
															if (met.startsWith("Microtask")) {
																
																microtask=microtask+1;
																int q_micro = ss.getQuarkRelativeAndAdd(q_theia, "Metrics","Microtasks","states");
																map_microtask.put(id, microtask);
																ss.modifyAttribute(ts, map_microtask.size(), q_micro);
																
															
															}
															
															else {
															
													     int q_nodejs = ss.getQuarkRelativeAndAdd(q_theia, "nodejs");

													     Integer quark = async_capability.get(ch + tid);

														  if (quark != null) {
	
															Integer quarkParent = ss.getQuarkRelativeAndAdd(quark, id);
															ss.modifyAttribute(ts, met, quarkParent);
															async_capability.put(id + tid, quarkParent);
															context_tid_status.put(tid, 1);
																context_tid_parent.put(tid, quarkParent);
	
														}
														}
													  }
													 }
	
													}

											}

										}

									}

								}

							}

						}

					}

				}

				else { // uv_send except the first expllicitly treated methods

					

				}

			}

			else {//

			}

		}

		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // $NON-NLS-1$

	}

	public void FixAndCreateNodeValue(long ts, ITmfStateSystemBuilder ss, int quark_node, String node, String value) {

		int quark = ss.getQuarkRelativeAndAdd(quark_node, node);
		ss.modifyAttribute(ts, value, quark);

	}

	public void FixNodeValue(long ts, ITmfStateSystemBuilder ss, int quark_node, String value) {

		ss.modifyAttribute(ts, value, quark_node);

	}

	public String GetValueFromStates(ITmfStateSystemBuilder ss, int quark) {

		String id = (String)(ss.queryOngoing(quark));

		return id;
	}

	public String GetStateFromQuark(ITmfStateSystemBuilder ss, int quark, String node)
			throws AttributeNotFoundException {

		List<Integer> q_f = ss.getSubAttributes(quark, false);
		for (int i : q_f) {
			int q_ret = ss.optQuarkRelative(i, node);
			if (q_ret > 0) {
				int q_ret_ok = ss.getQuarkRelative(i, node);
				String ret_state = (String)(ss.queryOngoing(q_ret_ok));

				return ret_state;

			}

		}

		return "err";

	}

	public int GetQuark(ITmfStateSystemBuilder ss, int quark, String node) throws AttributeNotFoundException {

		List<Integer> q_f = ss.getSubAttributes(quark, false);
		for (int i : q_f) {
			int q_ret = ss.optQuarkRelative(i, node);
			if (q_ret > 0) {

				return q_ret;

			}

		}

		return 0;

	}

	public boolean AsyncReqCompleted(ITmfStateSystemBuilder ss, int stateQuark) throws AttributeNotFoundException {
		
		String state = String.valueOf(ss.queryOngoing(stateQuark));
		if(state.equals("1"))
			return true;

		return false;

	}
	
   public Object GetStateFromQuark(ITmfStateSystemBuilder ss, int stateQuark) throws AttributeNotFoundException {
		
		return ss.queryOngoing(stateQuark);
		
	}
   
   

}

