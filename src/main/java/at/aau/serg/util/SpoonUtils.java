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

    /**
     * Returns true, if the ctType is of exact type Object (not a subtype).
     *
     * @param ctType
     * @return
     */
    public static boolean isObjectType(CtTypeReference<?> ctType) {
        return ctType != null && ctType.equals(new TypeFactory().objectType());
    }

    /**
     * Compares two CtTypeReference objects for equivalence.
     *
     * <p>Two type references are considered equivalent if they are both null,
     * equal according to their equals() method, or have the same qualified name.
     *
     * @param type1 the first type reference to compare
     * @param type2 the second type reference to compare
     * @return true if the type references are equivalent, false otherwise
     */
    public static boolean areTypeReferencesEquivalent(CtTypeReference<?> type1, CtTypeReference<?> type2) {
        if (type1 == null && type2 == null) {
            return true;
        }
        if (type1 == null || type2 == null) {
            return false;
        }

        if (type1.equals(type2)) {
            return true;
        }

        String qualifiedName1 = type1.getQualifiedName();
        String qualifiedName2 = type2.getQualifiedName();

        return qualifiedName1 != null && qualifiedName1.equals(qualifiedName2);
    }
}
