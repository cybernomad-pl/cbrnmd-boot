package tech.cybernomad.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BootAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootAnalyzerApplication.class, args);
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║  CBRNMD//BOOT - Spring Boot Analyzer                         ║");
        System.out.println("║  http://localhost:8080                                       ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");
    }
}
