echo .
echo .
echo call gradlew jar
call gradlew jar

echo .
echo .
echo call gradlew thinJar
call gradlew thinJar

echo .
echo .
echo call gradlew thinResolve
call gradlew thinResolve

echo .
echo .
echo call copyJar
call copyJar

