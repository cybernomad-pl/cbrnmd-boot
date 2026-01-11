package tech.cybernomad.boot;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import tech.cybernomad.boot.analyzer.SpringBootAnalyzer;
import tech.cybernomad.boot.model.ProjectInfo;
import tech.cybernomad.boot.report.HtmlReportGenerator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
    name = "cbrnmd-boot",
    mixinStandardHelpOptions = true,
    version = "cbrnmd-boot 0.1.0",
    description = "Analyzes Spring Boot codebase and generates HTML report"
)
public class BootAnalyzerCli implements Callable<Integer> {

    @Parameters(index = "0", description = "Path to Spring Boot project root")
    private Path projectPath;

    @Option(names = {"-o", "--output"}, description = "Output HTML file path (default: boot-report.html)")
    private Path outputPath;

    @Option(names = {"-v", "--verbose"}, description = "Verbose output")
    private boolean verbose;

    @Override
    public Integer call() throws Exception {
        // Validate input
        if (!Files.exists(projectPath)) {
            System.err.println("ERROR: Project path does not exist: " + projectPath);
            return 1;
        }

        if (!Files.isDirectory(projectPath)) {
            System.err.println("ERROR: Project path is not a directory: " + projectPath);
            return 1;
        }

        // Set default output path
        if (outputPath == null) {
            outputPath = projectPath.resolve("boot-report.html");
        }

        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║  CBRNMD//BOOT - Spring Boot Analyzer v0.1.0                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Analyzing: " + projectPath.toAbsolutePath());
        System.out.println();

        // Run analysis
        SpringBootAnalyzer analyzer = new SpringBootAnalyzer(projectPath.toAbsolutePath());
        ProjectInfo project = analyzer.analyze();

        if (verbose) {
            printSummary(project);
        }

        // Generate report
        HtmlReportGenerator reportGenerator = new HtmlReportGenerator();
        reportGenerator.generate(project, outputPath);

        System.out.println("✓ Report generated: " + outputPath.toAbsolutePath());
        System.out.println();
        System.out.println("Summary:");
        System.out.println("  Total beans found: " + project.getTotalBeans());
        project.getStatsByType().forEach((type, count) ->
            System.out.println("  " + type + ": " + count)
        );

        return 0;
    }

    private void printSummary(ProjectInfo project) {
        System.out.println("Classes found:");
        project.getAllClasses().forEach(cls -> {
            System.out.println("  " + cls.getBeanType().getIcon() + " " + cls.getFullName() + " [" + cls.getScope() + "]");
            if (!cls.getMappings().isEmpty()) {
                cls.getMappings().forEach(m -> System.out.println("      → " + m));
            }
        });
        System.out.println();
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new BootAnalyzerCli()).execute(args);
        System.exit(exitCode);
    }
}
