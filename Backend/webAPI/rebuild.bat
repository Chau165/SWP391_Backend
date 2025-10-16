@echo off
echo ========================================
echo  FORCE REBUILD AND REDEPLOY webAPI
echo ========================================
echo.

cd /d "c:\AK\HOCKI5\SWP391\Code\webAPI1\Backend\webAPI"

echo [1/4] Cleaning project...
call ant clean
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Clean failed!
    pause
    exit /b 1
)

echo.
echo [2/4] Compiling...
call ant compile
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Compile failed!
    pause
    exit /b 1
)

echo.
echo [3/4] Building WAR...
call ant dist
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo [4/4] Checking result...
if exist "dist\webAPI3.war" (
    echo SUCCESS: WAR file created at: dist\webAPI3.war
    dir "dist\webAPI3.war"
) else (
    echo ERROR: WAR file not found!
    pause
    exit /b 1
)

echo.
echo ========================================
echo  BUILD COMPLETED SUCCESSFULLY!
echo ========================================
echo.
echo NEXT STEPS:
echo 1. Open NetBeans
echo 2. Stop Tomcat server
echo 3. Undeploy old webAPI3
echo 4. Start Tomcat server
echo 5. Deploy/Run project
echo.
echo OR manually copy WAR to Tomcat:
echo    copy dist\webAPI3.war C:\path\to\tomcat\webapps\
echo.
pause
