package com.project.alm   
   
public enum CloudWorkflowStates{
    NEW_DEPLOY ("NEW_DEPLOY"), 
    ELIMINATE_NEW_ROUTE_TO_CURRENT_APP ("ELIMINATE_NEW_ROUTE_TO_CURRENT_APP"),
	ADD_STABLE_ROUTE_TO_NEW_APP ("ADD_STABLE_ROUTE_TO_NEW_APP"),
	ELIMINATE_STABLE_ROUTE_TO_CURRENT_APP ("ELIMINATE_STABLE_ROUTE_TO_CURRENT_APP"),
	ELIMINATE_CURRENT_APP ("ELIMINATE_CURRENT_APP"),
	END("END")

    private String name;

    private CloudWorkflowStates(String s){
        name=s;
    }

    public boolean equalsName(String other){
        return name.equals(other);
    }
    static   CloudWorkflowStates valueOfType( String other){
        values().find { it.name == other}
    }

	public String toString(){
		return name
	}

}