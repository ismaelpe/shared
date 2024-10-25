package com.caixabank.absis3

class DistributionPROResolver {
	
	public static DistributionModePRO determineFirstDistributionModeOnPRO(boolean deployOnSingleCenter) {
        if ( ! deployOnSingleCenter ) {
            return DistributionModePRO.CANARY_ON_ALL_CENTERS
        } else {
            return DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_1
        }
    }
	
	public static DistributionModePRO determineNextDistributionModeOnPRO(DistributionModePRO currentDistributionMode) {
		if ( currentDistributionMode == DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_1 ) {
			return DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_2
		} else if ( currentDistributionMode == DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_2 ) {
			return DistributionModePRO.CONCLUDED
		} else if ( currentDistributionMode == DistributionModePRO.CANARY_ON_ALL_CENTERS ) {
			return DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_1
		}
	}

}