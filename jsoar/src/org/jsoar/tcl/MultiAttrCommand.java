package org.jsoar.tcl;

import org.jsoar.kernel.symbols.StringSymbolImpl;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;

/**
 * @author ray
 */
final class MultiAttrCommand implements Command
{
    /**
     * 
     */
    private final SoarTclInterface ifc;

    /**
     * @param soarTclInterface
     */
    MultiAttrCommand(SoarTclInterface soarTclInterface)
    {
        ifc = soarTclInterface;
    }

    @Override
    public void cmdProc(Interp interp, TclObject[] args) throws TclException
    {
        if(args.length != 3)
        {
            throw new TclNumArgsException(interp, 2, args, "attr cost");
        }
        
        StringSymbolImpl attr = ifc.agent.syms.createString(args[1].toString());
        int cost = Integer.valueOf(args[2].toString());
        ifc.agent.getMultiAttributes().setCost(attr, cost);
    }
}