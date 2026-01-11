package tech.cybernomad.boot.model;

public enum BeanType {
    SPRING_APPLICATION("SpringBootApplication", "#3fc99a", "◉"),
    CONFIGURATION("Configuration", "#4a90d9", "⚙"),
    CONTROLLER("Controller", "#43D079", "▶"),
    REST_CONTROLLER("RestController", "#43D079", "◀▶"),
    SERVICE("Service", "#1fd1c1", "●"),
    REPOSITORY("Repository", "#9b59b6", "◆"),
    COMPONENT("Component", "#f39c12", "■"),
    ENTITY("Entity", "#e74c3c", "◇"),
    ASPECT("Aspect", "#8e44ad", "✦"),
    EVENT_LISTENER("EventListener", "#16a085", "⚡"),
    SCHEDULED("Scheduled", "#2980b9", "⏱"),
    BEAN_METHOD("Bean", "#7f8c8d", "○"),
    UNKNOWN("Unknown", "#666", "?");

    private final String label;
    private final String color;
    private final String icon;

    BeanType(String label, String color, String icon) {
        this.label = label;
        this.color = color;
        this.icon = icon;
    }

    public String getLabel() { return label; }
    public String getColor() { return color; }
    public String getIcon() { return icon; }
}
