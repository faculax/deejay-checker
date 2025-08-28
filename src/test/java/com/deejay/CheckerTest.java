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

    @Test
    public void testNbastwax016StaticHTML() {
        // Test the nbastwax016 URL - this should only check static HTML
        // because the iframe content doesn't contain product indicators
        String nbastwax016Html = """
            <div class="content">
                <p>Some content but no product indicators</p>
                <div class="sidebar">
                    <h3>DJ Equipment</h3>
                    <ul>
                        <li><a href="m_Equipment/substyles_1100">DJ Bags</a></li>
                        <li><a href="m_Equipment/substyles_1400">Headphones</a></li>
                    </ul>
                </div>
            </div>
            """;
        
        // This should return false because it's only checking static HTML
        // and the static HTML doesn't contain product indicators
        assertFalse(Checker.hasResults(nbastwax016Html), 
            "nbastwax016 should return false when only checking static HTML");
    }

    @Test
    public void testDtw004IframeContent() {
        // Test the dtw004 URL - this should successfully check iframe content
        // and find multiple product indicators
        String dtw004IframeHtml = """
            <div class="product-list">
                <div class="product">
                    <img src="/pics/images/m/vinyl1.jpg" alt="Vinyl Record 1">
                    <a href="/addCart/789" class="add-to-cart">Add to Cart</a>
                </div>
                <div class="product">
                    <img src="/pics/images/m/vinyl2.jpg" alt="Vinyl Record 2">
                    <a href="/addCart/101" class="add-to-cart">Add to Cart</a>
                </div>
                <div class="product">
                    <img src="/pics/images/m/vinyl3.jpg" alt="Vinyl Record 3">
                    <a href="/addCart/112" class="add-to-cart">Add to Cart</a>
                </div>
            </div>
            """;
        
        // This should return true because it's checking iframe content
        // and the iframe contains multiple product indicators
        assertTrue(Checker.hasResults(dtw004IframeHtml), 
            "dtw004 should return true when checking iframe content with products");
    }

    @Test
    public void testIframeDetection() {
        // Test to show the difference between iframe and static HTML detection
        String iframeWithProducts = """
            <div class="product-list">
                <img src="/pics/images/m/product.jpg">
                <a href="/addCart/123">Add to Cart</a>
            </div>
            """;
        
        String staticHtmlWithoutProducts = """
            <div class="main-content">
                <h1>Welcome to deejay.de</h1>
                <p>This is the main page content</p>
                <div class="sidebar">
                    <h3>Categories</h3>
                    <ul>
                        <li><a href="/m_House/">House</a></li>
                        <li><a href="/m_Techno/">Techno</a></li>
                    </ul>
                </div>
            </div>
            """;
        
        // Iframe content with products should return true
        assertTrue(Checker.hasResults(iframeWithProducts), 
            "Iframe content with products should return true");
        
        // Static HTML without products should return false
        assertFalse(Checker.hasResults(staticHtmlWithoutProducts), 
            "Static HTML without products should return false");
    }
}
