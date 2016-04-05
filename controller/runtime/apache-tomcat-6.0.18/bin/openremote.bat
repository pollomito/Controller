@echo off
set CATALINA_PID=C:\temp\or.pid
rem Licensed to the Apache Software Foundation (ASF) under one or more
rem contributor license agreements.  See the NOTICE file distributed with
rem this work for additional information regarding copyright ownership.
rem The ASF licenses this file to You under the Apache License, Version 2.0
rem (the "License"); you may not use this file except in compliance with
rem the License.  You may obtain a copy of the License at
rem
rem     http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.

rem ---------------------------------------------------------------------------
rem
rem Modifications added for specific OpenRemote use scenarios. 
rem Copyright 2008-2015 OpenRemote, Inc.
rem
rem Authors:
rem   Rich Turner (richard@openremote.org)
rem 
rem 
rem Following environment variables are supported to configure logging:
rem 
rem   CONTROLLER_CONSOLE_THRESHOLD
rem     
rem     Limits the messages targeted for standard output stream based on log
rem     message level. Valid level values in descending order of importance
rem     are:
rem 
rem       1) OFF
rem       2) ERROR
rem       3) WARN or WARNING
rem       4) INFO
rem       5) DEBUG
rem       6) TRACE
rem       7) ALL
rem 
rem     Default value for standard output stream is to print messages with
rem     level INFO or above. To set a different level, use for example:
rem 
rem       > set CONTROLLER_CONSOLE_THRESHOLD=ERROR
rem 
rem     before executing this script.
rem 
rem 
rem  CONTROLLER_STARTUP_LOG_LEVEL
rem 
rem    Sets the level of log messages recorded by controller bootstrap
rem    services. See valid level values in CONTROLLER_CONSOLE_THRESHOLD
rem    above. Notice that individual log targets may use their threshold
rem    settings to override recording these log messages.
rem 
rem ============================================================================
rem 
rem 

rem ---------------------------------------------------------------------------
if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Start/Stop Script for the CATALINA Server
rem
rem Environment Variable Prequisites
rem
rem   CATALINA_HOME   May point at your Catalina "build" directory.
rem
rem   CATALINA_BASE   (Optional) Base directory for resolving dynamic portions
rem                   of a Catalina installation.  If not present, resolves to
rem                   the same directory that CATALINA_HOME points to.
rem
rem   CATALINA_OPTS   (Optional) Java runtime options used when the "start",
rem                   or "run" command is executed.
rem
rem   CATALINA_TMPDIR (Optional) Directory path location of temporary directory
rem                   the JVM should use (java.io.tmpdir).  Defaults to
rem                   %CATALINA_BASE%\temp.
rem
rem   JAVA_HOME       Must point at your Java Development Kit installation.
rem                   Required to run the with the "debug" argument.
rem
rem   JRE_HOME        Must point at your Java Runtime installation.
rem                   Defaults to JAVA_HOME if empty.
rem
rem   JAVA_OPTS       (Optional) Java runtime options used when the "start",
rem                   "stop", or "run" command is executed.
rem
rem   JSSE_HOME       (Optional) May point at your Java Secure Sockets Extension
rem                   (JSSE) installation, whose JAR files will be added to the
rem                   system class path used to start Tomcat.
rem
rem   JPDA_TRANSPORT  (Optional) JPDA transport used when the "jpda start"
rem                   command is executed. The default is "dt_shmem".
rem
rem   JPDA_ADDRESS    (Optional) Java runtime options used when the "jpda start"
rem                   command is executed. The default is "jdbconn".
rem
rem   JPDA_SUSPEND    (Optional) Java runtime options used when the "jpda start"
rem                   command is executed. Specifies whether JVM should suspend
rem                   execution immediately after startup. Default is "n".
rem
rem   JPDA_OPTS       (Optional) Java runtime options used when the "jpda start"
rem                   command is executed. If used, JPDA_TRANSPORT, JPDA_ADDRESS,
rem                   and JPDA_SUSPEND are ignored. Thus, all required jpda
rem                   options MUST be specified. The default is:
rem
rem                   -Xdebug -Xrunjdwp:transport=%JPDA_TRANSPORT%,
rem                       address=%JPDA_ADDRESS%,server=y,suspend=%JPDA_SUSPEND%
rem
rem  CATALINA_PID    (Optional) Path of the file which should contains the pid
rem                   of catalina startup java process, when start (fork) is used
rem
rem $Id: catalina.bat 656834 2008-05-15 21:04:04Z markt $
rem ---------------------------------------------------------------------------

rem Guess CATALINA_HOME if not defined
set CURRENT_DIR=%cd%
if not "%CATALINA_HOME%" == "" goto gotHome
set CATALINA_HOME=%CURRENT_DIR%
if exist "%CATALINA_HOME%\bin\openremote.bat" goto okHome
cd ..
set CATALINA_HOME=%cd%
cd %CURRENT_DIR%
:gotHome
if exist "%CATALINA_HOME%\bin\openremote.bat" goto okHome
echo The CATALINA_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:okHome

rem Get standard Java environment variables
if exist "%CATALINA_HOME%\bin\tomcat\setclasspath.bat" goto okSetclasspath
echo Cannot find %CATALINA_HOME%\bin\tomcat\setclasspath.bat
echo This file is needed to run this program
goto end
:okSetclasspath
set BASEDIR=%CATALINA_HOME%
call "%CATALINA_HOME%\bin\tomcat\setclasspath.bat" %1
if errorlevel 1 goto end

rem Add on extra jar files to CLASSPATH
if "%JSSE_HOME%" == "" goto noJsse
set CLASSPATH=%CLASSPATH%;%JSSE_HOME%\lib\jcert.jar;%JSSE_HOME%\lib\jnet.jar;%JSSE_HOME%\lib\jsse.jar
:noJsse
set CLASSPATH=%CLASSPATH%;%CATALINA_HOME%\bin\tomcat\bootstrap.jar

if not "%CATALINA_BASE%" == "" goto gotBase
set CATALINA_BASE=%CATALINA_HOME%
:gotBase

if not "%CATALINA_TMPDIR%" == "" goto gotTmpdir
set CATALINA_TMPDIR=%CATALINA_BASE%\temp
:gotTmpdir

if not exist "%CATALINA_BASE%\conf\logging.properties" goto noJuli
set JAVA_OPTS=%JAVA_OPTS% -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager -Djava.util.logging.config.file="%CATALINA_BASE%\conf\logging.properties"
:noJuli



rem ===== OPENREMOTE SETUP ==============================================================

set JAVA_OPTS=%JAVA_OPTS% -Djava.library.path="%CATALINA_BASE%\webapps\controller\WEB-INF\lib\native"
rem SKIP PAST FUNCTIONS (NO TRUE FUNCTION SUPPORT IN BATCH FILES)

rem Set up the directory that contains native libraries for the OpenRemote Controller
set LD_LIBRARY_PATH="%LD_LIBRARY_PATH%:$CATALINA_BASE/webapps/controller/WEB-INF/lib/native"

goto endFunctions

rem Sets up connection properties for Beehive services from environment variables, if they
rem have been set. Otherwise uses default values.
:setBeehiveServiceConfigurations
  rem Base URI for all Beehive services. This base URI will be used to construct service URIs
  rem unless a service-specific override has been set...
  if "%BEEHIVE_BASE_URI%" == "" set BEEHIVE_BASE_URI=https://designer.openremote.com

  rem Remote Command Service Variables...
  if "%BEEHIVE_REMOTE_SERVICE_PATH%" == "" set BEEHIVE_REMOTE_SERVICE_PATH=ccs/rest/
  if "%BEEHIVE_REMOTE_SERVICE_URI%" == "" set BEEHIVE_REMOTE_SERVICE_URI=%BEEHIVE_BASE_URI%/%BEEHIVE_REMOTE_SERVICE_PATH%

  rem Beehive Sync Service Variables...
  if "%BEEHIVE_SYNC_SERVICE_PATH%" == "" set BEEHIVE_SYNC_SERVICE_PATH=beehive/rest/
  if "%BEEHIVE_SYNC_SERVICE_URI%" == "" set BEEHIVE_SYNC_SERVICE_URI=%BEEHIVE_BASE_URI%/%BEEHIVE_SYNC_SERVICE_PATH%

  rem Beehive Device Discovery Variables...
  if "%BEEHIVE_DEVICE_DISCOVERY_SERVICE_PATH%" == "" set BEEHIVE_DEVICE_DISCOVERY_SERVICE_PATH=dds/rest/
  if "%BEEHIVE_DEVICE_DISCOVERY_SERVICE_URI%" == "" set BEEHIVE_DEVICE_DISCOVERY_SERVICE_URI=%BEEHIVE_BASE_URI%/%BEEHIVE_DEVICE_DISCOVERY_SERVICE_PATH%
 
  rem print variables if not running in service mode
  if "%SERVICE%" == "" (
    echo BEEHIVE_BASE_URI = %BEEHIVE_BASE_URI%
    echo BEEHIVE_REMOTE_SERVICE_PATH = %BEEHIVE_REMOTE_SERVICE_PATH%
    echo BEEHIVE_REMOTE_SERVICE_URI = %BEEHIVE_REMOTE_SERVICE_URI%
    echo BEEHIVE_SYNC_SERVICE_PATH = %BEEHIVE_SYNC_SERVICE_PATH%
    echo BEEHIVE_SYNC_SERVICE_URI = %BEEHIVE_SYNC_SERVICE_URI%
    echo BEEHIVE_DEVICE_DISCOVERY_SERVICE_PATH = %BEEHIVE_DEVICE_DISCOVERY_SERVICE_PATH%
    echo BEEHIVE_DEVICE_DISCOVERY_SERVICE_URI = %BEEHIVE_DEVICE_DISCOVERY_SERVICE_URI%
  )
  exit /B 0

:setControllerID
  if "%OPENREMOTE_CONTROLLER_ID%" == "" set OPENREMOTE_CONTROLLER_ID=1
  if "%SERVICE%" == "" echo OPENREMOTE_CONTROLLER_ID = %OPENREMOTE_CONTROLLER_ID%
  exit /B 0

:setTomcatConsoleLevel
  if "%CONTROLLER_CONSOLE_THRESHOLD%" == "WARN" set CONTROLLER_CONSOLE_THRESHOLD=WARNING
  set TOMCAT_SERVER_CONSOLE_LOG_LEVEL=%CONTROLLER_CONSOLE_THRESHOLD%
  if "%CONTROLLER_CONSOLE_THRESHOLD%" == "ERROR" set TOMCAT_SERVER_CONSOLE_LOG_LEVEL=SEVERE
  if "%CONTROLLER_CONSOLE_THRESHOLD%" == "WARNING" TOMCAT_SERVER_CONSOLE_LOG_LEVEL=WARNING
  if "%CONTROLLER_CONSOLE_THRESHOLD%" == "DEBUG" TOMCAT_SERVER_CONSOLE_LOG_LEVEL=FINE
  if "%CONTROLLER_CONSOLE_THRESHOLD%" == "TRACE" TOMCAT_SERVER_CONSOLE_LOG_LEVEL=FINER
  exit /B 0

:printTomcatEnvVariables
  echo Using CATALINA_BASE:   %CATALINA_BASE%
  echo Using CATALINA_HOME:   %CATALINA_HOME%
  echo Using CATALINA_TMPDIR: %CATALINA_TMPDIR%
  echo Using JAVA_HOME:       %JAVA_HOME%
  echo Using JRE_HOME:        %JRE_HOME%
  exit /B 0

rem  Executes the Tomcat runtime.
:executeTomcat
if "%SERVICE%" == "" goto redirectDone
set "ACTION=%ACTION% | head -c 50000 >> ""%CATALINA_BASE%\logs\container\stderrout.log"" 2>&1 """
:redirectDone

rem Execute Java with the applicable properties
%_EXECJAVA% %JAVA_OPTS% %CATALINA_OPTS% %JPDA_OPTS% %DEBUG_OPTS%^
 -Djava.endorsed.dirs="%JAVA_ENDORSED_DIRS%"^
 -Dcatalina.home="%CATALINA_HOME%"^
 -Dcatalina.base="%CATALINA_BASE%"^
 -Djava.io.tmpdir="%CATALINA_TMPDIR%"^
 -classpath "%CLASSPATH%"^
 -Dtomcat.server.console.log.level="%TOMCAT_SERVER_CONSOLE_LOG_LEVEL%"^
 -Dopenremote.controller.startup.log.level="%CONTROLLER_STARTUP_LOG_LEVEL%"^
 -Dopenremote.controller.console.threshold="%CONTROLLER_CONSOLE_THRESHOLD%"^
 -Dopenremote.remote.command.service.uri="%BEEHIVE_REMOTE_SERVICE_URI%"^
 -Dopenremote.device.discovery.service.uri="%BEEHIVE_DEVICE_DISCOVERY_SERVICE_URI%"^
 -Dopenremote.sync.service.uri="%BEEHIVE_SYNC_SERVICE_URI%"^
 -Dopenremote.controller.id="%OPENREMOTE_CONTROLLER_ID%"^
 %MAINCLASS% %ACTION%
  exit /B 0


:endFunctions

rem ----- Execute The Requested Command ---------------------------------------

set _EXECJAVA=%_RUNJAVA%
set MAINCLASS=org.apache.catalina.startup.Bootstrap
set ACTION=start
set SECURITY_POLICY_FILE=
set DEBUG_OPTS=
set JPDA=
set SERVICE=

if not ""%1"" == ""jpda"" goto noJpda
set JPDA=jpda
if not "%JPDA_TRANSPORT%" == "" goto gotJpdaTransport
set JPDA_TRANSPORT=dt_shmem
:gotJpdaTransport
if not "%JPDA_ADDRESS%" == "" goto gotJpdaAddress
set JPDA_ADDRESS=jdbconn
:gotJpdaAddress
if not "%JPDA_SUSPEND%" == "" goto gotJpdaSuspend
set JPDA_SUSPEND=n
:gotJpdaSuspend
if not "%JPDA_OPTS%" == "" goto gotJpdaOpts
set JPDA_OPTS=-agentlib:jdwp=transport=%JPDA_TRANSPORT%,address=%JPDA_ADDRESS%,server=y,suspend=%JPDA_SUSPEND%
:gotJpdaOpts
shift
:noJpda

if ""%1"" == ""debug"" goto doDebug
if ""%1"" == ""run"" goto doRun
if ""%1"" == ""start"" goto doStart
if ""%1"" == ""stop"" goto doStop
if ""%1"" == ""config"" goto doConfig

echo Usage: openremote.bat ( commands ... )
echo commands:
echo   debug
echo        Start OpenRemote Controller in debug mode (JPDA) in the current window
echo/
echo   run
echo        Start OpenRemote Controller in development mode in the current window
echo/
echo   start
echo        Start OpenRemote Controller as a background process
echo/
echo   stop
echo        Stop OpenRemote Controller
echo/
echo   stop -force
echo        Stop OpenRemote Controller (followed by taskkill)
echo/
echo   config
echo        Display information about OpenRemote Controller environment variable
echo        configuration.
echo/
echo/
goto end

:doDebug
  shift
  set _EXECJAVA=%_RUNJDB%
  set DEBUG_OPTS=-sourcepath "%CATALINA_HOME%\..\..\java"
  rem run Tomcat...
  call :executeTomcat
  goto end


:doRun
  shift
  rem Configure logging when 'blocking' run target is executed (assumes development
  rem or troubleshoot environment)...
  rem Default startup log to DEBUG level unless explicitly set with env variable...
  if not "%CONTROLLER_STARTUP_LOG_LEVEL%" == "" goto okLogLevel
  set CONTROLLER_STARTUP_LOG_LEVEL=DEBUG
  :okLogLevel
  rem Default standard out (console) output to INFO level unless explicitly set
  rem with env variable...
  if not "%CONTROLLER_CONSOLE_THRESHOLD%" == "" goto okLogThreshold
  set CONTROLLER_CONSOLE_THRESHOLD=INFO
  :okLogThreshold
  rem set Tomcat's console logging level to match controller console logging level,
  rem pass log options to JVM and print the env variable values...
  call :setTomcatConsoleLevel
  call :printTomcatEnvVariables
  rem Let the user know how the logging has been configured...
  echo/
  echo ---- Logging ----------------------------------------------------------
  echo/
  echo  Console (stdout) threshold [CONTROLLER_CONSOLE_THRESHOLD]: %CONTROLLER_CONSOLE_THRESHOLD%
  echo/ 
  echo  System logs:
  echo/
  echo    - Controller startup log [CONTROLLER_STARTUP_LOG_LEVEL]: %CONTROLLER_STARTUP_LOG_LEVEL%
  echo/
  echo/
  echo -----------------------------------------------------------------------
  rem Parameterize Beehive Service URIs...
  call :setBeehiveServiceConfigurations
  rem Parameterize fixed controller ID...
  call :setControllerID
  echo/
  echo/
  rem run Tomcat...
  call :executeTomcat
  goto end


:doStart
  set SERVICE=1
  shift
  rem Default startup log to INFO level unless explicitly set with env variable...
  if not "%CONTROLLER_STARTUP_LOG_LEVEL%" == "" goto okLogLevel
  set CONTROLLER_STARTUP_LOG_LEVEL=INFO
  :okLogLevel
  rem Default standard out (console) output to OFF level unless explicitly set
  rem with env variable...
  if not "%CONTROLLER_CONSOLE_THRESHOLD%" == "" goto okLogThreshold
  set CONTROLLER_CONSOLE_THRESHOLD=OFF
  :okLogThreshold
  rem set Tomcat's console logging level to match controller console logging level,
  rem pass log options to JVM and print the env variable values...
  call :setTomcatConsoleLevel
  call :printTomcatEnvVariables
  rem Parameterize Beehive Service URIs...
  call :setBeehiveServiceConfigurations
  rem Parameterize fixed controller ID...
  call :setControllerID
  echo/
  echo/
  if not "%OS%" == "Windows_NT" goto noTitle
  set _EXECJAVA=start "Tomcat" /MIN ""%_RUNJAVA%
  goto gotTitle
  :noTitle
  set _EXECJAVA=start %_RUNJAVA%
  :gotTitle

  rem get list of existing java PIDs so we can compare and determine new PID
  setlocal EnableExtensions EnableDelayedExpansion
  set "PID="
  set "OLDPIDS=p"
  for /f "TOKENS=1" %%a in ('wmic PROCESS where "Name='java.exe'" get ProcessID 2^>nul ^| findstr [0-9]') do (set "OLDPIDS=!OLDPIDS!%%ap")

  rem run Tomcat as service...
  call :executeTomcat

  rem Check for CATALINA_PID and find new java PID then store in PID file
  if "%CATALINA_PID%" == "" goto end
  for /f "TOKENS=1" %%a in ('wmic PROCESS where "Name='java.exe'" get ProcessID 2^>nul ^| findstr [0-9]') do (
    if "!OLDPIDS:p%%ap=zz!"=="%OLDPIDS%" (
      set "PID=%%a"
      echo %%a>%CATALINA_PID%
      goto end
    )
  )
  echo/>%CATALINA_PID%
  goto end


:doStop
  shift
  if not ""%1"" == ""-force"" goto setStop
  shift
  if "%CATALINA_PID%" == "" goto killError
  set /p PID= <%CATALINA_PID%
  goto setStop
  :killError
  echo Kill failed: CATALINA_PID not set
  goto end
  :setStop
  set ACTION=stop
  set CATALINA_OPTS=
  call :executeTomcat
  if "%PID%" == "" goto end
  taskkill /F /PID %PID%
  goto end

:doConfig
  echo/
  echo/
  echo The following environment variables can be set to configure controller behavior:
  echo/
  echo General
  echo -------
  echo/
  echo   BEEHIVE_BASE_URI:
  echo/
  echo     Set to use a different default base URI for all Beehive services. Setting the base
  echo     URI will not affect Beehive service application paths. Individual services may be
  echo     configured with separate domains and paths using their specific environment variables.
  echo/
  echo   OPENREMOTE_CONTROLLER_ID:
  echo/
  echo     Set when a non-interactive controller registration with a fixed controller ID is
  echo     required. Value should be an integer representing a unique controller ID for this
  echo     controller instance.
  echo/
  echo/
  echo Beehive Sync Service
  echo --------------------
  echo/
  echo   BEEHIVE_SYNC_SERVICE_PATH:
  echo/
  echo     Set to modify the application path of Beehive download/sync service. This path is
  echo     appended to BEEHIVE_BASE_URI value. Value should *not* include a leading URI slash.
  echo/
  echo   BEEHIVE_SYNC_SERVICE_URI:
  echo/
  echo     Set to use custom URI with Beehive sync service. This variable will override both
  echo     BEEHIVE_BASE_URI and BEEHIVE_SYN_SERVICE_PATH settings.
  echo/
  echo/
  echo Beehive Remote Command Service
  echo ------------------------------
  echo/
  echo   BEEHIVE_REMOTE_SERVICE_PATH:
  echo/
  echo     Set to modify the application path of Beehive remote command service. This path is
  echo     appended to BEEHIVE_BASE_URI value. Value should *not* include a leading URI slash.
  echo/
  echo   BEEHIVE_REMOTE_SERVICE_URI:
  echo/
  echo     Set to use custom URI for Beehive remote service. This variable will override both
  echo     BEEHIVE_BASE_URI and BEEHIVE_REMOTE_SERVICE_PATH settings.
  echo/
  echo/
  echo Beehive Device Discovery Service
  echo --------------------------------
  echo/
  echo   BEEHIVE_DEVICE_DISCOVERY_SERVICE_PATH:
  echo/
  echo     Set to modify the application path of Beehive device discovery service. This path is
  echo     appended to BEEHIVE_BASE_URI value. Value should *not* include a leading URI slash.
  echo/
  echo   BEEHIVE_DEVICE_DISCOVERY_SERVICE_URI:
  echo/
  echo     Set to use custom URI for Beehive device discovery service. This variable will override
  echo     both BEEHIVE_BASE_URI and BEEHIVE_DEVICE_DISCOVERY_SERVICE_PATH settings.
  echo/
  echo/
  goto end

:end
