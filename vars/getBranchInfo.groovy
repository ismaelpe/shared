import com.caixabank.absis3.BranchStructure
import com.caixabank.absis3.EchoLevel

BranchStructure call() {
    return buildBranchStructure("${env.gitlabBranch}", this)
}

BranchStructure call(String branchName) {
    return buildBranchStructure(branchName, this)
}

static BranchStructure buildBranchStructure(String branchName, def script) {
    BranchStructure branchStructure = new BranchStructure();

    branchStructure.branchName = branchName
    branchStructure.init()

	script.printOpen("${branchStructure.toString()}", EchoLevel.INFO)
	
    return branchStructure
}
