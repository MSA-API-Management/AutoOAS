package at.aau.serg.parsers;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2 || args.length > 4) {
//            System.err.println("Usage: java -jar parser.jar <projectPath> [restApiModulePath] [enableExceptionLogging] <outputPath>");
//            System.exit(1);

            // todo remove before production
            args = new String[]{
                    "C:\\Users\\davidj\\Desktop\\Repos\\MSA-API-Management\\AutoOAS\\src\\test\\resources\\quarkus\\simple-quarkus-misc",
                    "C:\\Users\\davidj\\Desktop\\Repos\\MSA-API-Management\\AutoOAS\\src\\test\\resources\\quarkus\\simple-quarkus-misc",
                    "target/openapi/swagger.json"
            };
        }

        String projectPath;
        String restApiModulePath;
        boolean exceptionLoggingEnabled;
        String outputPath;

        // todo refactor to default values and args[-1]
        if (args.length == 2) {
            projectPath = args[0];
            restApiModulePath = projectPath;
            exceptionLoggingEnabled = false;
            outputPath = args[1];
        } else if (args.length == 3) {
            projectPath = args[0];
            restApiModulePath = args[1];
            exceptionLoggingEnabled = false;
            outputPath = args[2];
        } else { // length == 4
            projectPath = args[0];
            restApiModulePath = args[1];
            exceptionLoggingEnabled = Boolean.parseBoolean(args[2]);
            outputPath = args[3];
        }

        RestApiParser parser = new ParserFactory().createParserWithDetection(projectPath, restApiModulePath, outputPath, exceptionLoggingEnabled);
        parser.run();
    }
}
