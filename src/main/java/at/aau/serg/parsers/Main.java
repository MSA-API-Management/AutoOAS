package at.aau.serg.parsers;

public class Main {
    public static void main(String[] args) {
        String projectPath;
        String outputPath;
        if (args.length == 2) {
            projectPath = args[0];
            outputPath = args[1];
        } else {
//            throw new IllegalArgumentException("Please provide mvn project path and OAS output path");

            projectPath = "/Users/alelercher/IdeaProjects/Respector/dataset/digdag";
            // gravitee-api-management/gravitee-apim-rest-api";
            // /gravitee-apim-rest-api-management-v4/gravitee-apim-rest-api-management-v4-rest";
            outputPath = "target/openapi/swagger.json";
        }

        /** todo exception in cassandra
         * Exception in thread "main" java.lang.NullPointerException: Cannot invoke "spoon.reflect.reference.CtTypeReference.getSimpleName()" because the return value of "spoon.reflect.code.CtExpression.getType()" is null
         * 	at at.aau.serg.interceptors.JakartaOperationResponseCodeInterceptor.traceResponseCreationBackFromBuildCall(JakartaOperationResponseCodeInterceptor.java:179)
         * 	at at.aau.serg.interceptors.JakartaOperationResponseCodeInterceptor.analyzeResponseInvocation(JakartaOperationResponseCodeInterceptor.java:122)
         * 	at at.aau.serg.interceptors.JakartaOperationResponseCodeInterceptor.tryDetectJakartaResponsesInMethod(JakartaOperationResponseCodeInterceptor.java:99)
         * 	at at.aau.serg.interceptors.JakartaOperationResponseCodeInterceptor.intercept(JakartaOperationResponseCodeInterceptor.java:58)
         * 	at com.github.jrcodeza.schema.generator.OperationsTransformer.lambda$mapPost$16(OperationsTransformer.java:278)
         */

        /** todo many prints
         * e.Response.ok().entity("OK\n")
         * javax.ws.rs.core.Response.ok()
         * javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE).entity("Invalid JSON:" + e.getMessage())
         * javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE)
         * javax.ws.rs.core.Response.ok(java.lang.Integer.toString(maybePid.get()))
         * javax.ws.rs.core.Response.status(org.apache.http.HttpStatus.SC_NO_CONTENT)
         * javax.ws.rs.core.Response.serverError().entity(t.getLocalizedMessage())
         * javax.ws.rs.core.Response.serverError()
         */

        RestApiParser parser = new ParserFactory().createParserWithDetection(projectPath, outputPath);
        parser.run();
    }
}
