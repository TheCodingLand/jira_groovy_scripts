import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.IssueInputParametersImpl
import com.atlassian.jira.issue.comments.CommentManager;

def get_Priority(urgency,impact) {
    if (!urgency) { return "Minor"}
    if (!impact) { return "Minor"}
    urgency = ""+urgency
    impact = ""+impact
    def priorityMatrix = new HashMap<String, HashMap>();
    priorityMatrix.put("Critical", ["Extensive / Widespread": "Blocker", "Significant / Large": "Blocker", "Moderate / Limited": "High", "Minor / Localized": "Medium"])
    priorityMatrix.put("High" , ["Extensive / Widespread": "Blocker", "Significant / Large": "High", "Moderate / Limited": "Medium", "Minor / Localized": "Medium"])
    priorityMatrix.put("Medium", ["Extensive / Widespread": "High", "Significant / Large": "Medium", "Moderate / Limited": "Medium", "Minor / Localized": "Minor"])
    priorityMatrix.put("Low" , ["Extensive / Widespread": "Medium", "Significant / Large": "Medium", "Moderate / Limited": "Minor", "Minor / Localized": "Minor"])
    def t =priorityMatrix.get(urgency)
	log.info("test :" +t)
    
    def priority = t[impact]
    log.info("priority :" +priority)
	return priority
    }



def issue = event.issue
log.info(""+issue.priority.name) 

def customFieldManager = ComponentAccessor.getCustomFieldManager()
def constantsManager = ComponentAccessor.getConstantsManager()
def issueService = ComponentAccessor.getIssueService()

def user = ComponentAccessor.getJiraAuthenticationContext().getUser()

def urgency_field = customFieldManager.getCustomFieldObjectByName("Urgency")
def urgency = issue.getCustomFieldValue(urgency_field) ;
def impact_field = customFieldManager.getCustomFieldObjectByName("Impact")
def impact = issue.getCustomFieldValue(impact_field);

def priorityName=get_Priority(urgency, impact)

log.info("priority is : " + priorityName)

def priority_objects = constantsManager.getPriorities()
def priority =priority_objects[0]

for (value in priority_objects) { if (value.name == priorityName) { 
    priority=value
    log.info("" +value) }} 


def issueInputParameters = new IssueInputParametersImpl()

issueInputParameters.setPriorityId(priority.id)
issueInputParameters.setSkipScreenCheck(true)

def validationResult = issueService.validateUpdate(user, issue.id, issueInputParameters)

if (validationResult.isValid()) {
    issueService.update(user, validationResult)
} else {
    log.warn(""+validationResult.errorCollection.errors)
}
