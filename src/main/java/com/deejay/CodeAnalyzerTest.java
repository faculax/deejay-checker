package com.deejay;

import com.microsoft.playwright.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CodeAnalyzerTest {
    
    /**
     * Test version that analyzes a small set of codes for testing purposes
     */
    public static void main(String[] args) throws IOException {
        // Test with a small set of codes
        List<String> testCodes = Arrays.asList(
            "nbastwax016",  // Should be STATIC_HTML_ONLY
            "dtw004",       // Should be MULTIPLE_RESULTS
            "qv002",        // Test another code
            "rawqtroo3"     // Test another code
        );
        
        System.out.println("Testing CodeAnalyzer with " + testCodes.size() + " codes...");
        System.out.println("=" .repeat(60));
        
        List<CodeAnalyzer.AnalysisResult> results = new ArrayList<>();
        
        try (Playwright playwright = Playwright.create()) {
            try (Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setArgs(Arrays.asList("--no-sandbox", "--disable-dev-shm-usage")))) {
                
                Page page = browser.newPage();
                
                for (String code : testCodes) {
                    CodeAnalyzer.AnalysisResult result = CodeAnalyzer.analyzeCode(page, code);
                    results.add(result);
                    
                    // Print result immediately
                    System.out.println(result);
                    
                    // Small delay between requests
                    try {
                        Thread.sleep(2000); // Longer delay for testing
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                
                page.close();
            }
        }
        
        // Print summary
        printTestSummary(results);
        
        System.out.println("\nTest analysis complete!");
    }
    
    /**
     * Prints a summary of test results
     */
    private static void printTestSummary(List<CodeAnalyzer.AnalysisResult> results) {
        System.out.println("\n" + "=" .repeat(60));
        System.out.println("TEST ANALYSIS SUMMARY");
        System.out.println("=" .repeat(60));
        
        Map<CodeAnalyzer.ResultType, Integer> counts = new HashMap<>();
        for (CodeAnalyzer.ResultType type : CodeAnalyzer.ResultType.values()) {
            counts.put(type, 0);
        }
        
        for (CodeAnalyzer.AnalysisResult result : results) {
            counts.put(result.resultType, counts.get(result.resultType) + 1);
        }
        
        System.out.printf("Total codes tested: %d%n", results.size());
        System.out.printf("Single result: %d%n", counts.get(CodeAnalyzer.ResultType.SINGLE_RESULT));
        System.out.printf("Multiple results: %d%n", counts.get(CodeAnalyzer.ResultType.MULTIPLE_RESULTS));
        System.out.printf("Static HTML only: %d%n", counts.get(CodeAnalyzer.ResultType.STATIC_HTML_ONLY));
        System.out.printf("Errors: %d%n", counts.get(CodeAnalyzer.ResultType.ERROR));
        
        // Print expected vs actual results
        System.out.println("\nExpected Results:");
        System.out.println("- nbastwax016: STATIC_HTML_ONLY (no products)");
        System.out.println("- dtw004: MULTIPLE_RESULTS (multiple products)");
        System.out.println("- qv002: TBD (depends on iframe content)");
        System.out.println("- rawqtroo3: TBD (depends on iframe content)");
    }
}
