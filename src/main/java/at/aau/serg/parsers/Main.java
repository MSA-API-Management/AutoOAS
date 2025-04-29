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
//            projectPath = "src/test/resources/spring-boot/spring-advanced-response-codes";
//            projectPath = "src/test/resources/quarkus/quarkus-exceptions-231s4f";
            projectPath = "src/test/resources/quarkus/simple-quarkus-2cc23a";
//            projectPath = "src/test/resources/spring-boot/simple-spring-2cc389";
            outputPath = "target/openapi/swagger.json";
        }

        // TODO currently hardcoded
//        RestApiParser parser = new ParserFactory().createParser("spring", projectPath, outputPath);
        RestApiParser parser = new ParserFactory().createParser("jakarta", projectPath, outputPath);
//        RestApiParser parser = new ParserFactory().createParser("spring", projectPath, outputPath);
        parser.run();
    }
}
