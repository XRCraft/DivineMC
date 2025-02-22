package org.bxteam.divinemc.util;

import org.jetbrains.annotations.Nullable;

public final class StringUtil {
    public static boolean hasLength(@Nullable String str) {
        return (str != null && !str.isEmpty());
    }
}
