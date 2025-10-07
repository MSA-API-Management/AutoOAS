package at.aau.serg.parsers;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2 && args.length != 4) {
            System.err.println("Usage: java -jar parser.jar <projectPath> [restApiModulePath] <outputPath>");
            System.exit(1);
        }

        String projectPath;
        String restApiModulePath;
        boolean exceptionLoggingEnabled;
        String outputPath;

        if (args.length == 2) {
            projectPath = args[0];
            restApiModulePath = projectPath;
            outputPath = args[1];
            exceptionLoggingEnabled = false;
        } else {
            projectPath = args[0];
            restApiModulePath = args[1];
            exceptionLoggingEnabled = Boolean.parseBoolean(args[2]);
            outputPath = args[3];
        }

        RestApiParser parser = new ParserFactory().createParserWithDetection(projectPath, restApiModulePath, outputPath, exceptionLoggingEnabled);
        parser.run();
    }
}
