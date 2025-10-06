package at.aau.serg.util;

import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;

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

    /**
     * Resolves a method declaration from an executable reference by first attempting
     * direct resolution, then searching through the declaring type's methods.
     *
     * @param executableRef the executable reference to resolve
     * @return the corresponding method declaration, or null if not found
     */
    public static CtMethod<?> getMethodDeclaration(CtExecutableReference<?> executableRef) {
        try {
            // try get declaration directly
            var declaration = executableRef.getExecutableDeclaration();
            if (declaration instanceof CtMethod<?>) {
                return (CtMethod<?>) declaration;
            }

            // search for declaring type
            CtTypeReference<?> declaringType = executableRef.getDeclaringType();
            if (declaringType != null) {
                CtType<?> typeDeclaration = declaringType.getTypeDeclaration();
                if (typeDeclaration != null) {
                    for (CtMethod<?> method : typeDeclaration.getMethods()) {
                        if (isMethodSignatureMatch(method, executableRef)) {
                            return method;
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Could not resolve method declaration for: " + executableRef.getSignature() + " - " + e.getMessage());
        }

        return null;
    }

    /**
     * Checks if a method's signature matches an executable reference by comparing
     * method name, parameter count, and parameter types.
     *
     * @param method        the method to check
     * @param executableRef the executable reference to match against
     * @return true if the signatures match, false otherwise
     */
    public static boolean isMethodSignatureMatch(CtMethod<?> method, CtExecutableReference<?> executableRef) {
        if (!method.getSimpleName().equals(executableRef.getSimpleName())) {
            return false;
        }

        List<CtTypeReference<?>> refParams = executableRef.getParameters();
        List<CtParameter<?>> methodParams = method.getParameters();

        if (refParams.size() != methodParams.size()) {
            return false;
        }

        for (int i = 0; i < refParams.size(); i++) {
            if (!SpoonUtils.areTypeReferencesEquivalent(refParams.get(i), methodParams.get(i).getType())) {
                return false;
            }
        }

        return true;
    }
}
