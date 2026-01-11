package tech.cybernomad.boot.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ClassInfo {
    private String className;
    private String packageName;
    private String fullName;
    private Path filePath;
    private int lineNumber;
    private BeanType beanType;
    private String scope; // singleton, prototype, request, session
    private List<String> annotations;
    private List<String> mappings; // dla kontrolerów: /api/users etc
    private List<String> dependencies; // @Autowired pola
    private List<MethodInfo> beanMethods; // metody @Bean w @Configuration
    private String parentClass;
    private List<String> interfaces;

    public ClassInfo() {
        this.annotations = new ArrayList<>();
        this.mappings = new ArrayList<>();
        this.dependencies = new ArrayList<>();
        this.beanMethods = new ArrayList<>();
        this.interfaces = new ArrayList<>();
        this.scope = "singleton";
        this.beanType = BeanType.UNKNOWN;
    }

    // Getters and setters
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Path getFilePath() { return filePath; }
    public void setFilePath(Path filePath) { this.filePath = filePath; }

    public int getLineNumber() { return lineNumber; }
    public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }

    public BeanType getBeanType() { return beanType; }
    public void setBeanType(BeanType beanType) { this.beanType = beanType; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public List<String> getAnnotations() { return annotations; }
    public void setAnnotations(List<String> annotations) { this.annotations = annotations; }

    public List<String> getMappings() { return mappings; }
    public void setMappings(List<String> mappings) { this.mappings = mappings; }

    public List<String> getDependencies() { return dependencies; }
    public void setDependencies(List<String> dependencies) { this.dependencies = dependencies; }

    public List<MethodInfo> getBeanMethods() { return beanMethods; }
    public void setBeanMethods(List<MethodInfo> beanMethods) { this.beanMethods = beanMethods; }

    public String getParentClass() { return parentClass; }
    public void setParentClass(String parentClass) { this.parentClass = parentClass; }

    public List<String> getInterfaces() { return interfaces; }
    public void setInterfaces(List<String> interfaces) { this.interfaces = interfaces; }

    public void addAnnotation(String annotation) {
        this.annotations.add(annotation);
    }

    public void addMapping(String mapping) {
        this.mappings.add(mapping);
    }

    public void addDependency(String dependency) {
        this.dependencies.add(dependency);
    }

    public void addBeanMethod(MethodInfo method) {
        this.beanMethods.add(method);
    }

    public void addInterface(String iface) {
        this.interfaces.add(iface);
    }

    public String getRelativePath(Path basePath) {
        if (filePath != null && basePath != null) {
            return basePath.relativize(filePath).toString();
        }
        return filePath != null ? filePath.toString() : "";
    }
}
