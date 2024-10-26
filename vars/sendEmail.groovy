import com.project.alm.*
import java.util.Date

def call(String subject,String to, String body, String body1) {
	
	if (to!=null && !"".equals(to)){
		
		def date = new Date()
		String datePart = date.format("dd/MM/yyyy")
		
		
		String bodyEmail = "<p>Buenos dias, </p><p>El deploy de la app ${body} ha sido ${body1} en la fecha ${datePart}.</p><p>Visualizar logs de la pipeline: ${GlobalVars.JOB_DISPLAY_CONFLUENCE}</p><p>Saludos.</p>"
			
		emailext(body: "${bodyEmail}"
				, mimeType: 'text/html'
				, recipientProviders: [[$class: 'DevelopersRecipientProvider']]
				, replyTo: ''
				, from: "${GlobalVars.EMAIL_FROM_ALM}"
				, to: "${to}"
				, subject: "[Alm3-SRV-Cloud] ${subject} ${datePart} ")

	}
}


def call(String subject,String to, String body) {
	
	if (to!=null && !"".equals(to)){
		
		def date = new Date()
		String datePart = date.format("dd/MM/yyyy")
		
					
		emailext(body: "${body}"
				, mimeType: 'text/html'
				, recipientProviders: [[$class: 'DevelopersRecipientProvider']]
				, replyTo: ''
				, from: "${GlobalVars.EMAIL_FROM_ALM}"
				, to: "${to}"
				, subject: "[Alm3-SRV-Cloud] ${subject} ${datePart} ")

	}
}

def call(String subject,String from,String to, String replyTo,String attachmentsPattern, String body) {

	if (to!=null && !"".equals(to)){

		def date = new Date()
		String datePart = date.format("dd/MM/yyyy")

		emailext(
			mimeType: 'text/html',
			replyTo: "${replyTo}",
			attachmentsPattern: "${attachmentsPattern}",
			body: "${body}",
			subject: "[Alm3-SRV-Cloud] ${subject} ${datePart} ",
			from: "${from}",
			to: "${to}"
		)

	}
}

def call(String subject,String from,String to, String replyTo,String attachmentsPattern, String body, String mimeType) {

    if (to!=null && !"".equals(to)){

        def date = new Date()
        String datePart = date.format("dd/MM/yyyy")

        emailext(
            mimeType: "${mimeType}",
            replyTo: "${replyTo}",
            attachmentsPattern: "${attachmentsPattern}",
            body: "${body}",
            subject: "[Alm3-SRV-Cloud] ${subject} ${datePart} ",
            from: "${from}",
            to: "${to}"
        )

    }

}

