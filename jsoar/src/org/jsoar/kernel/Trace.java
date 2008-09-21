/*
 * Copyright (c) 2008  Dave Ray <daveray@gmail.com>
 *
 * Created on Sep 16, 2008
 */
package org.jsoar.kernel;

import java.io.OutputStreamWriter;
import java.util.Formattable;
import java.util.Formatter;

/**
 * This class encapsulates the tracing mechanism used throughout the kernel
 * into a single location.
 * 
 * @author ray
 */
public class Trace
{
    /**
     * gsysparam.h:92
     * 
     * @author ray
     */
    public static enum Category
    {
        TRACE_VERBOSE,
        TRACE_CONTEXT_DECISIONS_SYSPARAM(true),
        TRACE_PHASES_SYSPARAM,

        /* --- Warning: these next five MUST be consecutive and in the order of the
           production types defined above --- */
        TRACE_FIRINGS_OF_USER_PRODS_SYSPARAM,
        TRACE_FIRINGS_OF_DEFAULT_PRODS_SYSPARAM,
        TRACE_FIRINGS_OF_CHUNKS_SYSPARAM,
        TRACE_FIRINGS_OF_JUSTIFICATIONS_SYSPARAM,
        TRACE_FIRINGS_OF_TEMPLATES_SYSPARAM,

        TRACE_FIRINGS_PREFERENCES_SYSPARAM,
        TRACE_WM_CHANGES_SYSPARAM,
        TRACE_CHUNK_NAMES_SYSPARAM,
        TRACE_JUSTIFICATION_NAMES_SYSPARAM,
        TRACE_CHUNKS_SYSPARAM,
        TRACE_JUSTIFICATIONS_SYSPARAM,
        TRACE_BACKTRACING_SYSPARAM,
        TRACE_LOADING_SYSPARAM(true),
        TRACE_OPERAND2_REMOVALS_SYSPARAM ,
        TRACE_INDIFFERENT_SYSPARAM,
        TRACE_RL_SYSPARAM;
        
        public final boolean defaultSetting;
        
        Category()
        {
            this.defaultSetting = false;
        }
        
        Category(boolean defaultSetting)
        {
            this.defaultSetting = defaultSetting;
        }
    }
    
    /**
     * How much information to print about the wmes matching an instantiation
     * 
     * gsysparam.h:72:wme_trace_type
     * 
     * @author ray
     */
    public static enum WmeTraceType
    {
        NONE_WME_TRACE,      /* don't print anything */
        TIMETAG_WME_TRACE,   /* print just timetag */
        FULL_WME_TRACE,      /* print whole wme */
    }
    
    public static Trace createStdOutTrace()
    {
        return new Trace(new Printer(new OutputStreamWriter(System.out)));
    }
    
    private final Printer printer;
    private final boolean settings[] = new boolean[Category.values().length];
    private boolean enabled = true;
    private WmeTraceType wmeTraceType = WmeTraceType.NONE_WME_TRACE;
    
    /**
     * @param printer
     */
    public Trace(Printer printer)
    {
        this.printer = printer;
        
        for(Category c : Category.values())
        {
            settings[c.ordinal()] = c.defaultSetting;
        }
    }
    
    /**
     * @return The printer this trace is going to
     */
    public Printer getPrinter()
    {
        return this.printer;
    }

    
    /**
     * @return True if overall tracing is enabled
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Control global tracing. If set to false, no tracing will be performed
     * 
     * @param enabled True to enable tracing, false to disable all tracing
     */
    public Trace setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }
    
    /**
     * Enable all trace categories
     * 
     * @return this 
     */
    public Trace enableAll()
    {
        setEnabled(true);
        for(int i = 0; i < settings.length; ++i)
        {
            settings[i] = true;
        }
        this.setWmeTraceType(WmeTraceType.FULL_WME_TRACE);
        return this;
    }
    
    /**
     * @param c The category to query
     * @return True if tracing is enabled for the particular category
     */
    public boolean isEnabled(Category c)
    {
        return settings[c.ordinal()];
    }

    /**
     * Control category tracing. If set to false, no tracing will be performed
     * for the given category
     * 
     * @param c The category
     * @param enabled True to enable tracing for the given category, false to 
     *      disable tracing for the given category
     */
    public Trace setEnabled(Category c, boolean enabled)
    {
        this.settings[c.ordinal()] = enabled;
        return this;
    }

    /**
     * @return the wmeTraceType
     */
    public WmeTraceType getWmeTraceType()
    {
        return wmeTraceType;
    }

    /**
     * @param wmeTraceType the wmeTraceType to set
     */
    public void setWmeTraceType(WmeTraceType wmeTraceType)
    {
        this.wmeTraceType = wmeTraceType;
    }

    /**
     * Trace a string in a particular category.
     * 
     * @param c The category
     * @param format The printf-style format string
     * @param args printf arguments
     */
    public Trace print(Category c, String format, Object... args)
    {
        if(enabled && settings[c.ordinal()])
        {
            for(int i = 0; i < args.length; ++i)
            {
                // If it's traceable replace it with our proxy object.
                // TODO Restore args on return?
                if(args[i] instanceof Traceable)
                {
                    args[i] = new Proxy((Traceable) args[i]);
                }
            }
            printer.print(format, args);
        }
        return this;
    }
    
    public Trace startNewLine()
    {
        if(enabled)
        {
            printer.startNewLine();
        }
        return this;
    }
    
    /**
     * A wrapper object we put around all trace arguments that implement {@link Traceable}.
     * This gives us a way to give the traceable access to the Trace object in the
     * context of the Java formattable framework.
     * 
     * @author ray
     */
    private class Proxy implements Formattable
    {
        private final Traceable traceable;
        
        /**
         * @param traceable The traceable object
         */
        public Proxy(Traceable traceable)
        {
            this.traceable = traceable;
        }

        /* (non-Javadoc)
         * @see java.util.Formattable#formatTo(java.util.Formatter, int, int, int)
         */
        @Override
        public void formatTo(Formatter formatter, int flags, int width, int precision)
        {
            traceable.trace(Trace.this, formatter, flags, width, precision);
        }
        
    }
}
