package tech.cybernomad.boot.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import tech.cybernomad.boot.model.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class SpringBootAnalyzer {

    private static final Set<String> SPRING_ANNOTATIONS = Set.of(
        "SpringBootApplication", "Configuration", "EnableAutoConfiguration",
        "Controller", "RestController", "Service", "Repository", "Component",
        "Entity", "Aspect", "ControllerAdvice", "RestControllerAdvice"
    );

    private static final Map<String, BeanType> ANNOTATION_TO_TYPE = Map.ofEntries(
        Map.entry("SpringBootApplication", BeanType.SPRING_APPLICATION),
        Map.entry("Configuration", BeanType.CONFIGURATION),
        Map.entry("Controller", BeanType.CONTROLLER),
        Map.entry("RestController", BeanType.REST_CONTROLLER),
        Map.entry("Service", BeanType.SERVICE),
        Map.entry("Repository", BeanType.REPOSITORY),
        Map.entry("Component", BeanType.COMPONENT),
        Map.entry("Entity", BeanType.ENTITY),
        Map.entry("Aspect", BeanType.ASPECT),
        Map.entry("ControllerAdvice", BeanType.CONTROLLER),
        Map.entry("RestControllerAdvice", BeanType.REST_CONTROLLER)
    );

    private final JavaParser parser;
    private final Path basePath;

    public SpringBootAnalyzer(Path basePath) {
        this.basePath = basePath;
        this.parser = new JavaParser();
    }

    public ProjectInfo analyze() throws IOException {
        ProjectInfo project = new ProjectInfo();
        project.setBasePath(basePath);
        project.setProjectName(basePath.getFileName().toString());

        Path srcMain = findSourceRoot();
        if (srcMain == null) {
            System.err.println("Could not find src/main/java directory");
            return project;
        }

        try (Stream<Path> paths = Files.walk(srcMain)) {
            paths.filter(p -> p.toString().endsWith(".java"))
                 .forEach(path -> analyzeFile(path, project));
        }

        return project;
    }

    private Path findSourceRoot() {
        Path srcMainJava = basePath.resolve("src/main/java");
        if (Files.exists(srcMainJava)) {
            return srcMainJava;
        }
        // Try to find any java files
        try (Stream<Path> paths = Files.walk(basePath, 3)) {
            return paths.filter(p -> p.toString().endsWith("src/main/java"))
                       .findFirst()
                       .orElse(basePath);
        } catch (IOException e) {
            return basePath;
        }
    }

    private void analyzeFile(Path filePath, ProjectInfo project) {
        try {
            ParseResult<CompilationUnit> result = parser.parse(filePath);
            if (result.isSuccessful() && result.getResult().isPresent()) {
                CompilationUnit cu = result.getResult().get();
                analyzeCompilationUnit(cu, filePath, project);
            }
        } catch (IOException e) {
            System.err.println("Error parsing: " + filePath + " - " + e.getMessage());
        }
    }

    private void analyzeCompilationUnit(CompilationUnit cu, Path filePath, ProjectInfo project) {
        String packageName = cu.getPackageDeclaration()
            .map(pd -> pd.getNameAsString())
            .orElse("");

        for (TypeDeclaration<?> type : cu.getTypes()) {
            if (type instanceof ClassOrInterfaceDeclaration) {
                ClassOrInterfaceDeclaration classDecl = (ClassOrInterfaceDeclaration) type;
                if (!classDecl.isInterface()) {
                    ClassInfo classInfo = analyzeClass(classDecl, packageName, filePath);
                    if (classInfo.getBeanType() != BeanType.UNKNOWN) {
                        project.addClass(classInfo);
                    }
                }
            }
        }
    }

    private ClassInfo analyzeClass(ClassOrInterfaceDeclaration classDecl, String packageName, Path filePath) {
        ClassInfo info = new ClassInfo();
        info.setClassName(classDecl.getNameAsString());
        info.setPackageName(packageName);
        info.setFullName(packageName + "." + classDecl.getNameAsString());
        info.setFilePath(filePath);
        info.setLineNumber(classDecl.getBegin().map(p -> p.line).orElse(1));

        // Analyze parent class
        classDecl.getExtendedTypes().stream()
            .findFirst()
            .ifPresent(ext -> info.setParentClass(ext.getNameAsString()));

        // Analyze interfaces
        classDecl.getImplementedTypes()
            .forEach(impl -> info.addInterface(impl.getNameAsString()));

        // Analyze annotations
        BeanType determinedType = BeanType.UNKNOWN;
        for (AnnotationExpr annotation : classDecl.getAnnotations()) {
            String annotationName = annotation.getNameAsString();
            info.addAnnotation(annotationName);

            // Determine bean type
            if (ANNOTATION_TO_TYPE.containsKey(annotationName)) {
                determinedType = ANNOTATION_TO_TYPE.get(annotationName);
            }

            // Check for scope
            if ("Scope".equals(annotationName)) {
                extractScope(annotation, info);
            }

            // Check for request mappings
            if (annotationName.contains("Mapping") || "RequestMapping".equals(annotationName)) {
                extractMapping(annotation, info);
            }
        }

        info.setBeanType(determinedType);

        // Analyze fields for @Autowired dependencies
        analyzeFields(classDecl, info);

        // Analyze methods for @Bean definitions (in @Configuration classes)
        if (determinedType == BeanType.CONFIGURATION) {
            analyzeBeanMethods(classDecl, info);
        }

        // Check for @Scheduled methods
        analyzeScheduledMethods(classDecl, info);

        // Check for @EventListener methods
        analyzeEventListenerMethods(classDecl, info);

        return info;
    }

    private void extractScope(AnnotationExpr annotation, ClassInfo info) {
        if (annotation instanceof SingleMemberAnnotationExpr) {
            SingleMemberAnnotationExpr sma = (SingleMemberAnnotationExpr) annotation;
            String value = sma.getMemberValue().toString().replace("\"", "");
            info.setScope(value.toLowerCase());
        } else if (annotation instanceof NormalAnnotationExpr) {
            NormalAnnotationExpr na = (NormalAnnotationExpr) annotation;
            na.getPairs().stream()
                .filter(p -> "value".equals(p.getNameAsString()))
                .findFirst()
                .ifPresent(p -> info.setScope(p.getValue().toString().replace("\"", "").toLowerCase()));
        }
    }

    private void extractMapping(AnnotationExpr annotation, ClassInfo info) {
        if (annotation instanceof SingleMemberAnnotationExpr) {
            SingleMemberAnnotationExpr sma = (SingleMemberAnnotationExpr) annotation;
            info.addMapping(cleanMappingValue(sma.getMemberValue().toString()));
        } else if (annotation instanceof NormalAnnotationExpr) {
            NormalAnnotationExpr na = (NormalAnnotationExpr) annotation;
            na.getPairs().stream()
                .filter(p -> "value".equals(p.getNameAsString()) || "path".equals(p.getNameAsString()))
                .findFirst()
                .ifPresent(p -> info.addMapping(cleanMappingValue(p.getValue().toString())));
        } else if (annotation instanceof MarkerAnnotationExpr) {
            // No value specified, just the annotation name
            info.addMapping("/");
        }
    }

    private String cleanMappingValue(String value) {
        return value.replace("\"", "").replace("{", "").replace("}", "");
    }

    private void analyzeFields(ClassOrInterfaceDeclaration classDecl, ClassInfo info) {
        for (FieldDeclaration field : classDecl.getFields()) {
            boolean isAutowired = hasAnnotation(field, "Autowired", "Inject", "Resource");
            if (isAutowired) {
                field.getVariables().forEach(v -> {
                    String type = field.getElementType().asString();
                    info.addDependency(type);
                });
            }
        }

        // Also check constructor injection
        for (ConstructorDeclaration ctor : classDecl.getConstructors()) {
            if (hasAnnotation(ctor, "Autowired") || classDecl.getConstructors().size() == 1) {
                ctor.getParameters().forEach(param -> {
                    info.addDependency(param.getType().asString());
                });
            }
        }
    }

    private void analyzeBeanMethods(ClassOrInterfaceDeclaration classDecl, ClassInfo info) {
        for (MethodDeclaration method : classDecl.getMethods()) {
            if (hasAnnotation(method, "Bean")) {
                MethodInfo methodInfo = new MethodInfo();
                methodInfo.setName(method.getNameAsString());
                methodInfo.setReturnType(method.getType().asString());
                methodInfo.setLineNumber(method.getBegin().map(p -> p.line).orElse(1));

                // Check for scope on bean method
                method.getAnnotations().forEach(ann -> {
                    methodInfo.addAnnotation(ann.getNameAsString());
                    if ("Scope".equals(ann.getNameAsString())) {
                        extractMethodScope(ann, methodInfo);
                    }
                    if (ann.getNameAsString().startsWith("ConditionalOn")) {
                        methodInfo.addConditional(ann.toString());
                    }
                });

                info.addBeanMethod(methodInfo);
            }
        }
    }

    private void extractMethodScope(AnnotationExpr annotation, MethodInfo info) {
        if (annotation instanceof SingleMemberAnnotationExpr) {
            SingleMemberAnnotationExpr sma = (SingleMemberAnnotationExpr) annotation;
            info.setScope(sma.getMemberValue().toString().replace("\"", "").toLowerCase());
        }
    }

    private void analyzeScheduledMethods(ClassOrInterfaceDeclaration classDecl, ClassInfo info) {
        for (MethodDeclaration method : classDecl.getMethods()) {
            if (hasAnnotation(method, "Scheduled")) {
                if (info.getBeanType() == BeanType.UNKNOWN || info.getBeanType() == BeanType.COMPONENT) {
                    info.setBeanType(BeanType.SCHEDULED);
                }
            }
        }
    }

    private void analyzeEventListenerMethods(ClassOrInterfaceDeclaration classDecl, ClassInfo info) {
        for (MethodDeclaration method : classDecl.getMethods()) {
            if (hasAnnotation(method, "EventListener", "TransactionalEventListener")) {
                if (info.getBeanType() == BeanType.UNKNOWN || info.getBeanType() == BeanType.COMPONENT) {
                    info.setBeanType(BeanType.EVENT_LISTENER);
                }
            }
        }
    }

    private boolean hasAnnotation(NodeWithAnnotations<?> node, String... annotationNames) {
        Set<String> names = Set.of(annotationNames);
        return node.getAnnotations().stream()
            .anyMatch(a -> names.contains(a.getNameAsString()));
    }
}
