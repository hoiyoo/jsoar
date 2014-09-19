/*
 * Copyright (c) 2008  Dave Ray <daveray@gmail.com>
 *
 * Created on Sep 10, 2008
 */
package org.jsoar.kernel.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import org.jsoar.kernel.Agent;
import org.jsoar.kernel.Consistency;
import org.jsoar.kernel.Decider;
import org.jsoar.kernel.DecisionCycle;
import org.jsoar.kernel.Phase;
import org.jsoar.kernel.PredefinedSymbols;
import org.jsoar.kernel.Production;
import org.jsoar.kernel.Production.Support;
import org.jsoar.kernel.ProductionType;
import org.jsoar.kernel.SavedFiringType;
import org.jsoar.kernel.SoarConstants;
import org.jsoar.kernel.SoarProperties;
import org.jsoar.kernel.learning.Chunker;
import org.jsoar.kernel.learning.rl.ReinforcementLearning;
import org.jsoar.kernel.lhs.BackTraceInfo;
import org.jsoar.kernel.lhs.Condition;
import org.jsoar.kernel.lhs.PositiveCondition;
import org.jsoar.kernel.rete.ConditionsAndNots;
import org.jsoar.kernel.rete.Rete;
import org.jsoar.kernel.rete.SoarReteAssertion;
import org.jsoar.kernel.rete.SoarReteListener;
import org.jsoar.kernel.rete.Token;
import org.jsoar.kernel.rhs.Action;
import org.jsoar.kernel.rhs.FunctionAction;
import org.jsoar.kernel.rhs.MakeAction;
import org.jsoar.kernel.rhs.ReteLocation;
import org.jsoar.kernel.rhs.RhsFunctionCall;
import org.jsoar.kernel.rhs.RhsSymbolValue;
import org.jsoar.kernel.rhs.RhsValue;
import org.jsoar.kernel.rhs.UnboundVariable;
import org.jsoar.kernel.rhs.functions.RhsFunctionContext;
import org.jsoar.kernel.rhs.functions.RhsFunctionException;
import org.jsoar.kernel.smem.SemanticMemory;
import org.jsoar.kernel.symbols.Identifier;
import org.jsoar.kernel.symbols.IdentifierImpl;
import org.jsoar.kernel.symbols.Symbol;
import org.jsoar.kernel.symbols.SymbolFactory;
import org.jsoar.kernel.symbols.SymbolFactoryImpl;
import org.jsoar.kernel.symbols.SymbolImpl;
import org.jsoar.kernel.symbols.Variable;
import org.jsoar.kernel.tracing.Trace;
import org.jsoar.kernel.tracing.Trace.Category;
import org.jsoar.kernel.wma.DefaultWorkingMemoryActivation;
import org.jsoar.util.Arguments;
import org.jsoar.util.ByRef;
import org.jsoar.util.adaptables.Adaptables;
import org.jsoar.util.properties.LongPropertyProvider;
import org.jsoar.util.timing.ExecutionTimers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * <em>This is an internal interface. Don't use it unless you know what you're doing.</em>
 * 
 * <p>Recognition Memory (Firer and Chunker) Routines (Does not include the Rete
 * net)
 * 
 * <p>Init_firer() and init_chunker() should be called at startup time, to do
 * initialization.
 * 
 * <p>Do_preference_phase() runs the entire preference phases. This is called from
 * the top-level control in main.c.
 * 
 * <p>Possibly_deallocate_instantiation() checks whether an instantiation can be
 * deallocated yet, and does so if possible. This is used whenever the
 * (implicit) reference count on the instantiation decreases.
 * 
 * <p>recmem.cpp (also some prefmem.cpp)
 * 
 * @author ray
 */
public class RecognitionMemory
{
    private static final Logger logger = LoggerFactory.getLogger(RecognitionMemory.class);
    
    private final Agent context;
    private PredefinedSymbols predefinedSyms;
    private Decider decider;
    private Chunker chunker;
    private DecisionCycle decisionCycle;
    private Rete rete;
    private TemporaryMemory tempMemory;
    private OSupport osupport;
    private SoarReteListener soarReteListener;
    private Consistency consistency;
    private ReinforcementLearning rl;
    private DefaultWorkingMemoryActivation wma;
    
    /**
     * agent.h:174:firer_highest_rhs_unboundvar_index
     */
    private int firer_highest_rhs_unboundvar_index;
    
    /**
     * agent.h:571:newly_created_instantiations
     * @see Instantiation#insertAtHeadOfProdList(Instantiation)
     * @see Instantiation#removeFromProdList(Instantiation)
     */
    public Instantiation newly_created_instantiations;
    
    /**
     * during firing, points to the prod. being fired 
     * 
     * agent.h:574:production_being_fired
     */
    private Production production_being_fired;
    
    /**
     * agent.h:367:production_firing_count
     */
    private final LongPropertyProvider production_firing_count = new LongPropertyProvider(SoarProperties.PRODUCTION_FIRING_COUNT);
    
    /**
     * agent.h:720:FIRING_TYPE
     */
    public SavedFiringType FIRING_TYPE;
    
    private final RhsFunctionContext rhsFuncContext = new RhsFunctionContextImpl();
    
    /**
     * List of preferences created by currently executing RHS function
     */
    private final LinkedList<Preference> rhsFunctionPreferences = new LinkedList<Preference>();
    
    /**
     * Preference type of currently executing action, used when instantiating preferences from
     * RHS functions
     */
    private PreferenceType rhsFunctionPreferenceType;
    
    /**
     * @param context
     */
    public RecognitionMemory(Agent context)
    {
        this.context = context;
    }

    /**
     * As the "firer", this object is in charge of implementing the {@link RhsFunctionContext}
     * that is passed to RHS functions when they are executed.
     * 
     * @return The RHS function context used when RHS functions are executed.
     */
    public RhsFunctionContext getRhsFunctionContext()
    {
        return rhsFuncContext;
    }
    
    public void initialize()
    {
        this.predefinedSyms = Adaptables.adapt(context, PredefinedSymbols.class);
        this.decider = Adaptables.adapt(context, Decider.class);
        this.chunker = Adaptables.adapt(context, Chunker.class);
        this.decisionCycle = Adaptables.adapt(context, DecisionCycle.class);
        this.rete = Adaptables.adapt(context, Rete.class);
        this.tempMemory = Adaptables.adapt(context, TemporaryMemory.class);
        this.osupport = Adaptables.adapt(context, OSupport.class);
        this.soarReteListener = Adaptables.adapt(context, SoarReteListener.class);
        this.consistency = Adaptables.adapt(context, Consistency.class);
        this.rl = Adaptables.adapt(context, ReinforcementLearning.class);
        this.wma = Adaptables.adapt(context, DefaultWorkingMemoryActivation.class);
        
        context.getProperties().setProvider(SoarProperties.PRODUCTION_FIRING_COUNT, production_firing_count);
    }
    
    /**
     * <p>init_soar.cpp:297:reset_statistics
     * <p>init_soar.cpp:436:reinitialize_soar
     */
    public void reset()
    {
        this.production_firing_count.reset();
        
        this.FIRING_TYPE = SavedFiringType.IE_PRODS; /* KJC 10.05.98 was PE */
    }
    
    /*
     * Build context-dependent preference set
     * 
     * This function will copy the CDPS from a slot to the backtrace info for the
     * corresponding condition.  The copied CDPS will later be backtraced through.
     * 
     * Note: Until prohibits are included explicitly as part of the CDPS, we will
     * just copy them directly from the prohibits list so that there is no
     * additional overhead.  Once the CDPS is on by default, we can eliminate the
     * second half of the big else (and not call this function at all if the
     * sysparam is not set.
     * 
     * @param inst
     * 
     * recmem.cpp:95:build_CDPS
     * 
     */

    void build_CDPS(Instantiation inst)
    {
        for (Condition cond = inst.top_of_instantiated_conditions; cond != null; cond = cond.next)
        {
            final PositiveCondition pc = cond.asPositiveCondition();
            final BackTraceInfo bt = pc != null ? pc.bt() : null;
            if (pc != null)
            {
                bt.clearContextDependentPreferenceSet(context);
            }

            if (pc != null && bt.trace != null)
            {
                if (bt.trace.slot != null)
                {
                    if (this.chunker.chunkThroughEvaluationRules)
                    {
                        if (bt.trace.slot.hasContextDependentPreferenceSet())
                        {
                            for(Preference pref : bt.trace.slot.getContextDependentPreferenceSet())
                            {
                                Preference new_pref = null;
                                if (pref.inst.match_goal_level == inst.match_goal_level
                                        && pref.isInTempMemory())
                                {
                                    bt.addContextDependentPreference(pref);
                                }
                                else
                                {
                                    new_pref = Preference.find_clone_for_level(
                                            pref, inst.match_goal_level);
                                    if (new_pref != null)
                                    {
                                        if (new_pref.isInTempMemory())
                                        {
                                            bt.addContextDependentPreference(new_pref);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        Preference pref = bt.trace.slot.getPreferencesByType(PreferenceType.PROHIBIT);
                        while (pref != null)
                        {
                            Preference new_pref = null;
                            if (pref.inst.match_goal_level == inst.match_goal_level
                                    && pref.isInTempMemory())
                            {
                                bt.addContextDependentPreference(pref);
                            }
                            else
                            {
                                new_pref = Preference.find_clone_for_level(
                                        pref, inst.match_goal_level);
                                if (new_pref != null)
                                {
                                    if (new_pref.isInTempMemory())
                                    {
                                        bt.addContextDependentPreference(new_pref);
                                    }
                                }
                            }
                            pref = pref.next;
                        }
                    }
                }
            }
        }
    }

    /**
     * Given an instantiation, this routines looks at the instantiated
     * conditions to find its match goal. It fills in inst->match_goal and
     * inst->match_goal_level. If there is a match goal, match_goal is set to
     * point to the goal identifier. If no goal was matched, match_goal is set
     * to NIL and match_goal_level is set to ATTRIBUTE_IMPASSE_LEVEL.
     * 
     * TODO Make a method of Instantiation?
     * 
     * <p>recmem.cpp:149:find_match_goal
     * 
     * @param inst
     */
    private static void find_match_goal(Instantiation inst)
    {
        IdentifierImpl lowest_goal_so_far = null;
        int lowest_level_so_far = -1;
        for (Condition cond = inst.top_of_instantiated_conditions; cond != null; cond = cond.next)
        {
            final PositiveCondition pc = cond.asPositiveCondition();
            if (pc != null)
            {
                final BackTraceInfo bt = pc.bt();
                final IdentifierImpl id = bt.wme_.id;
                if (id.isGoal())
                {
                    if (bt.level > lowest_level_so_far)
                    {
                        lowest_goal_so_far = id;
                        lowest_level_so_far = bt.level;
                    }
                }
            }
        }

        inst.match_goal = lowest_goal_so_far;
        if (lowest_goal_so_far != null)
        {
            inst.match_goal_level = lowest_level_so_far;
        }
        else
        {
            inst.match_goal_level = SoarConstants.ATTRIBUTE_IMPASSE_LEVEL;
        }
    }

    /**
     * Execute_action() executes a given RHS action. For MAKE_ACTION's, it
     * returns the created preference structure, or NIL if an error occurs. For
     * FUNCALL_ACTION's, it returns NIL.
     * 
     * Instantiate_rhs_value() returns the (symbol) instantiation of an
     * rhs_value, or NIL if an error occurs. It takes a new_id_level argument
     * indicating what goal_stack_level a new id is to be created at, in case a
     * gensym is needed for the instantiation of a variable. (although I'm not
     * sure this is really needed.)
     * 
     * As rhs unbound variables are encountered, they are instantiated with new
     * gensyms. These gensyms are then stored in the rhs_variable_bindings
     * array, so if the same unbound variable is encountered a second time it
     * will be instantiated with the same gensym.
     * 
     * recmem.cpp:195:instantiate_rhs_value
     * 
     * @param rv the RHS value from the production
     * @param new_id_level current id level to use
     * @param new_id_letter letter to use for ids
     * @param tok the token
     * @param w the wme
     * @return the instantiated value as a symbol
     */
    public SymbolImpl instantiate_rhs_value(RhsValue rv, int new_id_level, char new_id_letter, Token tok, WmeImpl w)
    {
        final RhsSymbolValue rsv = rv.asSymbolValue();
        if (rsv != null)
        {
            final SymbolImpl result = rsv.getSym();
            
            // See recmem.cpp:instantiate_rhs_value() for long-winded explanation of the following
            final IdentifierImpl resultAsId = result.asIdentifier();
            if(resultAsId != null && resultAsId.smem_lti != 0 && resultAsId.level == SemanticMemory.LTI_UNKNOWN_LEVEL)
            {
                resultAsId.level = new_id_level;
                resultAsId.promotion_level = new_id_level;
            }
            return result;
        }

        final UnboundVariable uv = rv.asUnboundVariable();
        if (uv != null)
        {

            int index = uv.getIndex();
            if (this.firer_highest_rhs_unboundvar_index < index)
            {
                this.firer_highest_rhs_unboundvar_index = index;
            }
            SymbolImpl sym = this.rete.getRhsVariableBinding(index);

            final SymbolFactoryImpl syms = predefinedSyms.getSyms();
            if (sym == null)
            {
                sym = syms.make_new_identifier(new_id_letter, new_id_level);
                this.rete.setRhsVariableBinding(index, sym);
                return sym;
            }
            else if (sym.asVariable() != null)
            {
                final Variable v = sym.asVariable();
                new_id_letter = v.getFirstLetter();
                sym = syms.make_new_identifier(new_id_letter, new_id_level);
                this.rete.setRhsVariableBinding(index, sym);
                return sym;
            }
            else
            {
                return sym;
            }
        }

        final ReteLocation rl = rv.asReteLocation();
        if (rl != null)
        {
            return rl.lookupSymbol(tok, w);
        }

        final RhsFunctionCall fc = rv.asFunctionCall();
        if (fc == null)
        {
            throw new IllegalStateException("Unknow RhsValue type: " + rv);
        }
        
        // build up list of argument values
        final List<Symbol> arguments = new ArrayList<Symbol>(fc.getArguments().size());
        boolean nil_arg_found = false;
        for (RhsValue arg : fc.getArguments())
        {
            SymbolImpl instArg = instantiate_rhs_value(arg, new_id_level, new_id_letter, tok, w);
            if (instArg == null)
            {
                nil_arg_found = true;
            }
            arguments.add(instArg);
        }

        // if all args were ok, call the function

        if (!nil_arg_found)
        {
            // stop the kernel timer while doing RHS funcalls KJC 11/04
            // the total_cpu timer needs to be updated in case RHS fun is
            // statsCmd
            ExecutionTimers.pause(context.getTotalKernelTimer());
            ExecutionTimers.update(context.getTotalCpuTimer());

            try
            {
                // we provide the RhsFunctionContext so we know this will return SymbolImpl
                return (SymbolImpl) context.getRhsFunctions().execute(fc.getName().getValue(), arguments);
            }
            catch (RhsFunctionException e)
            {
                logger.error("Error executing RHS function '" + fc.getName() + "' with args " + arguments + ": " + e.getMessage(), e);
                context.getPrinter().error("Error executing RHS function '%s' with args %s: %s\n", fc.getName(), arguments, e.getMessage());
            }
            finally
            {
                ExecutionTimers.start(context.getTotalKernelTimer());
            }
        }

        return null;
    }

    /**
     * <p>recmem.cpp:292:execute_action
     * 
     * @param a the action
     * @param tok the rete token
     * @param w the wme
     * @return the preference resulting from the action, possibly {@code null}
     */
    private Preference execute_action(Action a, Token tok, WmeImpl w)
    {
        rhsFunctionPreferenceType = a.preference_type;
        
        final FunctionAction fa = a.asFunctionAction();
        if (fa != null)
        {
            instantiate_rhs_value(fa.getCall(), -1, 'v', tok, w);
            return null;
        }

        final MakeAction ma = a.asMakeAction();

        final SymbolImpl idSym = instantiate_rhs_value(ma.id, -1, 's', tok, w);
        if (idSym == null)
        {
            return null; // goto abort_execute_action;
        }
        
        final IdentifierImpl id = idSym.asIdentifier();
        if (id == null)
        {
            context.getPrinter().error("[%s] RHS makes a preference for %s (not an identifier)\n", production_being_fired, idSym);
            return null; // goto abort_execute_action;
        }

        final SymbolImpl attr = instantiate_rhs_value(ma.attr, id.level, 'a', tok, w);
        if (attr == null)
        {
            return null;
        }
        
        final char first_letter = attr.getFirstLetter();

        final SymbolImpl value = instantiate_rhs_value(ma.value, id.level, first_letter, tok, w);
        if (value == null)
        {
            return null; // goto abort_execute_action;
        }

        SymbolImpl referent = null;
        if (a.preference_type.isBinary())
        {
            referent = instantiate_rhs_value(ma.referent, id.level, first_letter, tok, w);
            if (referent == null)
            {
                return null; // goto abort_execute_action;
            }
        }

        if (a.preference_type != PreferenceType.ACCEPTABLE && 
            a.preference_type != PreferenceType.REJECT     && 
            !(id.isGoal() && attr == predefinedSyms.operator_symbol))
        {
            context.getPrinter().error("[%s] attribute preference other than +/- for %s ^%s -- ignoring it.", production_being_fired, id, attr);
            return null;
        }
        
       /* The parser assumes that any rhs preference of the form 
        *
        * (<s> ^operator <o> = <x>)
        * 
        * is a binary indifferent preference, because it assumes <x> is an
        * operator. However, it could be the case that <x> is actually bound to
        * a number, which would make this a numeric indifferent preference. The
        * parser had no way of easily figuring this out, but it's easy to check
        * here.
        *
        * jsoar note: in csoar this was put in create_instantiation(). I (dr) put
        * it here so that Preference.type could remain final. Joseph said there
        * was no particular reason it was in create_instantiation().
        * 
        * jzxu April 22, 2009
        */
        PreferenceType type = a.preference_type;
        if (type == PreferenceType.BINARY_INDIFFERENT && 
            (referent.asDouble() != null || referent.asInteger() != null))
        {
            type = PreferenceType.NUMERIC_INDIFFERENT;
        }

        return new Preference(type, id, attr, value, referent);
    }

    /**
     * This routine fills in a newly created instantiation structure with
     * various information.
     *     
     * <p>At input, the instantiation should have:
     * <ul>
     *   <li>preferences_generated filled in; 
     *   <li>instantiated conditions filled in;
     *   <li>top-level positive conditions should have bt.wme_, bt.level, and
     *      bt.trace filled in, but bt.wme_ and bt.trace shouldn't have their
     *      reference counts incremented yet.
     * </ul>
     * 
     * This routine does the following:
     * <ul>
     *   <li>increments reference count on production;
     *   <li>fills in match_goal and match_goal_level;
     *   <li>for each top-level positive cond:
     *   <ul>
     *        <li>replaces bt.trace with the preference for the correct level,
     *        <li>updates reference counts on bt.pref and bt.wmetraces and wmes
     *   </ul>
     *   <li>for each preference_generated, adds that pref to the list of all
     *       pref's for the match goal
     *   <li>fills in backtrace_number;
     *   <li>if "need_to_do_support_calculations" is TRUE, calculates o-support
     *       for preferences_generated;
     * </ul>
     *   
     * <p>recmem.cpp:385:fill_in_new_instantiation_stuff
     * 
     * @param inst
     * @param need_to_do_support_calculations
     * @param top_goal
     */
    public void fill_in_new_instantiation_stuff(Instantiation inst, boolean need_to_do_support_calculations)
    {
        find_match_goal(inst);

        int level = inst.match_goal_level;

        /*
         * Note: since we'll never backtrace through instantiations at the top
         * level, it might make sense to not increment the reference counts on
         * the wmes and preferences here if the instantiation is at the top
         * level. As it stands now, we could gradually accumulate garbage at the
         * top level if we have a never-ending sequence of production firings at
         * the top level that chain on each other's results. (E.g., incrementing
         * a counter on every decision cycle.) I'm leaving it this way for now,
         * because if we go to S-Support, we'll (I think) need to save these
         * around (maybe).
         */

        /*
         * KJC 6/00: maintaining all the top level ref cts does have a big
         * impact on memory pool usage and also performance (due to malloc).
         * (See tests done by Scott Wallace Fall 99.) Therefore added
         * preprocessor macro so that by unsetting macro the top level ref cts
         * are not incremented. It's possible that in some systems, these ref
         * cts may be desireable: they can be added by defining
         * DO_TOP_LEVEL_REF_CTS
         */

        for (Condition cond = inst.top_of_instantiated_conditions; cond != null; cond = cond.next)
        {
            final PositiveCondition pc = cond.asPositiveCondition();
            if (pc != null)
            {
                // TODO This should all be a method on pc.bt. What should it be called?
                if (SoarConstants.DO_TOP_LEVEL_REF_CTS)
                {
                    // (removed in jsoar) pc.bt.wme_.wme_add_ref();
                }
                else
                {
                    if (level > SoarConstants.TOP_GOAL_LEVEL)
                    {
                        // (removed in jsoar) pc.bt.wme_.wme_add_ref();
                    }
                }
                // if trace is for a lower level, find one for this level
                final BackTraceInfo bt = pc.bt();
                if (bt.trace != null)
                {
                    if (bt.trace.inst.match_goal_level > level)
                    {
                        bt.trace = Preference.find_clone_for_level(bt.trace, level);
                    }
                    
                    if (SoarConstants.DO_TOP_LEVEL_REF_CTS)
                    {
                        if (bt.trace != null)
                            bt.trace.preference_add_ref();
                    }
                    else
                    {
                        if ((bt.trace != null) && (level > SoarConstants.TOP_GOAL_LEVEL))
                            bt.trace.preference_add_ref();
                    }
                }
            }
        }

        if (inst.match_goal != null)
        {
            for (Preference p = inst.preferences_generated; p != null; p = p.inst_next)
            {
                inst.match_goal.goalInfo.addGoalPreference(p);
                p.on_goal_list = true;
            }
        }
        inst.backtrace_number = 0;

        // former o_support_calculation_type (0 or 3 or 4)  test site
        
        // do calc's the normal Soar 6 way
        if (need_to_do_support_calculations)
        {
            osupport.calculate_support_for_instantiation_preferences(inst);
        }
    }
    
    /**
     * This builds the instantiation for a new match, and adds it to
     * newly_created_instantiations. It also calls chunk_instantiation() to do
     * any necessary chunk or justification building.
     * 
     * <p>recmem.cpp:548:create_instantiation
     * 
     * @param prod
     * @param tok
     * @param w
     * @param top_goal
     */
    private void create_instantiation(Production prod, Token tok, WmeImpl w)
    {
        final Trace trace = context.getTrace();
        
        // RPM workaround for bug #139: don't fire justifications
        // code moved to do_preference_phase
        assert prod.getType() != ProductionType.JUSTIFICATION;

        final Instantiation inst = new Instantiation(prod, tok, w);
        this.newly_created_instantiations = inst.insertAtHeadOfProdList(this.newly_created_instantiations);

        trace.print(Category.VERBOSE, "\n in create_instantiation: %s", inst.prod.getName());

        this.production_being_fired = inst.prod;
        prod.incrementFiringCount(); // TODO move into Instantiation constructor
        this.production_firing_count.increment();

        // build the instantiated conditions, and bind LHS variables
        ConditionsAndNots cans = this.rete.p_node_to_conditions_and_nots(prod.getReteNode(), tok, w, false);
        inst.top_of_instantiated_conditions = cans.top;
        inst.bottom_of_instantiated_conditions = cans.bottom;
        inst.nots = cans.nots;

        // record the level of each of the wmes that was positively tested
        for (Condition cond = inst.top_of_instantiated_conditions; cond != null; cond = cond.next)
        {
            final PositiveCondition pc = cond.asPositiveCondition();
            if (pc != null)
            {
                final BackTraceInfo bt = pc.bt();
                bt.level = bt.wme_.id.level;
                bt.trace = bt.wme_.preference;
            }
        }

        boolean trace_it = trace.isEnabled(inst.prod.getType().getTraceCategory()) || inst.prod.isTraceFirings();
        if(trace_it)
        {
            trace.startNewLine().print("Firing %s", inst);
        }
        
        // initialize rhs_variable_bindings array with names of variables
        // (if there are any stored on the production -- for chunks there won't be any)
        int index = 0;
        for (Variable c : prod.getRhsUnboundVariables())
        {
            this.rete.setRhsVariableBinding(index, c);
            index++;
        }
        this.firer_highest_rhs_unboundvar_index = index - 1;

        // Before executing the RHS actions, tell the user that the
        // phases has changed to output by printing the arrow
        if(trace_it && trace.isEnabled(Category.FIRINGS_PREFERENCES))
        {
            trace.startNewLine().print("-->");
        }

        // execute the RHS actions, collect the results
        assert inst.preferences_generated == null;
        boolean need_to_do_support_calculations = false;
        for (Action a = prod.getFirstAction(); a != null; a = a.next)
        {
            Preference pref = null;
            if (prod.getType() != ProductionType.TEMPLATE)
            {
                pref = execute_action(a, tok, w);
            }
            else
            {
                pref = null;
                /* SymbolImpl *result = */rl.rl_build_template_instantiation(inst, tok, w);
            }

            /*
             * SoarTech changed from an IF stmt to a WHILE loop to support
             * GlobalDeepCpy
             */
            while (pref != null)
            {
                pref.setInstantiation(inst);
                                
                if (inst.prod.getDeclaredSupport() == Support.DECLARED_O_SUPPORT)
                {
                    pref.o_supported = true;
                }
                else if (inst.prod.getDeclaredSupport() == Support.DECLARED_I_SUPPORT)
                {
                    pref.o_supported = false;
                }
                else
                {
                    pref.o_supported = this.FIRING_TYPE == SavedFiringType.PE_PRODS;
                }

                pref = !rhsFunctionPreferences.isEmpty() ? rhsFunctionPreferences.pop() : null;
            }
        }

        // reset rhs_variable_bindings array to all zeros
        index = 0;
        for (; index <= firer_highest_rhs_unboundvar_index; ++index)
        {
            this.rete.setRhsVariableBinding(index, null);
        }

        // fill in lots of other stuff
        fill_in_new_instantiation_stuff(inst, need_to_do_support_calculations);

        // print trace info: printing preferences 
        // Note: can't move this up, since fill_in_new_instantiation_stuff gives
        // the o-support info for the preferences we're about to print
        if (trace_it && trace.isEnabled(Category.FIRINGS_PREFERENCES))
        {
            trace.startNewLine();
            for (Preference pref = inst.preferences_generated; pref != null; pref = pref.inst_next)
            {
                trace.print("%s", pref);
            }
        }
        
        // Copy any context-dependent preferences for conditions of this instantiation
        build_CDPS(inst);

        this.production_being_fired = null;

        // build chunks/justifications if necessary
        final ByRef<Instantiation> new_created_byref = ByRef.create(newly_created_instantiations);
        this.chunker.chunk_instantiation(inst, false, new_created_byref);
        newly_created_instantiations = new_created_byref.value;

        // TODO callback FIRING_CALLBACK
        //   if (!thisAgent->system_halted) {
        //      soar_invoke_callbacks(thisAgent, FIRING_CALLBACK, (soar_call_data) inst);
        //   }
    }
    
    /**
     * Returns true if the function create_instantiation should run for this production.
     * Used to delay firing of matches in the inner preference loop.
     * 
     * @param prod
     * @param tok
     * @param w
     * @return false to abort firing
     */
    private boolean shouldCreateInstantiation(Production prod, Token tok, WmeImpl w)
    {
        if (decider.active_level == decider.highest_active_level)
        {
            return true;
        }
        
        if (prod.getType() == ProductionType.TEMPLATE)
        {
            return true;
        }

        // Scan RHS identifiers for their levels, don't fire those at or higher than the change level
        for (Action a = prod.getFirstAction(); a != null; a = a.next)
        {
            // These next three calls get the identifier which has the level,
            // skipping anything that isn't an identifier.
            MakeAction ma = a.asMakeAction();
            if (ma == null)
            {
                continue;
            }
            
            // Skip unbound variables
            if (ma.id.asUnboundVariable() != null)
            {
                continue;
            }
            
            // Get the symbol or determine that it is a function call
            SymbolImpl idSym = null;
            RhsSymbolValue rsv = ma.id.asSymbolValue();
            if (rsv != null)
            {
                idSym = rsv.getSym();
            } 
            else
            {
                ReteLocation rl = ma.id.asReteLocation();
                if (rl != null)
                {
                    idSym = rl.lookupSymbol(tok, w);
                }
                else
                {
                    // It's a function call, skip it.
                    continue;
                }
            }
            assert idSym != null;

            IdentifierImpl id = idSym.asIdentifier();
            if (id == null)
            {
                continue;
            }
            
            if (id.level <= decider.change_level)
            {
                context.getTrace().print(Category.WATERFALL, "*** Waterfall: aborting firing because (%s * *)" +
                        " level %d is on or higher (lower int) than change level %d\n", 
                        id, id.level, decider.change_level);
                return false;
            }
        }

        return true;
    }
    
    /**
     * This deallocates the given instantiation. This should only be invoked via
     * the possibly_deallocate_instantiation() macro.
     * 
     * <p>recmem.cpp:757:deallocate_instantiation
     * 
     * @param inst
     */
    private void deallocate_instantiation (Instantiation inst) 
    {
        Condition cond;
        //Preference pref;
        int level;

        Stack<Condition> cond_stack = new Stack<Condition>();
        List<Instantiation> inst_list = new LinkedList<Instantiation>();
        inst_list.add(inst);
        ListIterator<Instantiation> next_iter = inst_list.listIterator();

        while ( next_iter.hasNext() ) 
        {
            inst = next_iter.next();
            assert(inst != null);

            level = inst.match_goal_level;

            for (cond=inst.top_of_instantiated_conditions; cond!=null; cond=cond.next) 
            {
                final PositiveCondition pc = cond.asPositiveCondition();
                if (pc != null)
                {
                    final BackTraceInfo bt = pc.bt();
                    bt.clearContextDependentPreferenceSet(context);

                    /*      voigtjr, nlderbin:
                            We flattened out the following recursive loop in order to prevent a stack
                            overflow that happens when the chain of backtrace instantiations is very long:

                                    retract_instantiation
                                    possibly_deallocate_instantiation
                                    loop start:
                                    deallocate_instantiation (here)
                                    preference_remove_ref
                                    possibly_deallocate_preferences_and_clones
                                    deallocate_preference
                                    possibly_deallocate_instantiation
                                    goto loop start
                     */
                    if (!SoarConstants.DO_TOP_LEVEL_REF_CTS && level <= SoarConstants.TOP_GOAL_LEVEL)
                    {
                        continue;
                    }
                    else
                    {
                        // (removed in jsoar) wme_remove_ref (thisAgent, cond->bt.wme_);
                        final Preference trace = bt.trace;
                        if (trace != null) 
                        {
                            trace.reference_count--;
                            if (trace.reference_count == 0) 
                            {
                                //Preference clone;
                    
                                boolean has_active_clones = false;
                                for (Preference clone=trace.next_clone; clone != null; clone=clone.next_clone) 
                                {
                                    if ( clone.reference_count != 0 ) 
                                    {
                                        has_active_clones = true;
                                    }
                                }
                                if ( has_active_clones ) 
                                {
                                    continue;
                                }
                                for ( Preference clone = trace.prev_clone; clone != null; clone = clone.prev_clone ) 
                                {
                                    if ( clone.reference_count != 0 ) 
                                    {
                                        has_active_clones = true;
                                    }
                                }
                                if ( has_active_clones ) 
                                {
                                    continue;
                                }

                                // The clones are hopefully a simple case so we just call deallocate_preference on them.
                                // Someone needs to create a test case to push this boundary...
                                {
                                    Preference clone = trace.next_clone;
                                    Preference next;
                                    while (clone != null) {
                                        next = clone.next_clone;
                                        Preference.deallocate_preference (clone, this);
                                        clone = next;
                                    }
                                    clone = trace.prev_clone;
                                    while (clone != null) {
                                        next = clone.prev_clone;
                                        Preference.deallocate_preference (clone, this);
                                        clone = next;
                                    }
                                }

                                /* --- deallocate pref --- */
                                /* --- remove it from the list of bt.trace's for its match goal --- */
                                if ( trace.on_goal_list ) 
                                {
                                    // The code below is an expansion of this remove_from_dll macro...
                                    //remove_from_dll( 
                                    //        cond->bt.trace->inst->match_goal->id.preferences_from_goal, 
                                    //        cond->bt.trace, all_of_goal_next, all_of_goal_prev );
                                    trace.inst.match_goal.goalInfo.removeGoalPreference(trace);
                                }

                                /* --- remove it from the list of bt.trace's from that instantiation --- */
                                // The code below is an expansion of this remove_from_dll macro...
                                //remove_from_dll( cond->bt.trace->inst->preferences_generated, cond->bt.trace, inst_next, inst_prev );

                                trace.inst.removeGeneratedPreferece(trace);

                                if ( ( trace.inst.preferences_generated == null ) && ( !trace.inst.in_ms ) ) 
                                {
                                    //next_iter = inst_list.insert( next_iter, trace.inst );
                                    // TODO: there must be a better way -- this looks terribly inefficient and ugly
                                    int index = next_iter.previousIndex()+1;
                                    inst_list.add(index, trace.inst);
                                    next_iter = inst_list.listIterator(index);
                                }

                                cond_stack.push( cond );
                            } // if
                        } // if
                    } // if
                    /* voigtjr, nlderbin end */
                } // if
            } // for
        } // while

        //        // free condition symbols and pref
        //        while( !cond_stack.empty() ) 
        //        {
        //                condition* temp = cond_stack.top();
        //                cond_stack.pop();
        //
        //                /* --- dereference component symbols --- */
        //                symbol_remove_ref( thisAgent, temp->bt.trace->id );
        //                symbol_remove_ref( thisAgent, temp->bt.trace->attr );
        //                symbol_remove_ref( thisAgent, temp->bt.trace->value );
        //                if ( preference_is_binary( temp->bt.trace->type ) ) 
        //                {
        //                        symbol_remove_ref( thisAgent, temp->bt.trace->referent );
        //                }
        //
        //                if ( temp->bt.trace->wma_o_set )
        //                {
        //                        wma_remove_pref_o_set( thisAgent, temp->bt.trace );
        //                }
        //
        //                /* --- free the memory --- */
        //                free_with_pool( &thisAgent->preference_pool, temp->bt.trace );
        //        }

        //        // free instantiations in the reverse order
        //        std::list<instantiation*>::reverse_iterator riter = inst_list.rbegin();
        //        while( riter != inst_list.rend() ) 
        //        {
        //                instantiation* temp = *riter;
        //                ++riter;
        //
        //                deallocate_condition_list( thisAgent, temp->top_of_instantiated_conditions );
        //                deallocate_list_of_nots( thisAgent, temp->nots );
        //                if ( temp->prod ) 
        //                {
        //                        production_remove_ref( thisAgent, temp->prod );
        //                }
        //                free_with_pool( &thisAgent->instantiation_pool, temp );
        //        }
        
        // TODO: are these next loops really necessary?
        
        for(Condition temp : cond_stack)
        {
            final Preference trace = temp.asPositiveCondition().bt().trace;
            if(trace != null)
            {
                if(trace.type == PreferenceType.BINARY_INDIFFERENT)
                {
                    trace.referent = null;
                }
                trace.wma_o_set = null;
            }
            temp.asPositiveCondition().bt().trace = null;
            if(temp.next != null)
            {
                temp.next.prev = temp.prev;
            }
            if(temp.prev != null)
            {
                temp.prev.next = temp.next;
            }
            temp.next = temp.prev = null;
        }
        
        for(Instantiation temp : inst_list)
        {
            temp.top_of_instantiated_conditions = null;//  deallocate_condition_list (thisAgent, inst->top_of_instantiated_conditions);
            temp.bottom_of_instantiated_conditions = null; // This is very important to avoid memory leaks!
            temp.nots = null; //deallocate_list_of_nots (thisAgent, inst->nots);
            
            temp.prod = null;
        }
    }
    
    /**
     * <p>recmem.h:65:possibly_deallocate_instantiation
     * 
     * @param inst
     */
    void possibly_deallocate_instantiation(Instantiation inst)
    {
        if (inst.preferences_generated == null && !inst.in_ms)
            deallocate_instantiation(inst);
    }


    /**
     * This retracts the given instantiation.
     * 
     * recmem.cpp:814:retract_instantiation
     * 
     * @param inst
     */
    private void retract_instantiation(Instantiation inst)
    {
        // TODO callback RETRACTION_CALLBACK
        // soar_invoke_callbacks(thisAgent, RETRACTION_CALLBACK, (soar_call_data) inst);

        boolean retracted_a_preference = false;

        final Trace trace = context.getTrace();
        final boolean trace_it = trace.isEnabled(inst.prod.getType().getTraceCategory()) || inst.prod.isTraceFirings();

        // retract any preferences that are in TM and aren't o-supported
        Preference pref = inst.preferences_generated;

        while (pref != null)
        {
            final Preference next = pref.inst_next;
            if (pref.isInTempMemory() && !pref.o_supported)
            {
                if (trace_it) {
                    if (!retracted_a_preference) 
                    {
                        trace.startNewLine().print("Retracting %s", inst);
                        if(trace.isEnabled(Category.FIRINGS_PREFERENCES))
                        {
                            trace.startNewLine().print("-->");
                        }
                    }
                    if(trace.isEnabled(Category.FIRINGS_PREFERENCES))
                    {
                        trace.startNewLine().print("%s", pref);
                    }
                }
                remove_preference_from_tm(pref);
                retracted_a_preference = true;
            }
            pref = next;
        }

        // remove inst from list of instantiations of this production
        inst.prod.instantiations = inst.removeFromProdList(inst.prod.instantiations);

        // if retracting a justification, excise it
        /*
         * if the reference_count on the production is 1 (or less) then the only
         * thing supporting this justification is the instantiation, hence it
         * has already been excised, and doing it again is wrong.
         */
        // Removed reference count check because it seemed unnecessary
        // and never made a difference in the tests. If this comes up, it seems
        // better to just make exciseProduction smarter so it doesn't crash if
        // a production is excised multiple times.
        if (inst.prod.getType() == ProductionType.JUSTIFICATION /*&& inst.prod.getReferenceCount() > 1*/)
        {
            context.getProductions().exciseProduction(inst.prod, false);
        }

        // mark as no longer in MS, and possibly deallocate
        inst.in_ms = false;
        possibly_deallocate_instantiation(inst);
    }

    /**
     * This routine scans through newly_created_instantiations, asserting each
     * preference generated except for o-rejects. It also removes each
     * instantiation from newly_created_instantiations, linking each onto the
     * list of instantiations for that particular production. O-rejects are
     * buffered and handled after everything else.
     * 
     * <p>Note that some instantiations on newly_created_instantiations are not in
     * the match set--for the initial instantiations of chunks/justifications,
     * if they don't match WM, we have to assert the o-supported preferences and
     * throw away the rest.
     * 
     * <p>recmem.cpp:891:assert_new_preferences
     * 
     * @bufdeallo Buffered preferences to deallocate after inner elaboration loop.
     */
    private void assert_new_preferences(List<Preference> bufdeallo)
    {
        final Trace trace = context.getTrace();
        // Note: In CSoar, this list is just build up using the next link in the
        // Preference object. When I tried to do that, I was getting some occasional
        // weird behavior. So, since this list is really supposed to be independent
        // for this function anyway, why not just use a normal list? Yay.
        final LinkedList<Preference> o_rejects = new LinkedList<Preference>();

        trace.print(Category.VERBOSE, "\n in assert_new_preferences:");

        
        // Do an initial loop to process o-rejects, then re-loop to process
        // normal preferences.
        Instantiation inst, next_inst;
        for (inst = this.newly_created_instantiations; inst != null; inst = next_inst)
        {
            next_inst = inst.nextInProdList;

            Preference pref, next_pref;
            for (pref = inst.preferences_generated; pref != null; pref = next_pref)
            {
                next_pref = pref.inst_next;
                if ((pref.type == PreferenceType.REJECT) && (pref.o_supported))
                {
                    // o-reject: just put it in the buffer for later
                    o_rejects.push(pref);
                }
            }
        }

        if (!o_rejects.isEmpty())
            process_o_rejects_and_deallocate_them(o_rejects, bufdeallo);
        
        // Note: In CSoar there is some random code commented out at this point. Is it important? Who knows?

        for (inst = this.newly_created_instantiations; inst != null; inst = next_inst)
        {
            next_inst = inst.nextInProdList;
            if (inst.in_ms)
            {
                inst.prod.instantiations = inst.insertAtHeadOfProdList(inst.prod.instantiations);
				trace.print(Category.VERBOSE, "\n asserting instantiation: %s\n", inst.prod.getName());
            }


            Preference pref, next_pref;
            for (pref = inst.preferences_generated; pref != null; pref = next_pref)
            {
                next_pref = pref.inst_next;
                
                // removed old code specific to !O_REJECTS_FIRST, which isn't supported by jsoar
                
                if ( inst.in_ms || pref.o_supported )
                {
                    // normal case
                    add_preference_to_tm(pref);
                    // No knowledge retrieval necessary in Operand2
                    
                    if (wma.wma_enabled())
                    {
                        wma.wma_activate_wmes_in_pref(pref);
                    }
                }
                else
                {
                    // inst. is refracted chunk, and pref. is not o-supported: remove the preference

                    // first splice it out of the clones list--otherwise we 
                    // might accidentally deallocate some clone that happens to
                    // have refcount==0 just because it hasn't been asserted yet
                    if (pref.next_clone != null)
                        pref.next_clone.prev_clone = pref.prev_clone;
                    if (pref.prev_clone != null)
                        pref.prev_clone.next_clone = pref.next_clone;
                    pref.next_clone = pref.prev_clone = null;

                    // now add then remove ref--this should result in deallocation
                    pref.preference_add_ref();
                    pref.preference_remove_ref(this);
                }
            }
        }
    }
    
    /**
     * Process_o_rejects_and_deallocate_them() handles the processing of
     * o-supported reject preferences. This routine is called from the firer and
     * passed a list of all the o-rejects generated in the current preference
     * phases (the list is linked via the "next" fields on the preference
     * structures). This routine removes all preferences for matching values
     * from TM, and deallocates the o-reject preferences when done.
     * 
     * prefmem.cpp:330:process_o_rejects_and_deallocate_them
     * 
     * @param o_rejects
     * @param bufdeallo Preferences buffered for deallocation after inner
     * elaboration loop completes.
     */
    private void process_o_rejects_and_deallocate_them(List<Preference> o_rejects, List<Preference> bufdeallo)
    {
        for (Preference pref : o_rejects)
        {
            // prevents it from being deallocated if it's a clone of some other 
            // pref we're about to remove
            pref.preference_add_ref(); 
            // #ifdef DEBUG_PREFS
            // print (thisAgent, "\nO-reject posted at 0x%8x: ",(unsigned
            // long)pref);
            // print_preference (thisAgent, pref);
            // #endif
        }

        for(Preference pref : o_rejects)
        {
            Slot s = Slot.find_slot(pref.id, pref.attr);
            if (s != null)
            {
                // remove all pref's in the slot that have the same value
                Preference p = s.getAllPreferences();
                while (p != null)
                {
                    final Preference next_p = p.nextOfSlot;
                    if (p.value == pref.value)
                    {
                        // Buffer deallocation by adding a reference here and putting it
                        // on a list. These are deallocated after the inner elaboration
                        // loop completes.
                        p.preference_add_ref();
                        bufdeallo.add(p);
                        remove_preference_from_tm(p);
                    }
                    p = next_p;
                }
            }
            pref.preference_remove_ref(this);
        }
    }
    
    /**
     * Add_preference_to_tm() adds a given preference to preference memory (and
     * hence temporary memory). 
     * 
     * <p>prefmem.cpp:216:add_preference_to_tm
     * 
     * TODO I wish this was private, but smem and epmem use it.
     * 
     * @param pref
     */
    public void add_preference_to_tm (Preference pref) 
    {
//    #ifdef DEBUG_PREFS
//       print (thisAgent, "\nAdd preference at 0x%8x:  ",reinterpret_cast<uintptr_t>(pref));
//       print_preference (thisAgent, pref);
//    #endif
       
       Slot s = Slot.make_slot( pref.id, pref.attr, predefinedSyms.operator_symbol );
       
       pref.slot = s;
       s.addPreference(pref); // note this handles inserting at the head of the all_preferences list and inserting into the correct preference type list
       
       /* --- other miscellaneous stuff --- */    
       pref.preference_add_ref();
       
       // if it's the case that the slot is unchanged, but has
       // some references laying around, clear them
       // this doesn't cause immediate memory deallocate/allocate
       // but once the WMEs are resolved, this should free the
       // memory, as opposed to lead to a "leak"
       if ( wma.wma_enabled() && !s.isa_context_slot )
       {
           if ( s.changed == null )
           {
               if ( s.wma_val_references != null )
               {
                   s.wma_val_references.clear();
               }
           }
       }

       tempMemory.mark_slot_as_changed(s);

       if ( wma.wma_enabled() && !s.isa_context_slot )
       {
          boolean exists = false;
          WmeImpl w = pref.slot.getWmes();
          while ( !exists && w != null )
          {
             if ( w.getValue() == pref.value )
             {
                exists = true;
             }

             w = w.next;
          }

          // if wme exists, it should already have been updated
          // during assertion of new preferences
          if ( !exists )
          {
             if ( s.wma_val_references == null )
             {
                 s.wma_val_references = new HashMap<Symbol, Long>();
             }

             Long numRef = s.wma_val_references.get(pref.value);
             if(numRef == null)
             {
                 numRef = 0L;
             }
             
             s.wma_val_references.put(pref.value, ++numRef);
          }
       }
       
       // --- update identifier levels ---
       IdentifierImpl valueId = pref.value.asIdentifier();
       if (valueId != null)
       {
           decider.post_link_addition (pref.id, valueId);
       }

       if (pref.type.isBinary())
       {
           IdentifierImpl refId = pref.referent.asIdentifier();
           if (refId != null)
           {
               decider.post_link_addition (pref.id, refId);
           }
       }
       
       // if acceptable/require pref for context slot, we may need to add a wme
       // later
       if (s.isa_context_slot
               && (pref.type == PreferenceType.ACCEPTABLE || 
                   pref.type == PreferenceType.REQUIRE))
       {
           decider.mark_context_slot_as_acceptable_preference_changed (s);
       }
    }

    /**
     * removes a given preference from PM and TM.
     * 
     * prefmem.cpp:282:remove_preference_from_tm
     * 
     * @param pref
     */
    public void remove_preference_from_tm(Preference pref)
    {
        Slot s = pref.slot;

        // #ifdef DEBUG_PREFS
        // print (thisAgent, "\nRemove preference at 0x%8x: ",(unsigned
        // long)pref);
        // print_preference (thisAgent, pref);
        // #endif

        // remove preference from the list for the slot
        s.removePreference(pref);

        // other miscellaneous stuff

        tempMemory.mark_slot_as_changed(s);

        /// if acceptable/require pref for context slot, we may need to remove a wme later
        if ((s.isa_context_slot)
                && ((pref.type == PreferenceType.ACCEPTABLE) || (pref.type == PreferenceType.REQUIRE)))
        {
            decider.mark_context_slot_as_acceptable_preference_changed(s);
        }

        // update identifier levels
        IdentifierImpl valueId = pref.value.asIdentifier();
        if (valueId != null)
        {
            decider.post_link_removal (pref.id, valueId);
        }
        if (pref.type.isBinary())
        {
            IdentifierImpl refId = pref.referent.asIdentifier();
            if (refId != null)
            {
                decider.post_link_removal (pref.id, refId);
            }
        }

        // deallocate it and clones if possible
        pref.preference_remove_ref(this);
    }

    /**
     * This routine is called from the top level to run the preference phases.
     * 
     * <p>recmem.cpp:1035:do_preference_phase
     * 
     * @param top_goal
     */
    public void do_preference_phase(IdentifierImpl top_goal)
    {
        /*
         * AGR 617/634: These are 2 bug reports that report the same problem,
         * namely that when 2 chunk firings happen in succession, there is an
         * extra newline printed out. The simple fix is to monitor
         * get_printer_output_column and see if it's at the beginning of a line
         * or not when we're ready to print a newline. 94.11.14
         */

        final Trace trace = context.getTrace();
        if (trace.isEnabled(Category.PHASES))
        {
            if (this.decisionCycle.current_phase.get() == Phase.APPLY)
            { /* it's always IE for PROPOSE */
                switch (FIRING_TYPE)
                {
                case PE_PRODS:
                    trace.startNewLine().print("--- Firing Productions (PE) For State At Depth %d ---",
                            decider.active_level);
                    break;
                case IE_PRODS:
                    trace.startNewLine().print("--- Firing Productions (IE) For State At Depth %d ---",
                            decider.active_level);
                    break;
                }
            }
        }
        
        if(wma.wma_enabled())
        {
            soarReteListener.wma_activate_wmes_tested_in_prods();
        }
        
        // Save previous active level for usage on next elaboration cycle.
        decider.highest_active_level = decider.active_level;
        decider.highest_active_goal = decider.active_goal;

        decider.change_level = decider.highest_active_level;
        decider.next_change_level = decider.highest_active_level;
        
        // Temporary list to buffer deallocation of some preferences until 
        // the inner elaboration loop is over.
        List<Preference> bufdeallo = Lists.newArrayList();
        
        // FIXME: should not do this inner elaboration loop for soar 7 mode.
        for (;;)
        {
            // Inner elaboration loop
            decider.change_level = decider.next_change_level;
            
            if (trace.isEnabled(Category.WATERFALL))
            {
                trace.print("--- Inner Elaboration Phase, active level: %d",
                        decider.active_level);
                if (decider.active_goal != null)
                {
                    trace.print(" (%s)", decider.active_goal);
                }
                trace.print(" ---\n");
            }
            
            this.newly_created_instantiations = null;
            
            SoarReteAssertion assertion = null;
            boolean assertionsExist = false;
            while ((assertion = this.soarReteListener.postpone_assertion()) != null)
            {
                assertionsExist = true;
                
                if(this.chunker.isMaxChunksReached()) 
                {
                    this.soarReteListener.consume_last_postponed_assertion();
                    this.decisionCycle.halt("Max chunks reached");
                    return;
                }
                
                if (assertion.production.getType() == ProductionType.JUSTIFICATION)
                {
                    this.soarReteListener.consume_last_postponed_assertion();

                    // don't fire justifications
                    continue;
                }
                
                if (shouldCreateInstantiation(assertion.production, assertion.token, assertion.wme))
                {
                    this.soarReteListener.consume_last_postponed_assertion();
                    create_instantiation(assertion.production, assertion.token, assertion.wme);
                }
            }
            
            // New waterfall model: something fired or is pending to fire at this level, 
            // so this active level becomes the next change level.
            if (assertionsExist)
            {
                if (decider.active_level > decider.next_change_level) 
                {
                    decider.next_change_level = decider.active_level;
                }
            }
            
            // New waterfall model: push unfired matches back on to the assertion lists
            this.soarReteListener.restore_postponed_assertions();
            
            assert_new_preferences(bufdeallo);
    
            // update accounting
            this.decisionCycle.inner_e_cycle_count.increment();
            
            if (decider.active_goal == null)
            {
                trace.print(Category.WATERFALL, " inner preference loop doesn't have active goal.\n");
                break;
            }
            
            if (decider.active_goal.goalInfo.lower_goal == null)
            {
                trace.print(Category.WATERFALL, " inner preference loop at bottom goal.\n");
                break;
            }
            
            
            if (this.decisionCycle.current_phase.get() == Phase.APPLY)
            {
                decider.active_goal = this.consistency.highest_active_goal_apply(decider.active_goal.goalInfo.lower_goal, true);
            }
            else if (this.decisionCycle.current_phase.get() == Phase.PROPOSE)
            {
                // PROPOSE
                decider.active_goal = this.consistency.highest_active_goal_propose(decider.active_goal.goalInfo.lower_goal, true);
            } 
            
            if(decider.active_goal != null)
            {
                decider.active_level = decider.active_goal.level;
            }
            else
            {
                trace.print(Category.WATERFALL, " inner preference loop finished but not at quiescence.\n");
                break;
            }
            
        } // inner elaboration loop/cycle end
        
        // Deallocate preferences delayed during inner elaboration loop.
        for (Preference p : bufdeallo)
            p.preference_remove_ref(this);

        // Restore previous active level.
        decider.active_level = decider.highest_active_level;
        decider.active_goal = decider.highest_active_goal;
        
        Instantiation inst = null;
        while ((inst = this.soarReteListener.get_next_retraction()) != null)
        {
            retract_instantiation(inst);
        }

        /*
         * In Waterfall, if there are nil goal retractions, then we want to
         * retract them as well, even though they are not associated with any
         * particular goal (because their goal has been deleted). The
         * functionality of this separate routine could have been easily
         * combined in get_next_retraction but I wanted to highlight the
         * distinction between regualr retractions (those that can be mapped
         * onto a goal) and nil goal retractions that require a special data
         * strucutre (because they don't appear on any goal) REW.
         */

        if (this.soarReteListener.hasNilGoalRetractions())
        {
            while ((inst = this.soarReteListener.get_next_nil_goal_retraction()) != null)
            {
                retract_instantiation(inst);
            }
        }
    }

    private class RhsFunctionContextImpl implements RhsFunctionContext
    {
        /* (non-Javadoc)
         * @see org.jsoar.kernel.rhs.functions.RhsFunctionContext#getSymbols()
         */
        @Override
        public SymbolFactory getSymbols()
        {
            return context.getSymbols();
        }

        /* (non-Javadoc)
         * @see org.jsoar.kernel.rhs.functions.RhsFunctionContext#addWme(org.jsoar.kernel.symbols.Identifier, org.jsoar.kernel.symbols.Symbol, org.jsoar.kernel.symbols.Symbol)
         */
        @Override
        public Void addWme(Identifier id, Symbol attr, Symbol value)
        {
            Arguments.checkNotNull(id, "id");
            Arguments.checkNotNull(attr, "attr");
            Arguments.checkNotNull(value, "value");
            
            Preference p = new Preference(rhsFunctionPreferenceType, 
                                          (IdentifierImpl) id, (SymbolImpl) attr, (SymbolImpl) value, 
                                          null);
            rhsFunctionPreferences.add(p);
            return null; // Void
        }

        /* (non-Javadoc)
         * @see org.jsoar.kernel.rhs.functions.RhsFunctionContext#getProductionBeingFired()
         */
        @Override
        public Production getProductionBeingFired()
        {
            return production_being_fired;
        }
        
    }
}
