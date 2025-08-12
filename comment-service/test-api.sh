#!/bin/bash

# API Testing Script for Comment Service
# Tests all endpoints and RSQL functionality

BASE_URL="http://localhost:8082"
API_BASE="$BASE_URL/api/v1/comments"

echo "🧪 Starting Comprehensive API Testing for Comment Service"
echo "=================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counter
PASSED=0
FAILED=0

# Function to test endpoint
test_endpoint() {
    local test_name="$1"
    local url="$2"
    local expected_status="$3"
    
    echo -e "${BLUE}Testing: $test_name${NC}"
    response=$(curl -s -w "%{http_code}" "$url")
    http_code="${response: -3}"
    body="${response%???}"
    
    if [ "$http_code" = "$expected_status" ]; then
        echo -e "${GREEN}✅ PASS${NC} - Status: $http_code"
        ((PASSED++))
    else
        echo -e "${RED}❌ FAIL${NC} - Expected: $expected_status, Got: $http_code"
        echo "Response: $body"
        ((FAILED++))
    fi
    echo ""
}

# Function to test RSQL query
test_rsql() {
    local test_name="$1"
    local query="$2"
    local expected_count="$3"
    
    echo -e "${BLUE}Testing RSQL: $test_name${NC}"
    response=$(curl -s "$API_BASE/search?query=$query")
    
    if echo "$response" | jq -e '.success == true' > /dev/null 2>&1; then
        count=$(echo "$response" | jq -r '.paylaod.pagination.total // 0')
        if [ "$count" = "$expected_count" ]; then
            echo -e "${GREEN}✅ PASS${NC} - Found $count items (expected: $expected_count)"
            ((PASSED++))
        else
            echo -e "${RED}❌ FAIL${NC} - Expected $expected_count, got $count"
            ((FAILED++))
        fi
    else
        echo -e "${RED}❌ FAIL${NC} - Query failed"
        echo "Response: $response"
        ((FAILED++))
    fi
    echo ""
}

echo "📋 Basic API Endpoints Testing"
echo "-----------------------------"

# Test 1: Health check
test_endpoint "Health Check" "$BASE_URL/actuator/health" "200"

# Test 2: Get all comments
test_endpoint "Get All Comments" "$API_BASE" "200"

# Test 3: Get comment by ID
test_endpoint "Get Comment by ID" "$API_BASE/comment-001" "200"

# Test 4: Get comments by post ID
test_endpoint "Get Comments by Post ID" "$API_BASE/post/post-001" "200"

# Test 5: Get comments by user ID
test_endpoint "Get Comments by User ID" "$API_BASE/user/1" "200"

# Test 6: Get comments hierarchy
test_endpoint "Get Comments Hierarchy" "$API_BASE/post/post-001/hierarchy" "200"

echo "🔍 RSQL Search Testing"
echo "---------------------"

# Test 7: Simple equality
test_rsql "Equality (==)" "likesCount==5" "1"

# Test 8: Greater than
test_rsql "Greater Than (>)" "likesCount%3E3" "1"

# Test 9: Less than
test_rsql "Less Than (<)" "likesCount%3C3" "3"

# Test 10: String search with wildcards
test_rsql "Text Search with Wildcards" "content==%2A%D0%A2%D0%B5%D1%81%D1%82%D0%BE%D0%B2%D0%B8%D0%B9%2A" "1"

# Test 11: AND operation
test_rsql "AND Operation" "likesCount==5;postId==post-001" "1"

# Test 12: OR operation
test_rsql "OR Operation" "likesCount==5,likesCount==3" "2"

# Test 13: Post ID search
test_rsql "Post ID Search" "postId==post-001" "5"

echo "📄 Pagination and Sorting Testing"
echo "--------------------------------"

# Test 14: Pagination
test_endpoint "Pagination (page=0, size=2)" "$API_BASE?page=0&size=2" "200"

# Test 15: Sorting
test_endpoint "Sorting (likesCount desc)" "$API_BASE?sort=likesCount,desc" "200"

# Test 16: Combined pagination and sorting
test_endpoint "Pagination + Sorting" "$API_BASE?page=0&size=3&sort=likesCount,desc" "200"

echo "🔧 Advanced RSQL Testing"
echo "-----------------------"

# Test 17: Not equal
test_rsql "Not Equal (!=)" "likesCount%21%3D5" "4"

# Test 18: Multiple conditions
test_rsql "Multiple Conditions" "postId==post-001;likesCount%3E0" "4"

# Test 19: Complex OR with AND
test_rsql "Complex OR with AND" "postId==post-001;(likesCount==5,likesCount==3)" "2"

echo "📊 Test Results Summary"
echo "======================"
echo -e "${GREEN}✅ Passed: $PASSED${NC}"
echo -e "${RED}❌ Failed: $FAILED${NC}"
echo -e "${BLUE}📈 Total: $((PASSED + FAILED))${NC}"

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}⚠️  Some tests failed. Please check the output above.${NC}"
    exit 1
fi

