package at.aau.serg.parsers;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2 || args.length > 3) {
            System.err.println("Usage: java -jar parser.jar <projectPath> [restApiModulePath] <outputPath>");
            System.exit(1);
        }

        String projectPath;
        String restApiModulePath;
        String outputPath;

        if (args.length == 2) {
            projectPath = args[0];
            restApiModulePath = projectPath;
            outputPath = args[1];
        } else {
            projectPath = args[0];
            restApiModulePath = args[1];
            outputPath = args[2];
        }

        RestApiParser parser = new ParserFactory().createParserWithDetection(projectPath, restApiModulePath, outputPath);
        parser.run();
    }
}
