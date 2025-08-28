package com.deejay;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CheckerTest {
    
    @Test
    public void testCodeWithResults() {
        // Mock HTML content that would be in the iframe when products are found
        String htmlWithProducts = """
            <div class="product-list">
                <div class="product">
                    <img src="/pics/images/m/product1.jpg" alt="Product 1">
                    <a href="/addCart/123" class="add-to-cart">Add to Cart</a>
                </div>
                <div class="product">
                    <img src="/pics/images/m/product2.jpg" alt="Product 2">
                    <a href="/addCart/456" class="add-to-cart">Add to Cart</a>
                </div>
            </div>
            """;
        
        assertTrue(Checker.hasResults(htmlWithProducts), "Should detect results for code with products");
    }

    @Test
    public void testCodeWithoutResults() {
        // Mock HTML content that would be in the iframe when no products are found
        String htmlWithoutProducts = """
            <div class="no-results">
                <p>Sorry, we didn´t find a matching Entry.</p>
            </div>
            """;
        
        assertFalse(Checker.hasResults(htmlWithoutProducts), "Should detect no results for code without products");
    }
    
    @Test
    public void testMixedContent() {
        // Test with content that has some product indicators but not others
        String mixedHtml = """
            <div class="content">
                <p>Some text here</p>
                <img src="/pics/images/m/product1.jpg" alt="Product 1">
                <p>More text</p>
            </div>
            """;
        
        assertTrue(Checker.hasResults(mixedHtml), "Should detect results when product image is present");
    }
    
    @Test
    public void testEmptyContent() {
        // Test with minimal content
        String emptyHtml = "<div></div>";
        assertFalse(Checker.hasResults(emptyHtml), "Should detect no results for empty content");
    }
}
