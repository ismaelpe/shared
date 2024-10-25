import com.project.alm.*


def call(def label, args = [:], Closure body) {
	Date start=new Date()
	printOpen ("Init Stage ${label} StartTime: ${start}",EchoLevel.INFO)
	try {
		body()
	}catch (err) {
		org.codehaus.groovy.runtime.StackTraceUtils.printSanitizedStackTrace(err)
		printOpen ("Error Stage  ${label} ${err}",EchoLevel.ERROR)
		throw err
	}finally{
		Date end=new Date()
		printOpen ("End Stage  ${label} EndTime: ${end} Duration: ${(end.getTime()-start.getTime())/1000}sec",EchoLevel.INFO)
	}
}

def withoutNotification(def label,args = [:], Closure body) {
	Date start=new Date()
	printOpen ("Init Stage  ${label} StartTime: ${start}",EchoLevel.INFO)
	try {
		body()
	}catch (err) {
		org.codehaus.groovy.runtime.StackTraceUtils.printSanitizedStackTrace(err)
		printOpen ("Error Stage  ${label} ${err}",EchoLevel.ERROR)
		throw err
	}finally{
		Date end=new Date()
		printOpen ("End Stage  ${label} EndTime: ${end} Duration: ${(end.getTime()-start.getTime())/1000}sec",EchoLevel.INFO)
	}
}
