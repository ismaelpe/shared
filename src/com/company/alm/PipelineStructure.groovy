package com.project.alm


abstract class PipelineStructure {

    String plataforma = GlobalVars.PLATAFORMA_AppPortal
    String nombre = ""
    String pipelineId
    ResultPipelineData resultPipelineData

    def abstract getStages()


    def abstract getResult(boolean result)

    PipelineStructure(String pipelineIdParam, String nombrePipelineParam) {
        nombre = nombrePipelineParam
		String date = new Date(System.currentTimeMillis()).format("yyyy-MM-dd_HH:mm:ss");
        pipelineId = pipelineIdParam+"_"+date
    }
    
    String getStageName(String stageId) {
        def stages = getStages()
        def stage = stages.find { it.id == stageId }
        if(stage == null) return "unknown"
        return stage.nombre
    }

    String toString() {
        return "PipelineStructure:\n" +
                "\tplataforma: ${plataforma}\n" +
                "\tnombre: ${nombre}\n" +
                "\tpipelineId: ${pipelineId}\n" +
                "\tresultPipelineData: ${resultPipelineData?resultPipelineData.toString():''}\n"
    }
}