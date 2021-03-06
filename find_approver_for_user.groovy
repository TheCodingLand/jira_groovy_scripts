import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.comments.CommentManager;
 
final int insightSchemaId = 1;
final int insightUserAttributeIdFqdnManager = 202;
final int insightUserAttributeIdName = 195; 
final int customFieldApproversId = 10007;

final int account_status_code_field_id = 205




def userSearchService = ComponentAccessor.getComponent(UserSearchService)
def fqdnManager = "";
def userName = "";

ApplicationUser applicationUser = null;
List<ApplicationUser> approvers = new ArrayList<ApplicationUser>();

Class iqlFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.IQLFacade"); 
def iqlFacade = ComponentAccessor.getOSGiComponentInstanceOfType(iqlFacadeClass);

def get_field_from_iql_query(schemaid, iql, fieldid) {
    Class iqlFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.IQLFacade"); 
	def iqlFacade = ComponentAccessor.getOSGiComponentInstanceOfType(iqlFacadeClass);
    def objects = iqlFacade.findObjectsByIQLAndSchema(schemaid, iql);

	for(objectAttributeBean in objects[0].getObjectAttributeBeans()){
        if(objectAttributeBean.getObjectTypeAttributeId() == fieldid){
        try{
                value = objectAttributeBean.getObjectAttributeValueBeans()[0].getValue();
            	return value;
            }catch(Exception ex){}
        }
	}
    }

try{
/* Get reporter ApplicationUser (The current user of the app making the request) */
def reporter = ComponentAccessor.getUserManager().getUserByKey(issue.reporterId);
def display_name = reporter.getDisplayName()
def LoginName = reporter.getUsername()

/* our LoginName field is the samAccountName */

def fqdn_of_Manager = get_field_from_iql_query(insightSchemaId,"objectType=\"Users\" AND \"Login Name\" IN (\""+LoginName+"\") ",insightUserAttributeIdFqdnManager)
log.info("FQDN of Manager : " + fqdn_of_Manager)
def manager_user_name = get_field_from_iql_query(insightSchemaId,"objectType=\"Users\" AND \"FQDN\" IN (\""+fqdn_of_Manager+"\")",insightUserAttributeIdName)
log.info("Manager User Name : " + manager_user_name)


applicationUser = userSearchService.findUsersByFullName(manager_user_name)[0];
/* Get the custom field for the approvers */
def cfApprovers = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(customFieldApproversId);
def cfApproversValue = issue.getCustomFieldValue(cfApprovers) ;

for(ApplicationUser u in cfApproversValue){
    approvers.add(u);
}
if(!approvers.contains(applicationUser)){
	approvers.add(applicationUser);
}

issue.setCustomFieldValue(cfApprovers, approvers);
Object cfVal = issue.getCustomFieldValue(cfApprovers);

IssueChangeHolder changeHolder = new DefaultIssueChangeHolder();
cfApprovers.updateValue(null, issue, new ModifiedValue("", approvers), changeHolder);

issue.store();
}catch(Exception ex){
    CommentManager commentMgr = ComponentAccessor.getCommentManager();
    commentMgr.create(issue, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), "No manager was found for approval", false);
    log.error("No manager was found for approval");
}
