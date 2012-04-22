@set SWT_JAR_WINDOWS=jars\windows\swt-3.7.2.jar
@set SWT_JAR_WINDOWS_64=jars\windows\swt-3.7.2_64.jar
@set ANTLR_JAR=jars\antlr-3.2.jar
@set GROOVY_JAR=jars\groovy-all-1.8.6.jar
@set SQLITE_JAR=jars\sqlitejdbc.jar
@set MAIL_JAR=jars\mail.jar

@set JAVAC_FLAGS=-source 1.5
@set JAVA_FLAGS=-Xmx256m -enableassertions

@set VERSION_MAJOR=0
@set VERSION_MINOR=02
@set VERSION_REVISION=unknown

@set CLASSPATH=classes;%SWT_JAR_WINDOWS%;%ANTLR_JAR%;%GROOVY_JAR%;%SQLITE_JAR%;%MAIL_JAR%

@set JAR=jar

@if "%1" == "help" goto help
@if "%1" == "config" goto config
@if "%1" == "compile" goto compile
@if "%1" == "c" goto compile
@if "%1" == "run" goto run
@if "%1" == "r" goto run
@if "%1" == "jars" goto jars
@goto compile

:help
@echo Targets:
@echo.
@echo   config  - create configuration file
@echo   compile - compile sources
@echo   run     - run
@echo   jars    - create jars
@goto end

:config
@del tmp\Config.java 2>NUL
@for /f "tokens=* delims=" %%s in (src\Config.java.in) do @(
  @set "line=%%s"
  @call set line=%%line:@VERSION_MAJOR@=%VERSION_MAJOR%%%
  @call set line=%%line:@VERSION_MINOR@=%VERSION_MINOR%%%
  @call set line=%%line:@VERSION_REVISION@=%VERSION_REVISION%%%
  @call echo %%line%% >> tmp\Config.java
)
@goto end

:compile
@mkdir classes 2>NUL

@if exist tmp\Config.java goto skipConfig
@call make.bat config
@if errorlevel 1 goto end
:skipConfig

java.exe %JAVA_FLAGS% -jar %ANTLR_JAR% -fo tmp src\SimpleJavaRecognizer.g
java.exe %JAVA_FLAGS% -jar %ANTLR_JAR% -fo tmp src\Shell.g
java.exe %JAVA_FLAGS% -jar %ANTLR_JAR% -fo tmp src\ShellEval.g 

javac.exe %JAVAC_FLAGS% -d classes -cp %CLASSPATH% tmp\*.java src\*.java
@goto end

:run
@call make.bat compile
@if errorlevel 1 goto end

java.exe %JAVA_FLAGS% -cp %CLASSPATH% Jsh
@goto end

:jars
@call make.bat compile
@if errorlevel 1 goto end

@mkdir tmp 2>NUL

@rem onzen-windows.jar
@mkdir tmp\jar 2>NUL

rem add classes
copy /Y classes\*.class tmp\jar 1>NUL
rem add SWT JAR
@cd tmp\jar
%JAR% xf ..\..\%SWT_JAR_WINDOWS% 1>NUL
del /S /Q META-INF 1>NUL 2>NUL
@cd ..\..
rem add SQLite JAR
@cd tmp\jar
%JAR% xf ..\..\%SQLITE_JAR% 1>NUL
del /S /Q META-INF 1>NUL 2>NUL
@cd ..\..
rem add mail JAR
@cd tmp\jar
%JAR% xf ..\..\%MAIL_JAR% 1>NUL
del /S /Q META-INF 1>NUL 2>NUL
@cd ..\..
rem add images
mkdir tmp\jar\images 2>NUL
copy /Y images\*.png tmp\jar\images 1>NUL
rem create combined JAR
@cd tmp\jar
%JAR% cmf ..\..\jar.txt ..\..\onzen-windows.jar *
@cd ..\..
rmdir /S /Q tmp\jar 1>NUL

@rem onzen-windows_64.jar
@mkdir tmp\jar 2>NUL

rem add classes
copy /Y classes\*.class tmp\jar 1>NUL
rem add SWT JAR
@cd tmp\jar
%JAR% xf ..\..\%SWT_JAR_WINDOWS_64% 1>NUL
del /S /Q META-INF 1>NUL 2>NUL
@cd ..\..
rem add SQLite JAR
@cd tmp\jar
%JAR% xf ..\..\%SQLITE_JAR% 1>NUL
del /S /Q META-INF 1>NUL 2>NUL
@cd ..\..
rem add mail JAR
@cd tmp\jar
%JAR% xf ..\..\%MAIL_JAR% 1>NUL
del /S /Q META-INF 1>NUL 2>NUL
@cd ..\..
rem add images
mkdir tmp\jar\images 2>NUL
copy /Y images\*.png tmp\jar\images 1>NUL
rem create combined JAR
@cd tmp\jar
%JAR% cmf ..\..\jar.txt ..\..\onzen-windows_64.jar *
@cd ..\..
rmdir /S /Q tmp\jar 1>NUL

@goto end


:end
