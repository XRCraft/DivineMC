package org.bxteam.divinemc.entity.pathfinding;

import net.minecraft.world.level.pathfinder.NodeEvaluator;
import org.jetbrains.annotations.NotNull;

public interface NodeEvaluatorGenerator {
    @NotNull NodeEvaluator generate(NodeEvaluatorFeatures nodeEvaluatorFeatures);
}
