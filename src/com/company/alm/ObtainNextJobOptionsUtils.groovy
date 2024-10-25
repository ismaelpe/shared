package com.project.alm

import com.project.alm.*

class ObtainNextJobOptionsUtils {

    public static NextJobOptions obtainNextJobInformation(String actionFlag, List actions) {
        NextJobOptions nextJobOptions = new NextJobOptions();
        if (actions != null) {
            List<BuildParameter> listOfBuildParameters = null;
            for (def it : actions) {
                if (true == it[actionFlag]) {
                    listOfBuildParameters = new ArrayList<BuildParameter>();
                    if (it["parametros"] != null) {
                        for (def param : it["parametros"]) {
                            BuildParameter parameter = new BuildParameter();
                            parameter.init(param)
                            listOfBuildParameters.add(parameter);
                        }
                    }

                    nextJobOptions.nextJobName = it["destino"];
                    nextJobOptions.type = it["tipoAccion"]
                }
            }
            nextJobOptions.parameters = listOfBuildParameters;

        }
        return nextJobOptions
    }
	
	public static boolean hasNextJob(String actionFlag, List actions) {
		if (actions != null) {
			for (def it : actions) {
				if (true == it[actionFlag]) {
					return true;
				}
			}
		}
		return false;
	}

}