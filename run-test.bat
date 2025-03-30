@echo off
cd /d D:\FiveBear
echo Compiling the project...
mvn compile
echo Copying dependencies...
mvn dependency:copy-dependencies

echo Running ProxyListTest...
java -cp "target/classes;target/dependency/*;lib/*" com.FiveBear.fivebear_system.test.ProxyListTest
pause 