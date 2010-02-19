/*
 * Copyright 2007-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
package net.java.btrace.visualvm.api.impl;

import com.sun.btrace.comm.Command;
import java.io.File;
import java.io.PrintWriter;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.java.btrace.visualvm.api.BTraceEngine;
import net.java.btrace.visualvm.api.BTraceTask;
import net.java.btrace.visualvm.spi.ProcessDetailsProvider;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Jaroslav Bachorik
 */
public class BTraceTaskImpl extends BTraceTask implements BTraceEngine.StateListener {
    final private static Pattern NAMED_EVENT_PATTERN = Pattern.compile("@OnEvent\\s*\\(\\s*\\\"(\\w.*)\\\"\\s*\\)", Pattern.MULTILINE);
    final private static Pattern ANONYMOUS_EVENT_PATTERN = Pattern.compile("@OnEvent(?!\\s*\\()");
    final private static Pattern UNSAFE_PATTERN = Pattern.compile("@BTrace\\s*\\(.*unsafe\\s*=\\s*(true|false).*\\)", Pattern.MULTILINE);
    final private static Pattern NAME_PATTERN = Pattern.compile("@BTrace\\s*\\(.*name\\s*=\\s*\"(.*)\".*\\)", Pattern.MULTILINE);

    final private Set<CommandListener> commandListeners = new HashSet<CommandListener>();

    final private AtomicReference<State> currentState = new AtomicReference<State>(State.NEW);
    final private Set<StateListener> stateListeners = new HashSet<StateListener>();


    private PrintWriter consoleWriter = new PrintWriter(System.out, true);

    private String script;
    private int numInstrClasses;
    private boolean unsafe;

    final private Set<String> classPath = new HashSet<String>();

    final private BTraceEngine engine;

    final private int pid;

    public BTraceTaskImpl(int pid, BTraceEngine engine) {
        this.pid = pid;
        this.engine = engine;
        engine.addListener(WeakListeners.create(BTraceEngine.StateListener.class, this, engine));
    }

    @Override
    public BTraceEngine getEngine() {
        return engine;
    }

    @Override
    public void setWriter(PrintWriter writer) {
        consoleWriter = writer;
    }

    @Override
    public PrintWriter getWriter() {
        return consoleWriter;
    }

    @Override
    public String getScript() {
        return script;
    }

    @Override
    public String getName() {
        Matcher m = NAME_PATTERN.matcher(script);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    @Override
    public void setScript(String newValue) {
        script = newValue;
        Matcher m = UNSAFE_PATTERN.matcher(script);
        if (m.find()) {
            unsafe = Boolean.parseBoolean(m.group(1));
        }
    }

    @Override
    public void sendEvent(String event) {
        getEngine().sendEvent(this, event);
    }

    @Override
    public void sendEvent() {
        getEngine().sendEvent(this);
    }

    @Override
    public Set<String> getNamedEvents() {
        Set<String> events = new HashSet<String>();
        Matcher matcher = NAMED_EVENT_PATTERN.matcher(getScript());
        while (matcher.find()) {
            events.add(matcher.group(1));
        }
        return events;
    }

    @Override
    public boolean hasAnonymousEvents() {
        Matcher matcher = ANONYMOUS_EVENT_PATTERN.matcher(getScript());
        return matcher.find();
    }

    @Override
    public boolean hasEvents() {
        return getScript() != null && getScript().contains("@OnEvent");
    }

    @Override
    public void addCommandListener(CommandListener listener) {
        commandListeners.add(listener);
    }

    @Override
    public void removeCommandListener(CommandListener listener) {
        commandListeners.remove(listener);
    }

    public int getPid() {
        return pid;
    }

    public Properties getSystemProperties() {
        return getProcessDetailsProvider().getSystemProperties(pid);
    }

    /**
     * Property getter
     * @return Returns the current state
     */
    protected State getState() {
        return currentState.get();
    }

    @Override
    public int getInstrClasses() {
        return EnumSet.of(State.INSTRUMENTING, State.RUNNING).contains(getState()) ? numInstrClasses : -1;
    }

    /**
     * Listener management (can use {@linkplain WeakListeners} to create a new listener)
     * @param listener {@linkplain StateListener} instance to add
     */
    @Override
    public void addStateListener(StateListener listener) {
        synchronized (stateListeners) {
            stateListeners.add(listener);
        }
    }

    /**
     * Listener management
     * @param listener {@linkplain StateListener} instance to remove
     */
    @Override
    public void removeStateListener(StateListener listener) {
        synchronized (stateListeners) {
            stateListeners.remove(listener);
        }
    }

    /**
     * Starts the injected code using the attached {@linkplain BTraceEngine}
     */
    @Override
    public void start() {
        final State previousState = getState();
        setState(State.STARTING);
        if (!getEngine().start(this)) {
            setState(previousState);
        }
    }

    /**
     * Stops the injected code running on the attached {@linkplain BTraceEngine}
     */
    @Override
    public void stop() {
        if (getState() == State.RUNNING) {
            if (!getEngine().stop(this)) {
                setState(State.RUNNING);
            }
        }
    }

    /**
     * @see BTraceEngine.StateListener#onTaskStart(net.java.visualvm.btrace.api.BTraceTask)
     */
    @Override
    public void onTaskStart(BTraceTask task) {
        if (task.equals(this)) {
            setState(State.RUNNING);
        }
    }

    /**
     * @see BTraceEngine.StateListener#onTaskStop(net.java.visualvm.btrace.api.BTraceTask)
     */
    @Override
    public void onTaskStop(BTraceTask task) {
        if (task.equals(this)) {
            setState(State.FINISHED);
        }
    }

    @Override
    public void addCPEntry(String cpEntry) {
        classPath.add(cpEntry);
    }

    @Override
    public void removeCPEntry(String cpEntry) {
        classPath.remove(cpEntry);
    }

    @Override
    public String getClassPath() {
        StringBuilder sb = new StringBuilder();
        String initCp = getInitClassPath();
        if (initCp != null) sb.append(initCp);
        for(String cpEntry : classPath) {
            if (sb.length() > 0) {
                sb.append(File.pathSeparator);
            }
            sb.append(cpEntry);
        }
        return sb.toString();
    }

    @Override
    public boolean isUnsafe() {
        return unsafe;
    }

    void setState(State newValue) {
        currentState.set(newValue);
        fireStateChange();
    }

    void setInstrClasses(int value) {
        numInstrClasses = value;
    }

    private void fireStateChange() {
        synchronized (stateListeners) {
            for (StateListener listener : stateListeners) {
                listener.stateChanged(getState());
            }
        }
    }

    void dispatchCommand(final Command cmd) {
        final Set<CommandListener> dispatchingSet = new HashSet<BTraceTask.CommandListener>();
        synchronized(commandListeners) {
            dispatchingSet.addAll(commandListeners);
        }
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                for(CommandListener listener : dispatchingSet) {
                    listener.onCommand(cmd);
                }
            }
        });
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BTraceTaskImpl other = (BTraceTaskImpl) obj;
        if (this.pid != other.pid) {
            return false;
        }
        if (this.engine != other.engine && (this.engine == null || !this.engine.equals(other.engine))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + pid;
        hash = 19 * hash + (this.engine != null ? this.engine.hashCode() : 0);
        return hash;
    }

    private String getInitClassPath() {
        Properties props = getSystemProperties();
        String userDir = props.getProperty("user.dir", null); // NOI18N
        String cp = props.getProperty("java.class.path", ""); // NOI18N
        StringBuilder initCP = new StringBuilder();
        if (userDir != null) {
            StringTokenizer st = new StringTokenizer(cp, File.pathSeparator);
            while (st.hasMoreTokens()) {
                String pathElement = st.nextToken();
                if (initCP.length() > 0) {
                    initCP.append(File.pathSeparator);
                }
                initCP.append(userDir).append(File.separator).append(pathElement);
            }
        }
        return cp.toString();
    }

    final private ProcessDetailsProvider getProcessDetailsProvider() {
        ProcessDetailsProvider pdp = Lookup.getDefault().lookup(ProcessDetailsProvider.class);
        return pdp != null ? pdp : ProcessDetailsProvider.NULL;
    }
}
