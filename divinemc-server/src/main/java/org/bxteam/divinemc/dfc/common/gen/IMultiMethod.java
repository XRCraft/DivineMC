package org.bxteam.divinemc.dfc.common.gen;

import org.bxteam.divinemc.dfc.common.ast.EvalType;
import org.bxteam.divinemc.dfc.common.util.ArrayCache;

@FunctionalInterface
public interface IMultiMethod {
    void evalMulti(double[] var1, int[] var2, int[] var3, int[] var4, EvalType var5, ArrayCache var6);
}
