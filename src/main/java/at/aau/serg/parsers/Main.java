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
//            projectPath = "src/test/resources/quarkus/quarkus-behavioral-exception-tests";
            projectPath = "src/test/resources/spring-boot/simple-spring-22c055";
            outputPath = "target/openapi/swagger.json";
        }

//        RestApiParser parser = new ParserFactory().createParser("jakarta", projectPath, outputPath);
        RestApiParser parser = new ParserFactory().createParserWithDetection(projectPath, outputPath);
        parser.run();
    }
}
