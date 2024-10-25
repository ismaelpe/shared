###jenkinsSharedLibrary

#### Notas refactor ciclo de vida generación de conectores de arquitectura (ADS/CICS/ASE)

Hay cierta similitud en la generación de ciertos conectores con servicios de arquitectura en lo que respecta a su ciclo de vida y utilización.
Hasta ahora la implementación de estos ciclos de vida se ha tratado de forma separada. Con la inclusión del tercer conector (ASE) debemos considerar la refactorización de la solución para unificar las partes del código comunes y separar únicamente las particulares,
con la finalidad de facilitar la incorporación de nuevos conectores a este ciclo de vida en el futuro.

En estas notas, nos referiremos a _scripts_, como los ficheros `.groovy` en `/vars` que conforman las pipelines declarativas ejecutadas por Jenkins.
Por otro lado, nos referiremos a _clases_, como los ficheros `.groovy` en `/src` que contienen clases que pueden ser instanciadas y utilizadas por los _scripts_.
Las _clases_ pueden ser testeadas independientemente fuera de Jenkins, mediante los test unitarios contenidos en `tst`.
Se debe maximizar el código en clases, ya que el testeo en Jenkins es lento y no depurable (no hay debugger).

Las siguientes notas deben tomarse como punto a considerar y no como aportación forzosa a la solución:

* Unificación de código encargado del manejo del versionado

    Actualmente existe código repetido (no sólo en el ciclo de vida de conectores) encargado de modificar las versiones de los artefactos (p.e. incrementar versión minor o RC).
    Mucho de este código podría unificarse en clases que puedan ser testeadas independientemente.
    Se sugiere la utilización de la clase `MavenVersionUtilities`, que reune ya algunos métodos para tratar este tema.
    
* Reutilización de las pipelines de provisioning, build y cierre

    Se debe estudiar la posibilidad de unificar las pipelines de provisioning, build y cierre.
    Esto es razonable porque el ciclo de vida de los tres conectores es el mismo (evolución de una SNAPSHOT a RC y a RELEASE dentro de una rama de feature, para juntarla con `master` al final)
    Sin embargo, debe tenerse en cuenta que cada pipeline puede tener pasos adicionales que no tengan las otras, dificultando la integración y haciendo el código más difícil de analizar y corregir.
    
    Ninguna acción hecho de momento.
    
* Reutilización de las clases de resultado de pipeline `PipelineData`

    Por la misma razón que el punto anterior. Las acciones disponibles en IDECUA están relacionadas con el ciclo de vida y este es común a los tres conectores.
    Además, estos objetos (`PipelineData` de cada uno de los conectores) contienen lógica común que probablemente se puedan unificar. 
    
    Ninguna acción hecha de momento. 
    
* Unificación de clases `ClientInfo`

    Hay clases con información del conector que está siendo generado llamadas `ClientCicsHISInfo`, `ADSClientInfo`.
    Esencialmente contienen información sobre el conector que se envía a GPL, así como métodos de versionado (tratados en un punto anterior y que podrian retirarse)
    y otros métodos susceptibles de ser unificados en clases dedicadas independientes.
    
    Por el momento se ha creado una interfaz `IClientInfo` y una implementación `ClientInfo` para su uso en ASE.
    La idea es unificar las clases Info de CICS y ADS en `ClientInfo` o crear nuevas clases que extiendan esta última o implementen la interfaz.
    

* Unificación de los scripts de inicio de pipeline en GPL `sendPipelineStartToGPL*`

    Ahora existen tres scripts `sendPipelineStartToGPL*`, uno general, otro para CICS y otro para ADS.
    La diferencia entre ellos es que el general trata con una instancia de PomXmlStructure y los otros dos con su instancia correspondiente de ClientInfo.
    Se sugiere incorporar un método sobrecargado en `sendPipelineStartToGPL` que trate con instancias de `IClientInfo` y eliminar los dos scripts dedicados.
    La implementación de ASE ya utiliza este método sobrecargado.

* Unificación de los scripts de publicación en catálogo `publishArtifactInCatalog`

    Igual que el anterior. Para ASE se sobrecargará el método de envío en `publishArtifactInCatalog`.
    Se sugiere, después de verificar que la funcionalidad es común, hacer los mismo con ADS/CICS y eliminar `publicArtifactClientADSInCatalog` y `publicArtifactClientADSInCatalog`.
    
* Unificación de métodos `getInfoGit*`

    Actualmente existen 4 scripts `getInfoGit`. Uno general y uno específico por conector.
    Se debe valorar si pueden unificarse todos en uno, o bien, unificar los tres relacionados con conectores en uno.

