package tech.cybernomad.boot.model;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ProjectInfo {
    private String projectName;
    private Path basePath;
    private ClassInfo mainApplication;
    private List<ClassInfo> allClasses;
    private Map<BeanType, List<ClassInfo>> byType;

    public ProjectInfo() {
        this.allClasses = new ArrayList<>();
        this.byType = new EnumMap<>(BeanType.class);
        for (BeanType type : BeanType.values()) {
            byType.put(type, new ArrayList<>());
        }
    }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public Path getBasePath() { return basePath; }
    public void setBasePath(Path basePath) { this.basePath = basePath; }

    public ClassInfo getMainApplication() { return mainApplication; }
    public void setMainApplication(ClassInfo mainApplication) {
        this.mainApplication = mainApplication;
    }

    public List<ClassInfo> getAllClasses() { return allClasses; }

    public void addClass(ClassInfo classInfo) {
        allClasses.add(classInfo);
        byType.get(classInfo.getBeanType()).add(classInfo);
        if (classInfo.getBeanType() == BeanType.SPRING_APPLICATION) {
            this.mainApplication = classInfo;
        }
    }

    public List<ClassInfo> getByType(BeanType type) {
        return byType.getOrDefault(type, Collections.emptyList());
    }

    public Map<BeanType, List<ClassInfo>> getByType() {
        return byType;
    }

    public int getTotalBeans() {
        return allClasses.size();
    }

    public Map<String, Long> getStatsByType() {
        return allClasses.stream()
            .collect(Collectors.groupingBy(
                c -> c.getBeanType().getLabel(),
                Collectors.counting()
            ));
    }

    public List<ClassInfo> getPrototypeBeans() {
        return allClasses.stream()
            .filter(c -> "prototype".equals(c.getScope()))
            .collect(Collectors.toList());
    }

    public List<ClassInfo> getByPackage(String packagePrefix) {
        return allClasses.stream()
            .filter(c -> c.getPackageName() != null && c.getPackageName().startsWith(packagePrefix))
            .collect(Collectors.toList());
    }
}
