package com.deejay;

import com.microsoft.playwright.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class CodeAnalyzer {
    
    /**
     * Analyzes a single code to determine if it has single or multiple results
     * @param page The Playwright page to analyze
     * @param code The code to analyze
     * @return AnalysisResult containing the findings
     */
    public static AnalysisResult analyzeCode(Page page, String code) {
        try {
            String url = "https://deejay.de/" + code;
            System.out.println("Analyzing: " + url);
            
            // Navigate to the page
            page.navigate(url);
            
            // Wait for the page to load
            page.waitForLoadState();
            
            // Check if iframe exists and can be accessed
            boolean iframeExists = checkIframeExists(page);
            
            if (!iframeExists) {
                return new AnalysisResult(code, ResultType.STATIC_HTML_ONLY, 
                    "Iframe not found - only checking static HTML", 0);
            }
            
            // Try to extract iframe content
            String iframeContent = extractIframeContent(page);
            
            if (iframeContent == null || iframeContent.trim().isEmpty()) {
                return new AnalysisResult(code, ResultType.STATIC_HTML_ONLY, 
                    "Iframe content is empty - only checking static HTML", 0);
            }
            
            // Check if iframe contains product indicators
            boolean hasProducts = Checker.hasResults(iframeContent);
            
            if (!hasProducts) {
                return new AnalysisResult(code, ResultType.STATIC_HTML_ONLY, 
                    "Iframe contains no product indicators - only checking static HTML", 0);
            }
            
            // Count the number of products found
            int productCount = countProducts(iframeContent);
            
            if (productCount == 1) {
                return new AnalysisResult(code, ResultType.SINGLE_RESULT, 
                    "Single product found in iframe", productCount);
            } else if (productCount > 1) {
                return new AnalysisResult(code, ResultType.MULTIPLE_RESULTS, 
                    "Multiple products found in iframe", productCount);
            } else {
                return new AnalysisResult(code, ResultType.STATIC_HTML_ONLY, 
                    "Iframe has product indicators but count is 0 - only checking static HTML", 0);
            }
            
        } catch (Exception e) {
            return new AnalysisResult(code, ResultType.ERROR, 
                "Error analyzing code: " + e.getMessage(), 0);
        }
    }
    
    /**
     * Checks if the iframe exists and is accessible
     */
    private static boolean checkIframeExists(Page page) {
        try {
            // Wait for iframe to appear with timeout
            page.waitForSelector("iframe#myIframe", new Page.WaitForSelectorOptions().setTimeout(8000));
            
            // Try to get the iframe
            Frame iframe = page.frameByUrl(url -> url.contains("content.php?param="));
            return iframe != null;
        } catch (Exception e) {
            System.out.println("Iframe check failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Extracts the content from the iframe
     */
    private static String extractIframeContent(Page page) {
        try {
            Frame iframe = page.frameByUrl(url -> url.contains("content.php?param="));
            if (iframe == null) {
                return null;
            }
            
            // Wait a bit for iframe content to load
            page.waitForTimeout(2000);
            
            return iframe.content();
        } catch (Exception e) {
            System.out.println("Iframe content extraction failed: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Counts the number of products in the iframe content
     */
    private static int countProducts(String iframeContent) {
        if (iframeContent == null) return 0;
        
        int imageCount = 0;
        int cartCount = 0;
        int listCount = 0;
        
        // Count product images
        if (iframeContent.contains("/pics/images/m/")) {
            String[] lines = iframeContent.split("\n");
            for (String line : lines) {
                if (line.contains("/pics/images/m/") && line.contains("alt=")) {
                    imageCount++;
                }
            }
        }
        
        // Count add to cart links
        if (iframeContent.contains("/addCart/")) {
            String[] lines = iframeContent.split("\n");
            for (String line : lines) {
                if (line.contains("/addCart/")) {
                    cartCount++;
                }
            }
        }
        
        // Count product list containers
        if (iframeContent.contains("class=\"product-list\"") || iframeContent.contains("class='product-list'")) {
            listCount = 1; // At least one product list container
        }
        
        // More sophisticated counting logic
        if (imageCount > 0 && cartCount > 0) {
            // If we have both images and cart links, use the minimum count
            return Math.min(imageCount, cartCount);
        } else if (imageCount > 0) {
            return imageCount;
        } else if (cartCount > 0) {
            return cartCount;
        } else if (listCount > 0) {
            // If we only have a product list container, assume at least 1 product
            return 1;
        }
        
        return 0;
    }
    
    /**
     * Main method to analyze all codes from codes.txt
     */
    public static void main(String[] args) throws IOException {
        // Read codes from codes.txt
        List<String> codes = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get("codes.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("Processing:")) {
                    codes.add(line);
                }
            }
        }
        
        System.out.println("Analyzing " + codes.size() + " codes...");
        System.out.println("=" .repeat(60));
        
        List<AnalysisResult> results = new ArrayList<>();
        
        try (Playwright playwright = Playwright.create()) {
            try (Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setArgs(Arrays.asList("--no-sandbox", "--disable-dev-shm-usage")))) {
                
                Page page = browser.newPage();
                
                for (String code : codes) {
                    AnalysisResult result = analyzeCode(page, code);
                    results.add(result);
                    
                    // Print result immediately
                    System.out.println(result);
                    
                    // Small delay between requests
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                
                page.close();
            }
        }
        
        // Print summary
        printSummary(results);
        
        // Save detailed results to file
        saveResultsToFile(results, "code_analysis_results.txt");
        
        System.out.println("\nAnalysis complete! Results saved to code_analysis_results.txt");
    }
    
    /**
     * Prints a summary of all results
     */
    private static void printSummary(List<AnalysisResult> results) {
        System.out.println("\n" + "=" .repeat(60));
        System.out.println("ANALYSIS SUMMARY");
        System.out.println("=" .repeat(60));
        
        Map<ResultType, Integer> counts = new HashMap<>();
        for (ResultType type : ResultType.values()) {
            counts.put(type, 0);
        }
        
        for (AnalysisResult result : results) {
            counts.put(result.resultType, counts.get(result.resultType) + 1);
        }
        
        System.out.printf("Total codes analyzed: %d%n", results.size());
        System.out.printf("Single result: %d%n", counts.get(ResultType.SINGLE_RESULT));
        System.out.printf("Multiple results: %d%n", counts.get(ResultType.MULTIPLE_RESULTS));
        System.out.printf("Static HTML only: %d%n", counts.get(ResultType.STATIC_HTML_ONLY));
        System.out.printf("Errors: %d%n", counts.get(ResultType.ERROR));
    }
    
    /**
     * Saves detailed results to a file
     */
    private static void saveResultsToFile(List<AnalysisResult> results, String filename) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename))) {
            writer.write("CODE ANALYSIS RESULTS\n");
            writer.write("=" .repeat(60) + "\n\n");
            
            for (AnalysisResult result : results) {
                writer.write(result.toString() + "\n");
            }
            
            // Add summary
            writer.write("\n" + "=" .repeat(60) + "\n");
            writer.write("SUMMARY\n");
            writer.write("=" .repeat(60) + "\n");
            
            Map<ResultType, Integer> counts = new HashMap<>();
            for (ResultType type : ResultType.values()) {
                counts.put(type, 0);
            }
            
            for (AnalysisResult result : results) {
                counts.put(result.resultType, counts.get(result.resultType) + 1);
            }
            
            writer.write(String.format("Total codes analyzed: %d%n", results.size()));
            writer.write(String.format("Single result: %d%n", counts.get(ResultType.SINGLE_RESULT)));
            writer.write(String.format("Multiple results: %d%n", counts.get(ResultType.MULTIPLE_RESULTS)));
            writer.write(String.format("Static HTML only: %d%n", counts.get(ResultType.STATIC_HTML_ONLY)));
            writer.write(String.format("Errors: %d%n", counts.get(ResultType.ERROR)));
        }
    }
    
    /**
     * Enum representing the type of result
     */
    public enum ResultType {
        SINGLE_RESULT,
        MULTIPLE_RESULTS,
        STATIC_HTML_ONLY,
        ERROR
    }
    
    /**
     * Class representing the analysis result for a single code
     */
    public static class AnalysisResult {
        public final String code;
        public final ResultType resultType;
        public final String description;
        public final int productCount;
        
        public AnalysisResult(String code, ResultType resultType, String description, int productCount) {
            this.code = code;
            this.resultType = resultType;
            this.description = description;
            this.productCount = productCount;
        }
        
        @Override
        public String toString() {
            String status = switch (resultType) {
                case SINGLE_RESULT -> "SINGLE";
                case MULTIPLE_RESULTS -> "MULTIPLE";
                case STATIC_HTML_ONLY -> "STATIC_HTML";
                case ERROR -> "ERROR";
            };
            
            return String.format("[%s] %s: %s (Products: %d)", 
                status, code, description, productCount);
        }
    }
}
