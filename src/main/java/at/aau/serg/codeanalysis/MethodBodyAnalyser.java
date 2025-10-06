package at.aau.serg.codeanalysis;

import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;

import java.util.Optional;

public class MethodBodyAnalyser {

    public boolean mayCompleteNormallyWithoutEarlyCompletion(CtMethod<?> m) {
        CtBlock<?> body = m.getBody();

        if (body == null)
            return false;

        return blockFallsThrough(body);
    }

    private boolean blockFallsThrough(CtBlock<?> block) {
        // check the content of every block
        for (CtStatement s : block.getStatements()) {
            if (statementFallsThrough(s))
                continue;
            else
                // abrupt completion; nothing after it is reachable
                return false;

        }
        // reached the end without guaranteed abrupt completion
        return true;
    }

    private boolean statementFallsThrough(CtStatement s) {
        if (s instanceof CtBlock<?> block) {
            // 2-method recursive call
            blockFallsThrough(block);
        }

        if (s instanceof CtReturn<?> || s instanceof CtThrow) {
            return false; // abrupt completion
        }

        if (s instanceof CtIf ctIf) {
            boolean thenFT = ctIf.getThenStatement() == null || statementFallsThrough(ctIf.getThenStatement());
            boolean elseFT = ctIf.getElseStatement() == null || statementFallsThrough(ctIf.getElseStatement());
            // if either branch can fall through, the if as a whole can fall through
            return thenFT || elseFT;
        }

        if (s instanceof CtTry ctTry) {
            boolean tryFT = statementFallsThrough(ctTry.getBody());
            boolean anyCatchFT = ctTry.getCatchers().isEmpty() || ctTry.getCatchers().stream()
                    .anyMatch(c -> statementFallsThrough(c.getBody()));
            boolean finallyFT = Optional.ofNullable(ctTry.getFinalizer())
                    .map(this::statementFallsThrough).orElse(true);
            // if try+any catch can fall through AND finally (if present) can fall through
            return (tryFT || anyCatchFT) && finallyFT;
        }

        if (s instanceof CtWhile || s instanceof CtFor || s instanceof CtDo) {
            // conservative: loops may not execute, finish, or may break
            return true;
        }

        if (s instanceof CtSwitch<?>) {
            // conservative: assume at least one case (or default) can fall through to after the switch
            return true;
        }

        // Expression, declaration, assignment, method call, etc.
        return true;
    }


}
