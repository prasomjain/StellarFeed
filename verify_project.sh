#!/bin/bash
echo "========================================="
echo "Roku Metadata Engine - Project Verification"
echo "========================================="
echo ""

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check project structure
echo "1. Checking Project Structure..."
if [ -f "pom.xml" ]; then
    echo -e "${GREEN}✓${NC} pom.xml found"
else
    echo -e "${RED}✗${NC} pom.xml missing"
fi

if [ -f "README.md" ]; then
    echo -e "${GREEN}✓${NC} README.md found"
else
    echo -e "${RED}✗${NC} README.md missing"
fi

if [ -f "QUICKSTART.md" ]; then
    echo -e "${GREEN}✓${NC} QUICKSTART.md found"
else
    echo -e "${RED}✗${NC} QUICKSTART.md missing"
fi

echo ""
echo "2. Checking Source Files..."
JAVA_FILES=$(find src/main/java -name "*.java" | wc -l)
echo -e "${GREEN}✓${NC} Found $JAVA_FILES Java source files"

TEST_FILES=$(find src/test/java -name "*.java" | wc -l)
echo -e "${GREEN}✓${NC} Found $TEST_FILES test files"

echo ""
echo "3. Checking Configuration Files..."
if [ -f "src/main/resources/application.yml" ]; then
    echo -e "${GREEN}✓${NC} application.yml found"
else
    echo -e "${RED}✗${NC} application.yml missing"
fi

if [ -f "src/main/resources/data.sql" ]; then
    echo -e "${GREEN}✓${NC} data.sql found"
    ROWS=$(grep -c "INSERT INTO" src/main/resources/data.sql)
    echo -e "  ${GREEN}→${NC} Contains $ROWS sample data rows"
else
    echo -e "${RED}✗${NC} data.sql missing"
fi

if [ -f "src/main/resources/schemas/roku-feed-schema.json" ]; then
    echo -e "${GREEN}✓${NC} roku-feed-schema.json found"
else
    echo -e "${RED}✗${NC} roku-feed-schema.json missing"
fi

echo ""
echo "4. Checking Package Structure..."
for pkg in config controller dto entity model repository service; do
    if [ -d "src/main/java/com/roku/metadata/$pkg" ]; then
        FILES=$(find "src/main/java/com/roku/metadata/$pkg" -name "*.java" | wc -l)
        echo -e "${GREEN}✓${NC} Package $pkg exists ($FILES files)"
    else
        echo -e "${RED}✗${NC} Package $pkg missing"
    fi
done

echo ""
echo "5. Checking Key Java Classes..."
CLASSES=(
    "src/main/java/com/roku/metadata/RokuMetadataApplication.java"
    "src/main/java/com/roku/metadata/entity/ContentMetadata.java"
    "src/main/java/com/roku/metadata/model/MediaType.java"
    "src/main/java/com/roku/metadata/repository/ContentMetadataRepository.java"
    "src/main/java/com/roku/metadata/service/RokuFeedService.java"
    "src/main/java/com/roku/metadata/service/FeedValidationService.java"
    "src/main/java/com/roku/metadata/controller/RokuFeedController.java"
    "src/main/java/com/roku/metadata/config/CacheConfiguration.java"
    "src/main/java/com/roku/metadata/dto/RokuFeedResponse.java"
    "src/main/java/com/roku/metadata/dto/ContentItemDto.java"
)

for class in "${CLASSES[@]}"; do
    if [ -f "$class" ]; then
        echo -e "${GREEN}✓${NC} $(basename $class)"
    else
        echo -e "${RED}✗${NC} $(basename $class) MISSING"
    fi
done

echo ""
echo "6. Checking Test Classes..."
TEST_CLASSES=(
    "src/test/java/com/roku/metadata/controller/RokuFeedControllerIntegrationTest.java"
    "src/test/java/com/roku/metadata/service/RokuFeedServiceTest.java"
    "src/test/java/com/roku/metadata/service/FeedValidationServiceTest.java"
    "src/test/java/com/roku/metadata/repository/ContentMetadataRepositoryTest.java"
)

for test in "${TEST_CLASSES[@]}"; do
    if [ -f "$test" ]; then
        echo -e "${GREEN}✓${NC} $(basename $test)"
    else
        echo -e "${RED}✗${NC} $(basename $test) MISSING"
    fi
done

echo ""
echo "7. Project Statistics..."
TOTAL_FILES=$(find . -type f \( -name "*.java" -o -name "*.xml" -o -name "*.yml" -o -name "*.sql" -o -name "*.json" -o -name "*.md" \) | wc -l)
TOTAL_LINES=$(find . -type f \( -name "*.java" -o -name "*.xml" -o -name "*.yml" -o -name "*.sql" \) -exec wc -l {} + 2>/dev/null | tail -1 | awk '{print $1}')
echo -e "${GREEN}✓${NC} Total project files: $TOTAL_FILES"
echo -e "${GREEN}✓${NC} Total lines of code: $TOTAL_LINES"

echo ""
echo "8. Checking Dependencies in pom.xml..."
if grep -q "spring-boot-starter-web" pom.xml; then
    echo -e "${GREEN}✓${NC} Spring Boot Web dependency"
fi
if grep -q "spring-boot-starter-data-jpa" pom.xml; then
    echo -e "${GREEN}✓${NC} Spring Data JPA dependency"
fi
if grep -q "spring-boot-starter-data-redis" pom.xml; then
    echo -e "${GREEN}✓${NC} Redis dependency"
fi
if grep -q "h2" pom.xml; then
    echo -e "${GREEN}✓${NC} H2 Database dependency"
fi
if grep -q "lombok" pom.xml; then
    echo -e "${GREEN}✓${NC} Lombok dependency"
fi
if grep -q "spring-boot-starter-test" pom.xml; then
    echo -e "${GREEN}✓${NC} Testing dependencies"
fi

echo ""
echo "========================================="
echo "Project Validation Complete!"
echo "========================================="
echo ""

# Final check
if [ -f "pom.xml" ] && [ -d "src" ] && [ $JAVA_FILES -ge 10 ]; then
    echo -e "${GREEN}✓ PROJECT IS READY TO BUILD${NC}"
    echo ""
    echo "Next steps:"
    echo "  1. Ensure Java 17+ is installed: java -version"
    echo "  2. Ensure Maven 3.8+ is installed: mvn -version"
    echo "  3. Build the project: mvn clean install"
    echo "  4. Run the application: mvn spring-boot:run"
else
    echo -e "${RED}✗ PROJECT HAS MISSING COMPONENTS${NC}"
fi
