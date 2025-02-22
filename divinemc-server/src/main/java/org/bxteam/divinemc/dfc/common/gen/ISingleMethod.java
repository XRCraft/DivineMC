package org.bxteam.divinemc.dfc.common.gen;

import org.bxteam.divinemc.dfc.common.ast.EvalType;

@FunctionalInterface
public interface ISingleMethod {
    double evalSingle(int var1, int var2, int var3, EvalType var4);
}
