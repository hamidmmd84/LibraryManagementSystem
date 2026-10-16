@echo off
setlocal
cd /d "%~dp0"
if not exist out mkdir out
where javac >nul 2>nul
if errorlevel 1 (
  echo Java JDK was not found. Install JDK 17 or newer and add it to PATH.
  pause
  exit /b 1
)
echo Compiling project...
javac -encoding UTF-8 -d out src\main\java\com\example\library\Main.java src\main\java\com\example\library\model\Book.java src\main\java\com\example\library\model\Member.java src\main\java\com\example\library\model\Loan.java src\main\java\com\example\library\service\LibraryService.java src\main\java\com\example\library\ui\MainFrame.java
if errorlevel 1 (
  echo.
  echo Compilation failed. Check the error messages above.
  pause
  exit /b 1
)
echo Starting Library Management System...
java -cp out com.example.library.Main
endlocal
