echo off
echo Upgrading MEDICS ...
cd medics
cd medics_upgrade
call bin\java\windows\bin\java -Dop.sys=win -Dis.upgrade=true -classpath "classes\production\ehs;lib\commons-net.jar;lib\activation.jar;lib\mailapi.jar;lib\smtp.jar;lib\swingx.jar;lib\sigilent.jar;lib\itext.jar;lib\jaxen-dom4j.jar;lib\jaxen-dom.jar;lib\jaxen-exml.jar;lib\dom.jar;lib\jaxen-full.jar;lib\jdom.jar;lib\jaxp-api.jar;lib\jaxen-jdom.jar;lib\jaxen-core.jar;lib\saxpath.jar;lib\sax.jar;lib\PDFRenderer.jar;lib\forms_rt.jar;lib\idea_rt.jar" maqs.ehs.form.Installation
pause
cd ..
rmdir /q/s medics_upgrade
echo Done.
echo on