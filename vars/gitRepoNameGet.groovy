/**
 *  Obtiene el nombre del repostorio
 */

def call() {
    return scm.getUserRemoteConfigs()[0].getUrl().tokenize('/')[-1].split("\\.")[0]
}
