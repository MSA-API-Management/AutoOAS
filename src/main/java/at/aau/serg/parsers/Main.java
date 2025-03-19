package at.aau.serg.parsers;

public class Main {
    public static void main(String[] args) {
        String projectPath;
        String outputPath;
        if (args.length == 2) {
            projectPath = args[0];
            outputPath = args[1];
        } else {
            throw new IllegalArgumentException("Please provide mvn project path and OAS output path");
//            projectPath = "src/test/resources/spring-boot/spring-advanced-response-codes";
//            outputPath = "target/openapi/swagger.json";
        }

        // TODO currently hardcoded
        RestApiParser parser = ParserFactory.createParser("spring", projectPath, outputPath);
        parser.run();
    }
}
