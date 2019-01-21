set BATCH_PATH=C:/Users/eric.lahoudie/Downloads/APIAprilImportTemps/APIAprilImportTemps
set SCIFORMA_URL=https://april-migration.sciforma.net/sciforma/
set BATCH_MAIN=APIAprilImportTemps.jar


set ROOT_DIR=%BATCH_PATH%
set LIB_DIR=%ROOT_DIR%/lib

cd %LIB_DIR%

REM IF EXIST "PSClient_en.jar" (
REM    del "PSClient_en.jar"
REM )
REM IF EXIST "PSClient.jar" (
REM    del "PSClient.jar"
REM )
REM IF EXIST "utilities.jar" (
REM    del "utilities.jar"
REM )

REM wget.exe  -O utilities.jar %SCIFORMA_URL%/utilities.jar
REM wget.exe  -O PSClient_en.jar %SCIFORMA_URL%/PSClient_en.jar
REM wget.exe -O PSClient.jar %SCIFORMA_URL%/PSClient.jar

cd %ROOT_DIR%

set JAVA_ARGS=-showversion
set JAVA_ARGS=%JAVA_ARGS% -Xms256m
set JAVA_ARGS=%JAVA_ARGS% -Xmx512m
set JAVA_ARGS=%JAVA_ARGS% -jar

java %JAVA_ARGS% %BATCH_MAIN%
pause
 