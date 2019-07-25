import groovyx.net.http.HTTPBuilder
import net.sf.json.JSONArray
import org.apache.http.HttpRequest
 
import org.apache.http.protocol.HttpContext
import org.apache.http.HttpRequestInterceptor
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.bc.issue.IssueService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.issue.IssueInputParameters
import com.atlassian.jira.issue.customfields.manager.OptionsManager
import com.atlassian.jira.issue.customfields.option.Options


def customFieldManager = ComponentAccessor.getCustomFieldManager()
final int senior_approver_required_field_id = 10600;
final int auto_approve_field_id = 10601;

def auto_approve_field = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(auto_approve_field_id);


//def customFieldManager = ComponentAccessor.getComponent(CustomFieldManager);

//function to check a Boolean value in Jira
def select_checkbox_value(int fieldid, value_name ) {
    def optionsManager = ComponentAccessor.getComponent(OptionsManager);
    IssueService issueService = ComponentAccessor.getComponent(IssueService);
    IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
    def customField=ComponentAccessor.getCustomFieldManager().getCustomFieldObject(fieldid);
    def user = ComponentAccessor.getJiraAuthenticationContext().loggedInUser
    Options options = optionsManager.getOptions(customField.getConfigurationSchemes().first().getOneAndOnlyConfig());
    def optionToSelect = options.find { it.value == value_name } 
    IssueManager issueManager = ComponentAccessor.getIssueManager();
    issue.setCustomFieldValue(customField, [optionToSelect])
    issueManager.updateIssue(user, issue, EventDispatchOption.DO_NOT_DISPATCH, false);
    issue.store();
    }

def query_jira_api_issue(){
    def JIRA_API_URL = "https://jira"
     
    def jira = new HTTPBuilder(JIRA_API_URL);
    jira.client.addRequestInterceptor(new HttpRequestInterceptor() {
    void process(HttpRequest httpRequest, HttpContext httpContext) {
    httpRequest.addHeader('Authorization', 'Basic ' + 'admin:admin'.bytes.encodeBase64().toString())
    }
    })
    //https://jira/rest/api/latest/issue/BBEITSM-33
    def resp = jira.get(path: '/rest/api/latest/issue/'+issue) 
	return resp
}

def has_recieved_at_least_x_approvals(resp,approvals_needed){
    // We need to know who has already approved the request. 
    // we use the api as the java classes dont seem to allow us to know it
    
    approvers = resp['fields']['customfield_10000']['approvers']
 
    approvals_count =0
    for (approver in approvers) { //Loop over approvers, if any has approved, increase approvals count
        log.info(""+approver['approver']['displayName'])
        log.info(""+approver['approverDecision'])
		if ("approved" in approver['approverDecision'])
		{
			
			approvals_count +=1
		}     
        
    }
	def approved=approvals_count>=approvals_needed //true or false
	log.info(""+approved)
	return approved //also returns the response, we might need it
}



//def senior_approver_required_field = customFieldManager.getCustomFieldObjectByName("Senior Management Approval")
//def senior_approver_required = issue.getCustomFieldValue(senior_approver_required_field)

//SCRIPT START
def senior_approver_required_field = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(senior_approver_required_field_id);
def senior_approver_required = issue.getCustomFieldValue(senior_approver_required_field)
//we get the custom field value to determine if it is required to have a senior manager's approval
log.info("Senior Approver Required ?" + senior_approver_required)
if (senior_approver_required){
log.info("needs approval")

} 
else 
{
    //We should auto approve if the request is waiting for an approval (could be already approved)
    //
    def resp = query_jira_api_issue()
    def approved = has_recieved_at_least_x_approvals(resp,1)
    def status = resp['fields']['customfield_10001']['currentStatus']['status']
    if (status=="Waiting for approval"){
	if (approved) {
     	    log.info("auto Approving")
        	
            select_checkbox_value(auto_approve_field_id, "True")
    	}
    }
    else 
	{
      log.info("already approved")
    } 
}
