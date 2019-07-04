import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.IssueInputParametersImpl
import com.atlassian.jira.issue.comments.CommentManager;


Priority_Blocker = new HashMap<String, List>();
Priority_Critical = new HashMap<String, List>();
Priority_High = new HashMap<String, List>();
Priority_Medium = new HashMap<String, List>();
Priority_Low = new HashMap<String, List>();


Priority_Blocker.put("Extensive / Widespread", ["Critical"])
Priority_Critical.put("Extensive / Widespread", ["High"])
Priority_Critical.put("Significant / Large", ["Critical"])

Priority_High.put("Significant / Large", ["Medium"])
Priority_High.put("Moderate / Limited", ["High"])
Priority_High.put("Extensive / Widespread", ["Medium"])

Priority_Medium.put("Minor / Localized", ["Critical", "High"])
Priority_Medium.put("Moderate / Limited", ["High", "Medium"])
Priority_Medium.put("Significant / Large", ["Medium", "Low"])

Priority_Low.put("Moderate / Limited", ["Low"])
Priority_Low.put("Minor / Localized", ["Medium","Low"])


def get_Priority(urgency,impact) {
    urgency = ""+urgency
    impact = ""+impact
    if (!urgency) { return "Minor"}
    if (!impact) { return "Minor"}
    if (urgency in Priority_Blocker.get(impact)) { return "Blocker"}
    if (urgency in Priority_Critical.get(impact)) { return "Critical"}
    if (urgency in Priority_High.get(impact)) { return "High"}
    if (urgency in Priority_Medium.get(impact)) { return "Medium"}
    if (urgency in Priority_Low.get(impact)) { return "Low"}
    }



log.info(""+issue.priority.name)

def customFieldManager = ComponentAccessor.getCustomFieldManager()
def constantsManager = ComponentAccessor.getConstantsManager()
def issueService = ComponentAccessor.getIssueService()

def user = ComponentAccessor.getJiraAuthenticationContext().getUser()

def urgency_field = customFieldManager.getCustomFieldObjectByName("Urgency")
def urgency = issue.getCustomFieldValue(urgency_field) ;
def impact_field = customFieldManager.getCustomFieldObjectByName("Impact")
def impact = issue.getCustomFieldValue(impact_field);

priorityName=get_Priority(urgency, impact)

log.info("priorty is : " + priorityName)

def priority_objects = constantsManager.getPriorities()

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
    log.warn validationResult.errorCollection.errors
}



