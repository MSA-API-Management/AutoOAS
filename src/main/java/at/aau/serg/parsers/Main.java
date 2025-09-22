package at.aau.serg.parsers;

public class Main {
    public static void main(String[] args) {
        String projectPath;
        String restApiModulePath;
        String outputPath;
        if (args.length == 3) {
            projectPath = args[0];
            restApiModulePath = args[1];
            outputPath = args[2];
        } else {
//            throw new IllegalArgumentException("Please provide mvn project path and OAS output path");

//            projectPath = "/Users/alelercher/IdeaProjects/Respector/dataset/enviroCar-server";
//            projectPath = "/Users/alelercher/IdeaProjects/Respector/dataset/management-api-for-apache-cassandra/management-api-server";
            projectPath = "/Users/alelercher/IdeaProjects/Respector/dataset/gravitee-api-management/gravitee-apim-rest-api";
            restApiModulePath = "/Users/alelercher/IdeaProjects/Respector/dataset/gravitee-api-management/gravitee-apim-rest-api/gravitee-apim-rest-api-management";

//            projectPath = "/Users/alelercher/IdeaProjects/Respector/dataset/kafka-rest";
//            projectPath = "/Users/alelercher/IdeaProjects/Respector/dataset/senzing-api-server";
//            projectPath = "/Users/alelercher/IdeaProjects/Respector/dataset/restcountries";


            outputPath = "target/openapi/swagger.json";
        }

        RestApiParser parser = new ParserFactory().createParserWithDetection(projectPath, restApiModulePath, outputPath);
        parser.run();
    }
}
