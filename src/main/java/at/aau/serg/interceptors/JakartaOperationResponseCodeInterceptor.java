package at.aau.serg.interceptors;

import com.github.jrcodeza.schema.generator.interceptors.OperationInterceptor;
import io.swagger.v3.oas.models.Operation;
import org.apache.commons.lang3.NotImplementedException;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.lang.reflect.Method;
import java.util.List;

public class JakartaOperationResponseCodeInterceptor implements OperationInterceptor {
    List<CtType<?>> adviceClasses; // todo check for equivalent of controllerAdviceClasses

    public JakartaOperationResponseCodeInterceptor(List<CtType<?>> adviceClasses) {
        this.adviceClasses = adviceClasses;
    }

    @Override
    public void intercept(Method method, Operation transformedOperation) {
        throw new NotImplementedException();
    }

    @Override
    public void intercept(CtMethod<?> method, Operation transformedOperation) {
        // TODO
    }
}
