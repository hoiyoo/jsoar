package org.jsoar.kernel.rhs.functions;

import java.util.List;

import org.jsoar.kernel.symbols.ISymbolFactory;
import org.jsoar.kernel.symbols.IntegerSymbol;
import org.jsoar.kernel.symbols.Symbol;

/**
 * Takes one or more int_constant or float_constant arguments.
 * If 0 arguments, returns NIL (error).
 * If 1 argument (x), returns -x.
 * If >=2 arguments (x, y1, ..., yk), returns x - y1 - ... - yk.
 *  
 * rhsfun_math.cpp:125:minus_rhs_function_code
 */
public final class Minus extends AbstractRhsFunctionHandler
{
    /**
     * @param name
     */
    public Minus()
    {
        super("-");
    }

    @Override
    public Symbol execute(ISymbolFactory syms, List<Symbol> arguments) throws RhsFunctionException
    {
        RhsFunctionTools.checkAllArgumentsAreNumeric(getName(), arguments);
        RhsFunctionTools.checkArgumentCount(getName(), arguments, 1, Integer.MAX_VALUE);
        
        Symbol arg = arguments.get(0);
        if(arguments.size() == 1)
        {
            IntegerSymbol i = arg.asIntConstant();
            
            return i != null ? syms.make_int_constant(-i.getValue()) : 
                               syms.make_float_constant(-arg.asFloatConstant().getValue());
        }
        
        int i = 0;
        double f = 0;
        boolean float_found = false;
        IntegerSymbol ic = arg.asIntConstant();
        if(ic != null)
        {
            i = ic.getValue();
        }
        else
        {
            float_found = true;
            f = arg.asFloatConstant().getValue();
        }
        for(int index = 1; index < arguments.size(); ++index)
        {
            arg = arguments.get(index);
            
            ic = arg.asIntConstant();
            if(ic != null)
            {
                if(float_found)
                {
                    f -= ic.getValue();
                }
                else
                {
                    i -= ic.getValue();
                }
            }
            else
            {
                if(float_found)
                {
                    f -= arg.asFloatConstant().getValue();
                }
                else
                {
                    float_found = true;
                    f = i - arg.asFloatConstant().getValue();
                }
            }
        }
        
        return float_found ? syms.make_float_constant(f) : syms.make_int_constant(i);
    }
}