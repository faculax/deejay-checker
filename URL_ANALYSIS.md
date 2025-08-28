# URL Analysis: nbastwax016 vs dtw004

## Overview
This document explains the key differences between two deejay.de URLs and when the system falls back to checking only static HTML instead of iframe content.

## URLs Analyzed
- **nbastwax016**: `https://deejay.de/nbastwax016` (Single result scenario)
- **dtw004**: `https://deejay.de/dtw004` (Multiple results scenario)

## Key Differences

### 1. Iframe Source URLs
Both URLs have iframes, but they point to different content:

```html
<!-- nbastwax016 -->
<iframe name="myIframe" id="myIframe" src="/content.php?param=%2Fnbastwax016"></iframe>

<!-- dtw004 -->
<iframe name="myIframe" id="myIframe" src="/content.php?param=%2Fdtw004"></iframe>
```

### 2. Content Detection Scenarios

#### Scenario A: nbastwax016 (Single Result)
- **Iframe Status**: Iframe loads but content doesn't contain product indicators
- **Detection Method**: System falls back to checking static HTML
- **Result**: Only finds static page elements (navigation, sidebar, etc.)
- **Product Indicators**: None found in iframe content
- **Outcome**: `NOT FOUND` - System is only checking static HTML

#### Scenario B: dtw004 (Multiple Results)
- **Iframe Status**: Iframe loads successfully with product content
- **Detection Method**: System successfully extracts iframe content
- **Result**: Finds multiple product indicators in iframe
- **Product Indicators**: 
  - Product images (`/pics/images/m/`)
  - Add to cart links (`/addCart/`)
  - Product list classes (`class="product-list"`)
- **Outcome**: `FOUND` - System is checking iframe content

## How to Detect Which Scenario You're In

### In the Code
The `hasResultsFromIframe()` method in ```15:25:src/main/java/com/deejay/Checker.java``` shows when you're only checking static HTML:

```java
public static boolean hasResultsFromIframe(Page page) {
    try {
        // Wait for iframe to appear with shorter timeout
        page.waitForSelector("iframe#myIframe", new Page.WaitForSelectorOptions().setTimeout(8000));
        Frame iframe = page.frameByUrl(url -> url.contains("content.php?param="));
        if (iframe == null) {
            System.out.println("Iframe not found!");
            return false;  // ← This means you're only checking static HTML
        }
        // ... rest of iframe checking logic
    } catch (Exception e) {
        System.out.println("Error checking iframe: " + e.getMessage());
        return false;  // ← This also means you're only checking static HTML
    }
}
```

### Indicators of Static HTML Only
1. **Iframe not found**: `"Iframe not found!"` message
2. **Iframe error**: `"Error checking iframe: [error message]"` message
3. **Return value**: `false` from `hasResultsFromIframe()`

## Why This Happens

### nbastwax016 (Static HTML Only)
- Invalid or non-existent product code
- Iframe content returns error page or empty content
- No product indicators found in iframe
- System falls back to static HTML checking

### dtw004 (Iframe Content Success)
- Valid product code
- Iframe content loads successfully
- Multiple product indicators found
- System successfully processes iframe content

## Test Files Created
- `src/test/resources/nbastwax016.html` - Static HTML content
- `src/test/resources/dtw004.html` - Iframe-based content
- Enhanced test cases in `CheckerTest.java` demonstrating both scenarios

## Recommendations

1. **Log iframe status**: Add logging to identify when system falls back to static HTML
2. **Monitor iframe content**: Check if iframe content is empty or contains errors
3. **Handle edge cases**: Implement fallback logic for when iframe content is unavailable
4. **Validate codes**: Pre-validate codes before attempting iframe content extraction

## Conclusion
The difference between single and multiple results lies in whether the system can successfully extract and process iframe content. When iframe content fails, the system only checks static HTML, which typically contains no product indicators, leading to "NOT FOUND" results.
