/*
 * (c) 2008  Dave Ray
 *
 * Created on Aug 15, 2008
 */
package org.jsoar.kernel.symbols;

import org.jsoar.kernel.DeciderFlag;
import org.jsoar.kernel.lhs.EqualityTest;
import org.jsoar.kernel.memory.Wme;
import org.jsoar.util.ListHead;

/**
 * 
 * <p>symtab.h:251:SymbolImpl
 * 
 * @author ray
 */
public abstract class SymbolImpl extends EqualityTest implements Symbol
{
    public DeciderFlag decider_flag;
    public Wme decider_wme;
    public int retesave_symindex;
    public final int hash_id;
    
    /*package*/ SymbolImpl(int hash_id)
    {
        this.hash_id = hash_id;
    }
    
    /* (non-Javadoc)
     * @see org.jsoar.kernel.symbols.Symbol#asFloatConstant()
     */
    public FloatConstant asFloatConstant()
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.jsoar.kernel.symbols.Symbol#asIntConstant()
     */
    public IntConstant asIntConstant()
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.jsoar.kernel.symbols.Symbol#asSymConstant()
     */
    public SymConstant asSymConstant()
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.jsoar.kernel.symbols.Symbol#asVariable()
     */
    public Variable asVariable()
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.jsoar.kernel.symbols.Symbol#asIdentifier()
     */
    public Identifier asIdentifier()
    {
        return null;
    }
    
    /**
     * @return
     */
    public char getFirstLetter()
    {
        return '*';
    }
    
    /**
     * Return true if this symbol is the same type as other symbol. This is
     * a replacement for comparing symbol type enums in the C kernel
     * 
     * @param other The symbol to test against
     * @return True if this symbol has the same type as other
     */
    public abstract boolean isSameTypeAs(SymbolImpl other);
    
    /**
     * Return true if this symbol's numeric value is less than the numeric value of
     * other. If neither symbol is numeric, returns false.
     * 
     * @param other SymbolImpl to compare against
     * @return Result of numeric less-than comparison
     */
    public boolean numericLess(SymbolImpl other)
    {
        return false;
    }
    
    /**
     * Return true if this symbol's numeric value is less than or equal to the numeric value of
     * other. If neither symbol is numeric, returns false.
     * 
     * @param other SymbolImpl to compare against
     * @return Result of numeric less-than comparison
     */
    public boolean numericLessOrEqual(SymbolImpl other)
    {
        return false;
    }
    
    /**
     * Return true if this symbol's numeric value is greater than the numeric value of
     * other. If neither symbol is numeric, returns false.
     * 
     * @param other SymbolImpl to compare against
     * @return Result of numeric less-than comparison
     */
    public boolean numericGreater(SymbolImpl other)
    {
        return false;
    }
    
    /**
     * Return true if this symbol's numeric value is greater than or equal to the numeric value of
     * other. If neither symbol is numeric, returns false.
     * 
     * @param other SymbolImpl to compare against
     * @return Result of numeric less-than comparison
     */
    public boolean numericGreaterOrEqual(SymbolImpl other)
    {
        return false;
    }

    /**
     * <p>production.cpp:1299:add_symbol_to_tc
     * 
     * @param tc
     * @param id_list
     * @param var_list
     */
    public void add_symbol_to_tc(int tc, ListHead<Identifier> id_list, ListHead<Variable> var_list)
    {
        // DO nothing by default
    }
    
    /**
     * <p>production.cpp:1346:symbol_is_in_tc
     * 
     * @param tc
     * @return
     */
    public boolean symbol_is_in_tc(int tc)
    {
        // False by default
        return false;
    }
    
    public static EqualityTest makeEqualityTest(SymbolImpl sym)
    {
        return sym;
    }
    
    /* (non-Javadoc)
     * @see org.jsoar.kernel.lhs.EqualityTest#getReferent()
     */
    @Override
    public SymbolImpl getReferent()
    {
        return this;
    }
    
    
}