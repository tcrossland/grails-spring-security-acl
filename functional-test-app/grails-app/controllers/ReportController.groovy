import com.testacl.Report
import grails.plugin.springsecurity.acl.AclClass
import grails.plugin.springsecurity.acl.AclEntry
import grails.plugin.springsecurity.acl.AclObjectIdentity
import grails.plugin.springsecurity.acl.AclSid
import grails.plugin.springsecurity.acl.test.ReportService
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.dao.DataIntegrityViolationException

@Secured('ROLE_USER')
class ReportController {

	static defaultAction = 'list'

	ReportService reportService

	def list() {
		params.max = Math.min(params.max ? params.int('max') : 1000, 1000)
		[reports: reportService.list(params), reportCount: reportService.count()]
	}

	def create(Report report) {
		[report: report]
	}

	def save(String name, Integer number) {
		def report = reportService.create(name, number)
		if (!renderWithErrors('create', report)) {
			redirectShow "Report $number created", number
		}
	}

	def show(Integer number) {
		def report = find(number)
		if (!report) return

		[report: report]
	}

	def edit(Integer number) {
		def report = find(number)
		if (!report) return

		[report: report]
	}

	def update(String name, Integer number) {
		def report = find(number)
		if (!report) return

		reportService.update report, name
		if (!renderWithErrors('edit', report)) {
			redirectShow "Report $number updated", number
		}
	}

	def delete(Integer number) {
		def report = find(number)
		if (!report) return

		try {
			reportService.delete report
			flash.message = "Report $number deleted"
			redirect action: 'list'
		}
		catch (DataIntegrityViolationException e) {
			redirectShow "Report $number could not be deleted", number
		}
	}

	def grant(Integer number, String recipient, Integer permission) {
		def report = find(number)
		if (!report) return

		if (!request.post) {
			return [report: report]
		}

		reportService.addPermission report, recipient, permission

		redirectShow "Permission $permission granted on Report $number to $recipient", number
	}

	def dump() {

		def html = new StringBuilder()

		html << "<br/>${AclClass.count()} AclClass:<br/>"
		AclClass.list().each { AclClass aclClass ->
			html << "&nbsp;&nbsp;&nbsp;ID: $aclClass.id, class name: $aclClass.className<br/>"
		}

		html << "<br/>${AclSid.count()} AclSid:<br/>"
		AclSid.list().each { AclSid aclSid ->
			html << "&nbsp;&nbsp;&nbsp;ID: $aclSid.id, SID: $aclSid.sid : principal: $aclSid.principal<br/>"
		}

		html << "<br/>${AclObjectIdentity.count()} AclObjectIdentity:<br/>"
		AclObjectIdentity.list().each { AclObjectIdentity aoi ->
			html << "&nbsp;&nbsp;&nbsp;ID: $aoi.id, objectId: $aoi.objectId, aclClass: $aoi.aclClass.id($aoi.aclClass.className), owner: $aoi.owner.id<br/>"
		}

		html << "<br/>${AclEntry.count()} AclEntry:<br/>"
		AclEntry.list().each { AclEntry aclEntry ->
			html << "&nbsp;&nbsp;&nbsp;ID: $aclEntry.id, aclObjectIdentity: $aclEntry.aclObjectIdentity.id, order: $aclEntry.aceOrder, sid: $aclEntry.sid.id($aclEntry.sid.sid), mask: $aclEntry.mask<br/>"
		}

		render html.toString()
	}

	private Report find(Integer number) {
		def report = number ? reportService.get(idForNumber(number)) : null
		if (!report) {
			flash.message = "Report not found with number $number"
			redirect action: 'list'
		}
		report
	}

	private void redirectShow(String message, int number) {
		flash.message = message
		redirect action: 'show', params: [number: number]
	}

	private boolean renderWithErrors(String view, Report report) {
		if (report.hasErrors()) {
			render view: view, model: [report: report]
			return true
		}
		false
	}

	private Long idForNumber(int number) {
		Report.findByNumber(number)?.id
	}
}
