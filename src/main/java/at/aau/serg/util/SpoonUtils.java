package at.aau.serg.util;

import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;

public class SpoonUtils {

    /**
     * Checks if the types are equivalent, also accepting <em>ctType subtypeOf type</em>.
     *
     * @param ctType
     * @param type
     * @return
     */
    public static boolean isTypeEquivalent(CtTypeReference<?> ctType, Class<?> type) {
        return ctType != null && ctType.isSubtypeOf(new TypeFactory().get(type).getReference());
    }

}
