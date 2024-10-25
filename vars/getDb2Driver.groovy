import com.caixabank.absis3.Environment

String call(String environment) {
	def driverDb2=""
	if (Environment.valueOfType(environment)==Environment.PRE) {
		driverDb2=env.DB2_DRIVER_PRE
	}else if (Environment.valueOfType(environment)==Environment.PRO) {
		driverDb2=env.DB2_DRIVER_PRO
		//En este no deveriamos llegar nunca
	}else if (Environment.valueOfType(environment)==Environment.TST) {
		driverDb2=env.DB2_DRIVER_TST
	}else if (Environment.valueOfType(environment)==Environment.DEV) {
		driverDb2=env.DB2_DRIVER_TST
	}else {
		driverDb2=env.DB2_DRIVER_TST
	} 
	return driverDb2
}
