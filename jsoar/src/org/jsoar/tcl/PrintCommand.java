/*
 * Copyright (c) 2008  Dave Ray <daveray@gmail.com>
 *
 * Created on Oct 30, 2008
 */
package org.jsoar.tcl;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.jsoar.kernel.Agent;
import org.jsoar.kernel.Production;
import org.jsoar.kernel.ProductionManager;
import org.jsoar.kernel.ProductionType;
import org.jsoar.kernel.memory.WorkingMemoryPrinter;
import org.jsoar.kernel.symbols.Symbol;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclObject;

/**
 * @author ray
 */
public class PrintCommand implements Command
{
    private static enum Options
    {
        ALL, CHUNKS, DEFAULTS, INTERNAL, JUSTIFICATIONS, STACK, TREE, TEMPLATE, USER,
        STATES, OPERATORS, FULL;
        // TODO full, filename, rl, states, varprint
    }
    private final SoarTclInterface ifc;
    private final WorkingMemoryPrinter wmp = new WorkingMemoryPrinter();
    
    private final EnumSet<Options> options = EnumSet.noneOf(Options.class);
    private int depth = 1;
    
    /**
     * @param agent
     */
    public PrintCommand(SoarTclInterface ifc)
    {
        this.ifc = ifc;
    }
    
    private static boolean has(String arg, String little, String big)
    {
        return arg.equals(little) || arg.equals(big);
    }
    
    private List<Production> collectProductions()
    {
        final ProductionManager pm = ifc.getAgent().getProductions();
        final List<Production> result = new ArrayList<Production>();
        
        if(options.contains(Options.ALL))
        {
            result.addAll(pm.getProductions(null));
        }
        else
        {
            if(options.contains(Options.CHUNKS)) result.addAll(pm.getProductions(ProductionType.CHUNK));
            if(options.contains(Options.USER)) result.addAll(pm.getProductions(ProductionType.USER));
            if(options.contains(Options.DEFAULTS)) result.addAll(pm.getProductions(ProductionType.DEFAULT));
            if(options.contains(Options.TEMPLATE)) result.addAll(pm.getProductions(ProductionType.TEMPLATE));
            if(options.contains(Options.JUSTIFICATIONS)) result.addAll(pm.getProductions(ProductionType.JUSTIFICATION));
        }
        
        return result;
    }
    
    private int processArgs(Interp interp, TclObject[] args) throws TclException
    {
        int i = 1;
        for(; i < args.length; ++i)
        {
            String arg = args[i].toString();
            if(has(arg, "-d", "--depth"))
            {
                if(i + 1 == args.length)
                {
                    throw new TclException(interp, "No argument for --depth option");
                }
                try
                {
                    depth = Integer.parseInt(args[++i].toString());
                    if(depth < 0)
                    {
                        throw new TclException(interp, "--depth must be positive");
                    }
                }
                catch(NumberFormatException e)
                {
                    throw new TclException(interp, "Invalid --depth value");
                }
            }
            else if(has(arg, "-t", "--tree"))
            {
                options.add(Options.TREE);
            }
            else if(has(arg, "-i", "--internal"))
            {
                options.add(Options.INTERNAL);
            }
            else if(has(arg, "-a", "--all"))
            {
                options.add(Options.ALL);
            }
            else if(has(arg, "-u", "--user"))
            {
                options.add(Options.USER);
            }
            else if(has(arg, "-c", "--chunks"))
            {
                options.add(Options.CHUNKS);
            }
            else if(has(arg, "-d", "--defaults"))
            {
                options.add(Options.DEFAULTS);
            }
            else if(has(arg, "-j", "--justifications"))
            {
                options.add(Options.JUSTIFICATIONS);
            }
            else if(has(arg, "-s", "--stack"))
            {
                options.add(Options.STACK);
            }
            else if(has(arg, "-S", "--states"))
            {
                options.add(Options.STATES);
            }
            else if(has(arg, "-o", "--operators"))
            {
                options.add(Options.OPERATORS);
            }
            else if(has(arg, "-f", "--full"))
            {
                options.add(Options.FULL);
            }
            else if(has(arg, "-T", "--template"))
            {
                options.add(Options.TEMPLATE);
            }
            else if(arg.startsWith("-"))
            {
                throw new TclException(interp, "Unknow option " + arg);
            }
            else
            {
                break;
            }
        }
        return i;
    }

    @Override
    public void cmdProc(Interp interp, TclObject[] args) throws TclException
    {
        final Agent agent = ifc.getAgent();
        agent.getPrinter().startNewLine();
        
        options.clear();
        
        int firstNonOption = processArgs(interp, args);
        if(options.contains(Options.STACK))
        {
            agent.printStackTrace(options.contains(Options.STATES), options.contains(Options.OPERATORS));
            agent.getPrinter().print("\n").flush();
            return;
        }
        else if(options.contains(Options.ALL) || options.contains(Options.CHUNKS) ||
                options.contains(Options.USER) || options.contains(Options.TEMPLATE) ||
                options.contains(Options.DEFAULTS))
        {
            for(Production p : collectProductions())
            {
                if(options.contains(Options.FULL))
                {
                    p.print(agent.getPrinter(), options.contains(Options.INTERNAL));
                }
                else
                {
                    agent.getPrinter().print("%s   ", p.getName());
                    if(p.rl_rule)
                    {
                        agent.getPrinter().print("%d   %s", p.rl_update_count, p.action_list.asMakeAction().referent);
                    }
                }
                agent.getPrinter().print("\n");
            }
            agent.getPrinter().flush();
            return;
        }
                
        if(firstNonOption == args.length)
        {
            throw new TclException(interp, "Expected id or production name argument");
        }
        
        String argString = args[firstNonOption].toString();
        Symbol arg = agent.readIdentifierOrContextVariable(argString);
        if(arg != null)
        {
            agent.getPrinter().startNewLine();
            wmp.setDepth(depth);
            wmp.setInternal(options.contains(Options.INTERNAL));
            wmp.setTree(options.contains(Options.TREE));
            wmp.print(agent.syms, agent.getPrinter(), arg);
            agent.getPrinter().flush();
        }
        else
        {
            agent.getPrinter().startNewLine();
            Production p = agent.getProductions().getProduction(argString);
            if(p != null)
            {
                p.print(agent.getPrinter(), options.contains(Options.INTERNAL));
            }
            else
            {
                agent.getPrinter().print("No production '" + argString + "'");
            }
            agent.getPrinter().flush();
        }
    }
}