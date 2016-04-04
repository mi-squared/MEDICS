call xcopy /s/I/Y/Q %1\bin\*.* %2\bin
call xcopy /s/I/Y/Q %1\classes\*.* %2\classes
call xcopy /s/I/Y/Q %1\lib\*.* %2\lib
call xcopy /s/I/Y/Q %1\resource\*.* %2\resource
call xcopy /s/I/Y/Q %1\docs\*.* %2\docs
call xcopy /s/I/Y/Q %1\image\*.* %2\image

rem Save copy of original config.ini off into upgrade dir    

call xcopy /s/I/Y/Q %2\conf\*.* %1\conf_original
rem Upgrade all elements from upgrade dir
call xcopy /s/I/Y/Q %1\conf\*.* %2\conf
rem Copy saved original config.ini back into destination dir
call copy /Y %1\conf_original\config.ini %2\conf\config.ini
rem Copy saved original field_defaults.ini back into destination dir
call copy /Y %1\conf_original\field_defaults.ini %2\conf\config.ini

call xcopy /s/I/Y/Q %1\*.sh %2\.
call xcopy /s/I/Y/Q %1\*.bat %2\.
