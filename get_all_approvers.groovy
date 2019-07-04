import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.comments.CommentManager;

// declaring a variable without type definition makes it Global, so we can access it even within functions 
insightSchemaId = 1;

final int insight_custom_users_field_in_approvers_list = 254;
final int insightUserAttributeIdFqdnManager = 202;
final int insightUserAttributeIdName = 195; 
final int customFieldApproversId = 10007;

def userSearchService = ComponentAccessor.getComponent(UserSearchService)
def fqdnManager = "";
def userName = "";


def ApplicationUser applicationUser = null;
List<ApplicationUser> approvers = new ArrayList<ApplicationUser>();

//This function retrieves all insights objects using an iql query
def get_objects_property_from_iql_query(iql, property_field_id) {
    Class iqlFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.IQLFacade"); 
	def iqlFacade = ComponentAccessor.getOSGiComponentInstanceOfType(iqlFacadeClass);
    def objects = iqlFacade.findObjectsByIQLAndSchema(insightSchemaId, iql);
    def property_list = new ArrayList();
    for (object in objects) {
        log.info("" + object)
        for(objectAttributeBean in object.getObjectAttributeBeans()){
            if(objectAttributeBean.getObjectTypeAttributeId() == property_field_id){
                
                
            //try{
                	for (value in objectAttributeBean.getObjectAttributeValueBeans())
                		{
                   
                	log.info(""+value.getValue())
                    property_list.add(value.getValue()); }
              //  }catch(Exception ex){}
            }
		} 
    }

	return property_list
    }
def get_field_from_iql_query(iql, fieldid) {
    Class iqlFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.IQLFacade"); 
	def iqlFacade = ComponentAccessor.getOSGiComponentInstanceOfType(iqlFacadeClass);
    def objects = iqlFacade.findObjectsByIQLAndSchema(insightSchemaId, iql);

	for(objectAttributeBean in objects[0].getObjectAttributeBeans()){
        if(objectAttributeBean.getObjectTypeAttributeId() == fieldid){
        try{
                value = objectAttributeBean.getObjectAttributeValueBeans()[0].getValue();
            	return value;
            }catch(Exception ex){}
        }
	}
    }


def reporter = ComponentAccessor.getUserManager().getUserByKey(issue.reporterId);
def display_name = reporter.getDisplayName() //only retieved used for ogging debugging purposes
def LoginName = reporter.getUsername()


def list_of_insight_approver_objects = get_objects_property_from_iql_query("objectType = \"Approver Lists\" and Name = \"Senior Management Approvers\"",insight_custom_users_field_in_approvers_list)
log.info("list of Manager : " + list_of_insight_approver_objects)


def approver_user_name = ""
for(u in list_of_insight_approver_objects){
    approver_user_name = get_field_from_iql_query("objectType=\"Users\" AND \"Login Name\" = (\""+u+"\")",insightUserAttributeIdName)
	log.info("Approver User Name : " + approver_user_name)
    applicationUser = userSearchService.findUsersByFullName(approver_user_name)[0];
    approvers.add(applicationUser);
if(!approvers.contains(applicationUser)){
	approvers.add(applicationUser)
}
}


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


// maybe now we need to add the user's manager as possible approver ?
// Get reporter ApplicationUser (The current user of the app making the request)

try{
log.info("" + insightUserAttributeIdFqdnManager)

def fqdn_of_Manager = get_field_from_iql_query(insightSchemaId,"objectType=\"Users\" AND \"Login Name\" IN (\""+LoginName+"\") ",insightUserAttributeIdFqdnManager)
log.info("FQDN of Manager : " + fqdn_of_Manager)
def manager_user_name = get_field_from_iql_query(insightSchemaId,"objectType=\"Users\" AND \"FQDN\" IN (\""+fqdn_of_Manager+"\")",insightUserAttributeIdName)
log.info("Manager User Name : " + manager_user_name)

applicationUser = userSearchService.findUsersByFullName(manager_user_name)[0];
approvers.add(applicationUser);
}catch(Exception ex){
    CommentManager commentMgr = ComponentAccessor.getCommentManager();
    commentMgr.create(issue, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), "No manager was found for approval", false);
    log.error("No manager was found for approval");
}



def cfApprovers = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(customFieldApproversId);

issue.setCustomFieldValue(cfApprovers, approvers);
Object cfVal = issue.getCustomFieldValue(cfApprovers);

IssueChangeHolder changeHolder = new DefaultIssueChangeHolder();
cfApprovers.updateValue(null, issue, new ModifiedValue("", approvers), changeHolder);

issue.store();
