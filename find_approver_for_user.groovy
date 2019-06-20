/*
n=1 from insight DestinguishedName

Last Update: 15/11/2017
Matthew Van Kuyk
*/

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.comments.CommentManager;
 
final int insightSchemaId = 8;
final int insightUserAttributeIdFqdnManager = 10612;
final int insightUserAttributeIdName = 9681; 
final int customFieldApproversId = 187;

def userSearchService = ComponentAccessor.getComponent(UserSearchService)
def fqdnManager = "";
def userName = "";

ApplicationUser applicationUser = null;

List<ApplicationUser> approvers = new ArrayList<ApplicationUser>();

/* Get Insight IQL Facade from plugin accessor */
Class iqlFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.IQLFacade"); 
def iqlFacade = ComponentAccessor.getOSGiComponentInstanceOfType(iqlFacadeClass);
try{
/* Get reporter ApplicationUser (The current user of the app making the request) */
def reporter = ComponentAccessor.getUserManager().getUserByKey(issue.reporterId);
def display_name = reporter.getDisplayName()




/* Specify the schema id as well as the IQL that will fetch objects. In this case all objects with Name matching the valueCF, be sure to include " around value */ 
def objects = iqlFacade.findObjectsByIQLAndSchema(insightSchemaId, "objectType=\"User\" AND \"Last Name\" IN (\""+display_name+"\") AND \"First Name\" IN (\""+display_name+"\")");

/* Look for the FQDN of the n+1 in Insight */
for(objectAttributeBean in objects[0].getObjectAttributeBeans()){
    if(objectAttributeBean.getObjectTypeAttributeId() == insightUserAttributeIdFqdnManager){
    try{
        	fqdnManager = objectAttributeBean.getObjectAttributeValueBeans()[0].getValue();
        }catch(Exception ex){}
    }
}

// The function over this one puts into a value object, a list of objects that could match the query. I added that we check also the first name in jira fields to reduce risk.
// In normal cases, if there are no user duplicates, this function should only return one element.
// but since it is still possible that we have 2 accounts with the same display
/*Get the applicationUser of the n+1*/


objects = iqlFacade.findObjectsByIQLAndSchema(insightSchemaId, "objectType=\"User\" AND \"distinguishedName\" IN (\""+fqdnManager+"\")");
for(objectAttributeBean in objects[0].getObjectAttributeBeans()){
    if(objectAttributeBean.getObjectTypeAttributeId() == insightUserAttributeIdName){
    try{
        userName = objectAttributeBean.getObjectAttributeValueBeans()[0].getValue();
        applicationUser = userSearchService.findUsersByFullName(userName)[0];
        if(applicationUser){
            log.error("@@@@@ "+applicationUser);
        }
        
        }catch(Exception ex){log.error(ex.toString());}
    }
}

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
    commentMgr.create(issue, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), "No manager was found for approval", false)
}
