mkdir .\..\jcoJars\jcoql-ds-client\deps\repository\jco\ql\jcods\jcoql-ds-core\1.0.1\
mkdir .\..\jcoJars\jcoql-ds-server\deps\repository\jco\ql\jcods\jcoql-ds-core\1.0.1\
mkdir .\..\jcoJars\jcoql-ds-client\deps\repository\jcoql-parent\jcoql-model\1.0.0\
mkdir .\..\jcoJars\jcoql-ds-server\deps\repository\jcoql-parent\jcoql-model\1.0.0\

copy .\..\jcoJars\jcoql-engine\deps\repository\jco\ql\jcods\jcoql-ds-core\1.0.1\jcoql-ds-core-1.0.1.jar       .\..\jcoJars\jcoql-ds-client\deps\repository\jco\ql\jcods\jcoql-ds-core\1.0.1\
copy .\..\jcoJars\jcoql-engine\deps\repository\jco\ql\jcods\jcoql-ds-core\1.0.1\jcoql-ds-core-1.0.1.jar       .\..\jcoJars\jcoql-ds-server\deps\repository\jco\ql\jcods\jcoql-ds-core\1.0.1\
copy .\..\jcoJars\jcoql-engine\deps\repository\jcoql-parent\jcoql-model\1.0.0\jcoql-model-1.0.0.jar           .\..\jcoJars\jcoql-ds-client\deps\repository\jcoql-parent\jcoql-model\1.0.0\
copy .\..\jcoJars\jcoql-engine\deps\repository\jcoql-parent\jcoql-model\1.0.0\jcoql-model-1.0.0.jar           .\..\jcoJars\jcoql-ds-server\deps\repository\jcoql-parent\jcoql-model\1.0.0\

del .\..\jcoJars\jcoql-parent-1.0.0.jar

   

