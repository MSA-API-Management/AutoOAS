package at.aau.serg.parsers;

public class ParserFactory {
    public static FrameworkParser createParser(Framework framework, String projectPath, String outputFileName) {
        if (framework == Framework.SPRING) {
            return new FrameworkParser(projectPath, outputFileName, new SpringRestFramework());
        } else {
            throw new IllegalArgumentException("Unsupported framework: " + framework);
        }

        // TODO not supported yet
/*        return switch (framework) {
            case SPRING -> new SpringParser(projectPath, outputFileName);
            default -> throw new IllegalArgumentException("Unsupported framework: " + framework);
        };*/
    }
}
