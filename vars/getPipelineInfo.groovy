import com.project.alm.PipelineJob
import com.project.alm.GlobalVars

//Pipeline unico que construye todos los tipos de artefactos
//Recibe los siguientes parametros
//type: String con el tipo de artifact el repo del qual ha lanzado el PipeLine
def call() {
    PipelineJob pipeline = new PipelineJob()

    return pipeline;
}
