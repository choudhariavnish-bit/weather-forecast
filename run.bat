@echo off
echo Starting Weather Dashboard...

REM Check previous path
if exist "C:\Users\sachi\maven\apache-maven-3.9.6\bin\mvn.cmd" (
    "C:\Users\sachi\maven\apache-maven-3.9.6\bin\mvn.cmd" org.eclipse.jetty.ee10:jetty-ee10-maven-plugin:12.0.8:run
    goto :eof
)

REM Check local system path (Pratik)
if exist "C:\Users\Pratik\maven\apache-maven-3.9.6\bin\mvn.cmd" (
    "C:\Users\Pratik\maven\apache-maven-3.9.6\bin\mvn.cmd" org.eclipse.jetty.ee10:jetty-ee10-maven-plugin:12.0.8:run
    goto :eof
)

REM Fallback to system PATH
mvn org.eclipse.jetty.ee10:jetty-ee10-maven-plugin:12.0.8:run
