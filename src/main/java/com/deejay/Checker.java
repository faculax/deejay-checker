package com.deejay;

import com.microsoft.playwright.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class Checker {
    // Robust product/result detection logic using iframe content
    public static boolean hasResultsFromIframe(Page page) {
        try {
            // Wait for iframe to appear with shorter timeout
            page.waitForSelector("iframe#myIframe", new Page.WaitForSelectorOptions().setTimeout(8000));
            Frame iframe = page.frameByUrl(url -> url.contains("content.php?param="));
            if (iframe == null) {
                System.out.println("Iframe not found!");
                return false;
            }
            String iframeHtml = iframe.content();
            // Check for 'Sorry' message
            if (iframeHtml.contains("Sorry, we didn´t find a matching Entry.")) {
                return false;
            }
            // Check for product indicators
            boolean hasProductImage = iframeHtml.contains("/pics/images/m/");
            boolean hasProductLink = iframeHtml.contains("/addCart/");
            boolean hasProductList = iframeHtml.contains("class=\"product-list\"") || iframeHtml.contains("class='product-list'");
            return hasProductImage || hasProductLink || hasProductList;
        } catch (Exception e) {
            System.out.println("Error checking iframe: " + e.getMessage());
            return false;
        }
    }

    // Static method to parse HTML content and detect results (useful for testing)
    public static boolean hasResults(String html) {
        // Check for 'Sorry' message
        if (html.contains("Sorry, we didn´t find a matching Entry.")) {
            return false;
        }
        // Check for product indicators
        boolean hasProductImage = html.contains("/pics/images/m/");
        boolean hasProductLink = html.contains("/addCart/");
        boolean hasProductList = html.contains("class=\"product-list\"") || html.contains("class='product-list'");
        return hasProductImage || hasProductLink || hasProductList;
    }

    public static void main(String[] args) throws IOException {
        // Read codes from codes.txt, ignore lines starting with 'Processing:' and empty lines
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
        
        System.out.println("Processing " + codes.size() + " codes with parallel processing...");
        
        // More aggressive parallel processing
        int maxConcurrent = 8; // Increased from 4 to 8 parallel browser instances
        ExecutorService executor = Executors.newFixedThreadPool(maxConcurrent);
        List<Future<String>> futures = new ArrayList<>();
        
        try (Playwright playwright = Playwright.create()) {
            for (String code : codes) {
                Future<String> future = executor.submit(() -> {
                    try (Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                            .setHeadless(true)
                            .setArgs(Arrays.asList("--no-sandbox", "--disable-dev-shm-usage")))) {
                        Page page = browser.newPage();
                        try {
                            String url = "https://deejay.de/" + code;
                            page.navigate(url);
                            // Use proper element waiting instead of fixed delays
                            boolean hasResults = hasResultsFromIframe(page);
                            String result = code + ": " + (hasResults ? "FOUND" : "NOT FOUND");
                            System.out.println(result);
                            return result;
                        } finally {
                            page.close();
                        }
                    } catch (Exception e) {
                        String result = code + ": ERROR - " + e.getMessage();
                        System.out.println(result);
                        return result;
                    }
                });
                futures.add(future);
            }
            
            // Collect results as they complete
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("results.txt"))) {
                for (Future<String> future : futures) {
                    try {
                        String result = future.get();
                        writer.write(result);
                        writer.newLine();
                        writer.flush(); // Write immediately as results come in
                    } catch (Exception e) {
                        System.err.println("Error getting result: " + e.getMessage());
                    }
                }
            }
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) { // Reduced timeout
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
        
        System.out.println("Processing complete!");
    }
}
