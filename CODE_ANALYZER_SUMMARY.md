# CodeAnalyzer Class - Complete Analysis

## Overview
The `CodeAnalyzer` class is a new Java application that analyzes every code in `codes.txt` to determine whether each code has single results, multiple results, or only static HTML content. This leverages the logic from the unit tests to provide comprehensive analysis of deejay.de product codes.

## What It Does

### 1. **Code Analysis Process**
For each code in `codes.txt`, the `CodeAnalyzer`:
- Navigates to `https://deejay.de/{code}`
- Checks if the iframe exists and is accessible
- Extracts iframe content if available
- Analyzes the content for product indicators
- Counts the number of products found
- Categorizes the result into one of four types

### 2. **Result Categories**
- **SINGLE_RESULT**: One product found in iframe
- **MULTIPLE_RESULTS**: Multiple products found in iframe  
- **STATIC_HTML_ONLY**: No iframe or no product indicators (fallback to static HTML)
- **ERROR**: Error occurred during analysis

### 3. **Product Detection Logic**
The analyzer looks for these indicators in iframe content:
- Product images: `/pics/images/m/` with `alt=` attributes
- Add to cart links: `/addCart/`
- Product list containers: `class="product-list"` or `class='product-list'`

## Key Features

### **Real-time Analysis**
- Processes codes sequentially with configurable delays
- Prints results immediately as they're processed
- Provides progress feedback during long runs

### **Comprehensive Reporting**
- Console output with real-time results
- Detailed results file (`code_analysis_results.txt`)
- Summary statistics at the end
- Error handling and logging

### **Performance Optimized**
- Uses Playwright for reliable web scraping
- Headless browser mode for efficiency
- Configurable delays between requests
- Single browser instance for all codes

## Usage

### **Full Analysis (All Codes)**
```bash
mvn exec:java -Dexec.mainClass="com.deejay.CodeAnalyzer"
```

### **Test Analysis (Sample Codes)**
```bash
mvn exec:java -Dexec.mainClass="com.deejay.CodeAnalyzerTest"
```

## Analysis Results

### **Summary Statistics**
From the latest run of 56 codes:
- **Total codes analyzed**: 56
- **Single result**: 47 (83.9%)
- **Multiple results**: 8 (14.3%)
- **Static HTML only**: 1 (1.8%)
- **Errors**: 0 (0%)

### **Key Findings**
1. **Most codes (83.9%)** return single product results
2. **Multiple product results (14.3%)** are less common but significant
3. **Very few codes (1.8%)** fall back to static HTML only
4. **No errors** occurred during analysis

### **Examples by Category**

#### Single Results (47 codes)
- `BNS085`, `HT005`, `OBLICUO001`, `KW053`, `2CENACID`
- `MARU006`, `RELIKTORIGINALS006`, `VAM08`, `DCY030`

#### Multiple Results (8 codes)
- `MNR007`: 2 products
- `LTH004`: 2 products  
- `QV002`: 2 products
- `WH03/21`: 4 products
- `RT002`: 4 products

#### Static HTML Only (1 code)
- `REPEAT12`: Iframe not found

## Technical Implementation

### **Core Classes**
- **`CodeAnalyzer`**: Main analysis engine
- **`CodeAnalyzerTest`**: Test version for small code sets
- **`AnalysisResult`**: Data structure for results
- **`ResultType`**: Enum for result categories

### **Key Methods**
- `analyzeCode()`: Analyzes a single code
- `checkIframeExists()`: Verifies iframe accessibility
- `extractIframeContent()`: Gets iframe HTML content
- `countProducts()`: Counts product indicators
- `saveResultsToFile()`: Saves detailed results

### **Dependencies**
- **Playwright**: Web automation and iframe handling
- **Java NIO**: File operations
- **Java Collections**: Data management

## Benefits

### **1. Comprehensive Understanding**
- Now you know exactly which codes have single vs multiple results
- Clear identification of when system falls back to static HTML
- Quantitative data on product distribution

### **2. Better Decision Making**
- Can prioritize codes with multiple results
- Identify problematic codes that only return static HTML
- Understand the success rate of iframe content extraction

### **3. Quality Assurance**
- Validates that the system is working correctly
- Identifies edge cases and error conditions
- Provides data for system optimization

## Future Enhancements

### **Potential Improvements**
1. **Parallel Processing**: Process multiple codes simultaneously
2. **Detailed Product Info**: Extract product names, prices, etc.
3. **Historical Tracking**: Track changes over time
4. **Performance Metrics**: Measure response times and success rates
5. **Export Formats**: CSV, JSON, or database storage

### **Integration Opportunities**
- **Dashboard**: Web interface for results visualization
- **Alerts**: Notifications for failed analyses
- **Scheduling**: Automated periodic analysis
- **API**: REST endpoints for external access

## Conclusion

The `CodeAnalyzer` class successfully addresses your original question about determining single vs multiple results for each code. It provides:

- **Clear categorization** of each code's result type
- **Quantitative analysis** of your entire codebase
- **Real-time feedback** during processing
- **Comprehensive reporting** for further analysis

This tool gives you the insights needed to understand when your system is successfully processing iframe content versus falling back to static HTML checking, exactly as requested.
