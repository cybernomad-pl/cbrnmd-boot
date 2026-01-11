package tech.cybernomad.boot.model;

import java.util.ArrayList;
import java.util.List;

public class MethodInfo {
    private String name;
    private String returnType;
    private int lineNumber;
    private String scope; // singleton, prototype
    private List<String> annotations;
    private List<String> conditionalOn; // @ConditionalOnProperty etc

    public MethodInfo() {
        this.annotations = new ArrayList<>();
        this.conditionalOn = new ArrayList<>();
        this.scope = "singleton";
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getReturnType() { return returnType; }
    public void setReturnType(String returnType) { this.returnType = returnType; }

    public int getLineNumber() { return lineNumber; }
    public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public List<String> getAnnotations() { return annotations; }
    public void setAnnotations(List<String> annotations) { this.annotations = annotations; }

    public List<String> getConditionalOn() { return conditionalOn; }
    public void setConditionalOn(List<String> conditionalOn) { this.conditionalOn = conditionalOn; }

    public void addAnnotation(String annotation) {
        this.annotations.add(annotation);
    }

    public void addConditional(String conditional) {
        this.conditionalOn.add(conditional);
    }
}
