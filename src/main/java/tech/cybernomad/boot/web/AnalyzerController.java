package tech.cybernomad.boot.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import tech.cybernomad.boot.analyzer.SpringBootAnalyzer;
import tech.cybernomad.boot.model.ProjectInfo;
import tech.cybernomad.boot.report.HtmlReportGenerator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
public class AnalyzerController {

    private final HtmlReportGenerator reportGenerator = new HtmlReportGenerator();

    @GetMapping(value = "/analyze", produces = MediaType.TEXT_HTML_VALUE)
    public String analyze(@RequestParam String path) {
        try {
            Path projectPath = Path.of(path).toAbsolutePath();

            if (!Files.exists(projectPath)) {
                return errorPage("Folder nie istnieje: " + path);
            }

            if (!Files.isDirectory(projectPath)) {
                return errorPage("Ścieżka nie jest folderem: " + path);
            }

            SpringBootAnalyzer analyzer = new SpringBootAnalyzer(projectPath);
            ProjectInfo project = analyzer.analyze();

            if (project.getTotalBeans() == 0) {
                return errorPage("Nie znaleziono żadnych beanów Spring Boot w: " + path);
            }

            return reportGenerator.generateString(project);

        } catch (Exception e) {
            return errorPage("Błąd analizy: " + e.getMessage());
        }
    }

    @PostMapping(value = "/analyze", produces = MediaType.TEXT_HTML_VALUE)
    public String analyzePost(@RequestBody Map<String, String> body) {
        return analyze(body.get("path"));
    }

    private String errorPage(String message) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Błąd - CBRNMD//BOOT</title>
                <style>
                    body { background: #111; color: #e74c3c; font-family: 'Share Tech Mono', monospace; padding: 50px; }
                    .error { border: 1px solid #e74c3c; padding: 20px; max-width: 600px; }
                    a { color: #3fc99a; }
                </style>
                <link href="https://fonts.googleapis.com/css?family=Share+Tech+Mono" rel="stylesheet">
            </head>
            <body>
                <div class="error">
                    <h2>✗ Błąd</h2>
                    <p>%s</p>
                    <p><a href="/">← Powrót</a></p>
                </div>
            </body>
            </html>
            """.formatted(message);
    }
}
