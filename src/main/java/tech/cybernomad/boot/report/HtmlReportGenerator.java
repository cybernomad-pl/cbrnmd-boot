package tech.cybernomad.boot.report;

import tech.cybernomad.boot.model.*;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class HtmlReportGenerator {

    private static final String CSS = """
        @import url('https://fonts.googleapis.com/css?family=Share+Tech+Mono&display=swap');

        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            background: #111;
            color: #3fc99a;
            font-family: 'Share Tech Mono', monospace;
            font-size: 14px;
            line-height: 1.6;
            min-height: 100vh;
            background-image:
                linear-gradient(0deg, rgba(63, 201, 154, 0.015) 1px, transparent 1px),
                linear-gradient(90deg, rgba(63, 201, 154, 0.015) 1px, transparent 1px);
            background-size: 50px 50px;
        }

        .container {
            max-width: 1400px;
            margin: 0 auto;
            padding: 20px;
        }

        header {
            border-bottom: 1px solid #3fc99a33;
            padding-bottom: 20px;
            margin-bottom: 30px;
        }

        h1 {
            color: #3fc99a;
            font-size: 24px;
            font-weight: normal;
            letter-spacing: 2px;
        }

        h1 span.sub {
            color: #666;
            font-size: 14px;
            display: block;
            margin-top: 5px;
        }

        .meta {
            color: #666;
            font-size: 12px;
            margin-top: 10px;
        }

        .stats {
            display: flex;
            gap: 20px;
            flex-wrap: wrap;
            margin-bottom: 30px;
        }

        .stat-box {
            background: #1a1a1a;
            border: 1px solid #333;
            padding: 15px 20px;
            min-width: 120px;
        }

        .stat-box .value {
            font-size: 28px;
            color: #3fc99a;
        }

        .stat-box .label {
            color: #666;
            font-size: 11px;
            text-transform: uppercase;
            letter-spacing: 1px;
        }

        .section {
            margin-bottom: 40px;
        }

        .section-header {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 15px;
            padding-bottom: 10px;
            border-bottom: 1px solid #333;
        }

        .section-header .icon {
            font-size: 16px;
        }

        .section-header h2 {
            font-size: 16px;
            font-weight: normal;
            color: #3fc99a;
        }

        .section-header .count {
            color: #666;
            font-size: 12px;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            font-size: 13px;
        }

        th {
            text-align: left;
            color: #666;
            font-weight: normal;
            padding: 8px 12px;
            border-bottom: 1px solid #333;
            font-size: 11px;
            text-transform: uppercase;
            letter-spacing: 1px;
        }

        td {
            padding: 10px 12px;
            border-bottom: 1px solid #222;
            vertical-align: top;
        }

        tr:hover td {
            background: #1a1a1a;
        }

        .class-name {
            color: #3fc99a;
        }

        .class-name a {
            color: inherit;
            text-decoration: none;
        }

        .class-name a:hover {
            text-decoration: underline;
        }

        .package {
            color: #666;
            font-size: 11px;
        }

        .scope {
            display: inline-block;
            padding: 2px 8px;
            font-size: 10px;
            border-radius: 2px;
            text-transform: uppercase;
        }

        .scope-singleton { background: #1a2a20; color: #3fc99a; }
        .scope-prototype { background: #2a2a1a; color: #f39c12; }
        .scope-request { background: #1a1a2a; color: #4a90d9; }
        .scope-session { background: #2a1a2a; color: #9b59b6; }

        .mapping {
            color: #4a90d9;
            font-size: 12px;
        }

        .deps {
            color: #666;
            font-size: 11px;
        }

        .bean-method {
            margin: 5px 0;
            padding: 5px 10px;
            background: #1a1a1a;
            border-left: 2px solid #3fc99a33;
        }

        .bean-method .name {
            color: #43D079;
        }

        .bean-method .return-type {
            color: #666;
            font-size: 11px;
        }

        .main-app {
            background: #0f1f1a;
            border: 1px solid #3fc99a33;
            padding: 20px;
            margin-bottom: 30px;
        }

        .main-app h3 {
            color: #3fc99a;
            font-size: 18px;
            font-weight: normal;
            margin-bottom: 10px;
        }

        .tree {
            margin-left: 20px;
            border-left: 1px solid #333;
            padding-left: 20px;
        }

        .tree-item {
            margin: 5px 0;
            position: relative;
        }

        .tree-item::before {
            content: '';
            position: absolute;
            left: -20px;
            top: 10px;
            width: 15px;
            height: 1px;
            background: #333;
        }

        footer {
            margin-top: 50px;
            padding-top: 20px;
            border-top: 1px solid #333;
            color: #444;
            font-size: 11px;
            text-align: center;
        }

        footer a {
            color: #3fc99a;
            text-decoration: none;
        }
        """;

    public String generateString(ProjectInfo project) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("<title>").append(escape(project.getProjectName())).append(" - Spring Boot Analysis</title>\n");
        html.append("<style>\n").append(CSS).append("\n</style>\n");
        html.append("</head>\n<body>\n");
        html.append("<div class=\"container\">\n");

        // Header with back link
        html.append("<header>\n");
        html.append("<h1><a href=\"/\" style=\"color:#3fc99a;text-decoration:none;\">CBRNMD//BOOT</a><span class=\"sub\">Spring Boot Codebase Analysis</span></h1>\n");
        html.append("<div class=\"meta\">Project: ").append(escape(project.getProjectName()));
        html.append(" | Path: ").append(escape(project.getBasePath().toString()));
        html.append(" | Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        html.append("</div>\n</header>\n");

        // Stats
        html.append("<div class=\"stats\">\n");
        html.append(statBox(String.valueOf(project.getTotalBeans()), "Total Beans"));
        for (BeanType type : List.of(BeanType.CONTROLLER, BeanType.REST_CONTROLLER, BeanType.SERVICE, BeanType.REPOSITORY, BeanType.CONFIGURATION)) {
            int count = project.getByType(type).size();
            if (count > 0) {
                html.append(statBox(String.valueOf(count), type.getLabel() + "s"));
            }
        }
        html.append("</div>\n");

        // Main Application
        if (project.getMainApplication() != null) {
            ClassInfo main = project.getMainApplication();
            html.append("<div class=\"main-app\">\n");
            html.append("<h3>").append(main.getBeanType().getIcon()).append(" ").append(escape(main.getClassName())).append("</h3>\n");
            html.append("<div class=\"package\">").append(escape(main.getPackageName())).append("</div>\n");
            html.append("<div style=\"margin-top:10px; color:#666;\">File: ");
            html.append("<span style=\"color:#4a90d9;\">").append(escape(main.getRelativePath(project.getBasePath()))).append("</span></div>\n");
            html.append("</div>\n");
        }

        // Sections by type
        renderSection(html, project, BeanType.CONFIGURATION, "Configuration Classes");
        renderSection(html, project, BeanType.REST_CONTROLLER, "REST Controllers");
        renderSection(html, project, BeanType.CONTROLLER, "Controllers");
        renderSection(html, project, BeanType.SERVICE, "Services");
        renderSection(html, project, BeanType.REPOSITORY, "Repositories");
        renderSection(html, project, BeanType.COMPONENT, "Components");
        renderSection(html, project, BeanType.ENTITY, "Entities");
        renderSection(html, project, BeanType.SCHEDULED, "Scheduled Tasks");
        renderSection(html, project, BeanType.EVENT_LISTENER, "Event Listeners");
        renderSection(html, project, BeanType.ASPECT, "Aspects");

        // Footer
        html.append("<footer>\n");
        html.append("Generated by <a href=\"https://cybernomad.tech\">CBRNMD//BOOT</a> v0.1.0\n");
        html.append("</footer>\n");

        html.append("</div>\n</body>\n</html>");

        return html.toString();
    }

    public void generate(ProjectInfo project, Path outputPath) throws IOException {
        Files.writeString(outputPath, generateString(project));
    }

    private String statBox(String value, String label) {
        return "<div class=\"stat-box\"><div class=\"value\">" + value + "</div><div class=\"label\">" + label + "</div></div>\n";
    }

    private void renderSection(StringBuilder html, ProjectInfo project, BeanType type, String title) {
        List<ClassInfo> classes = project.getByType(type);
        if (classes.isEmpty()) return;

        html.append("<div class=\"section\">\n");
        html.append("<div class=\"section-header\">\n");
        html.append("<span class=\"icon\" style=\"color:").append(type.getColor()).append(";\">").append(type.getIcon()).append("</span>\n");
        html.append("<h2>").append(title).append("</h2>\n");
        html.append("<span class=\"count\">(").append(classes.size()).append(")</span>\n");
        html.append("</div>\n");

        html.append("<table>\n<thead><tr>");
        html.append("<th>Class</th><th>Scope</th>");
        if (type == BeanType.CONTROLLER || type == BeanType.REST_CONTROLLER) {
            html.append("<th>Mappings</th>");
        }
        html.append("<th>Dependencies</th>");
        if (type == BeanType.CONFIGURATION) {
            html.append("<th>@Bean Methods</th>");
        }
        html.append("<th>File</th>");
        html.append("</tr></thead>\n<tbody>\n");

        for (ClassInfo cls : classes) {
            html.append("<tr>");

            // Class name
            html.append("<td><div class=\"class-name\">").append(escape(cls.getClassName())).append("</div>");
            html.append("<div class=\"package\">").append(escape(cls.getPackageName())).append("</div></td>");

            // Scope
            html.append("<td><span class=\"scope scope-").append(cls.getScope()).append("\">")
                .append(cls.getScope()).append("</span></td>");

            // Mappings (for controllers)
            if (type == BeanType.CONTROLLER || type == BeanType.REST_CONTROLLER) {
                html.append("<td class=\"mapping\">");
                html.append(cls.getMappings().stream().map(this::escape).collect(Collectors.joining("<br>")));
                html.append("</td>");
            }

            // Dependencies
            html.append("<td class=\"deps\">");
            html.append(cls.getDependencies().stream().map(this::escape).collect(Collectors.joining(", ")));
            html.append("</td>");

            // Bean methods (for configuration)
            if (type == BeanType.CONFIGURATION) {
                html.append("<td>");
                for (MethodInfo method : cls.getBeanMethods()) {
                    html.append("<div class=\"bean-method\">");
                    html.append("<span class=\"name\">").append(escape(method.getName())).append("()</span> ");
                    html.append("<span class=\"return-type\">→ ").append(escape(method.getReturnType())).append("</span>");
                    if (!"singleton".equals(method.getScope())) {
                        html.append(" <span class=\"scope scope-").append(method.getScope()).append("\">")
                            .append(method.getScope()).append("</span>");
                    }
                    html.append("</div>");
                }
                html.append("</td>");
            }

            // File link
            html.append("<td>");
            String relativePath = cls.getRelativePath(project.getBasePath());
            html.append("<a href=\"file://").append(cls.getFilePath()).append("#L").append(cls.getLineNumber())
                .append("\" style=\"color:#4a90d9;\" title=\"Line ").append(cls.getLineNumber()).append("\">")
                .append(escape(relativePath)).append("</a>");
            html.append("</td>");

            html.append("</tr>\n");
        }

        html.append("</tbody></table>\n</div>\n");
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
