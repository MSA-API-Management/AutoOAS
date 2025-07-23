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

            projectPath = "/Users/alelercher/IdeaProjects/Respector/dataset/gravitee-api-management/gravitee-apim-rest-api";
//            projectPath = "/Users/alelercher/IdeaProjects/Respector/dataset/management-api-for-apache-cassandra/management-api-server";
            //gravitee-api-management/gravitee-apim-rest-api";
            // gravitee-apim-rest-api-management-v4/gravitee-apim-rest-api-management-v4-rest";
            outputPath = "target/openapi/swagger.json";
        }

        RestApiParser parser = new ParserFactory().createParserWithDetection(projectPath, outputPath);
        parser.run();
    }
}
