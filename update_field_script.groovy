import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.JiraServiceContext;
import com.riadalabs.jira.plugins.insight.services.model.MutableObjectBean;




import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;

import java.util.ArrayList;
import java.util.stream.Collectors;

boolean SetInsightValue (def log, int InsightObjectId, int InsightAttributeId, def NewValue) {
 log.info("SetInsightValue function has been called.");
 
 /* Get Insight Object Facade from plugin accessor */
 Class objectFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade"); 
 def objectFacade = ComponentAccessor.getOSGiComponentInstanceOfType(objectFacadeClass);
 
 /* Get Insight Object Type Facade from plugin accessor */
 Class objectTypeFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeFacade"); 
 def objectTypeFacade = ComponentAccessor.getOSGiComponentInstanceOfType(objectTypeFacadeClass);
 
 /* Get Insight Object Attribute Facade from plugin accessor */
 Class objectTypeAttributeFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeAttributeFacade"); 
 def objectTypeAttributeFacade = ComponentAccessor.getOSGiComponentInstanceOfType(objectTypeAttributeFacadeClass);
 
 
 def obj = objectFacade.loadObjectBean(InsightObjectId);
 if (obj == null) {
 log.warn("Cannot find the Insight object. UsingInsightObjectId: " + InsightAttributeId + ". Exiting.");
 return false; }
 else { log.info("Insight object is: " + obj); }
 
 log.info("Object found: " + obj.getName());
 
 attribType = objectTypeAttributeFacade.loadObjectTypeAttributeBean(InsightAttributeId); 
 if (attribType == null) { 
 log.warn("Cannot find the attribute type in the object definition. Using InsightAttributeId " + InsightAttributeId + ". Exiting.");
 return false; }
 else { log.info("Attribute type for Insight object: " + attribType); }

 
 newAttrib = obj.createObjectAttributeBean(attribType);
 log.info("Created newAttrib:" + newAttrib);
 
 def newAttribValue = newAttrib.createObjectAttributeValueBean();
 log.info("Created newAttribValue");
 
 log.info("Trying to set value " + NewValue);
 log.info("Class of new value is " + NewValue.getClass());

 log.info("----");
 try {
 newAttribValue.setValue(attribType, NewValue);
 } catch (Exception vie) {
 log.warn("Unable to set attribute " + attribType + " to " + NewValue + ". Exiting.");
 log.warn(vie.getMessage());
 return false;
 }

 
 log.info("About to save value");
 def attribValues = newAttrib.getObjectAttributeValueBeans();
 attribValues.add(newAttribValue);
 newAttrib.setObjectAttributeValueBeans(attribValues);
 
 try {
 newAttrib = objectFacade.storeObjectAttributeBean(newAttrib);
 } catch (Exception vie) {
 log.warn("Unable to save changes. Exiting.");
 log.warn(vie.getMessage());
 return false;
 }
 log.info("Successfully saved value");
 log.info("===================");
 
 return true;
}



def modify_insight_attribute(int attributeNumber, value) { 
       
    def objectFacade = ComponentAccessor.getOSGiComponentInstanceOfType(ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade"));
    def objectTypeAttributeFacade = ComponentAccessor.getOSGiComponentInstanceOfType(ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeAttributeFacade"));
    def objectAttributeBeanFactory = ComponentAccessor.getOSGiComponentInstanceOfType(ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.services.model.factory.ObjectAttributeBeanFactory"));

	def objectTypeAttributeBean = objectTypeAttributeFacade.loadObjectTypeAttributeBean(attributeNumber).createMutable() //The id of the attribute
  /* Create the new attribute bean based on the value */
	 def newObjectAttributeBean = objectAttributeBeanFactory.createObjectAttributeBeanForObject(object, objectTypeAttributeBean, value);
        /* Load the attribute bean */
        def objectAttributeBean = objectFacade.loadObjectAttributeBean(object.getId(), objectTypeAttributeBean.getId());
        if (objectAttributeBean != null) {
           /* If attribute exist reuse the old id for the new attribute */
           newObjectAttributeBean.setId(objectAttributeBean.getId());
        }
/* Store the object attribute into Insight. */
    try {
        objectTypeAttributeBean = objectFacade.storeObjectAttributeBean(newObjectAttributeBean);
    } catch (Exception vie) {
        log.warn("Could not update object attribute due to validation exception:" + vie.getMessage());
    }
}

def modify_insight_reference_attribute(int attributeNumber, int value) { 
       
      def objectFacade = ComponentAccessor.getOSGiComponentInstanceOfType(ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade"));
    def objectTypeAttributeFacade = ComponentAccessor.getOSGiComponentInstanceOfType(ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeAttributeFacade"));
    def objectAttributeBeanFactory = ComponentAccessor.getOSGiComponentInstanceOfType(ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.services.model.factory.ObjectAttributeBeanFactory"));

	def objectTypeAttributeBean = objectTypeAttributeFacade.loadObjectTypeAttributeBean(attributeNumber).createMutable() //The id of the attribute
  /* Create the new attribute bean based on the value */
	 def newObjectAttributeBean = objectAttributeBeanFactory.createObjectAttributeBeanForObject(object, objectTypeAttributeBean, value);
        /* Load the attribute bean */
        def objectAttributeBean = objectFacade.loadObjectAttributeBean(object.getId(), objectTypeAttributeBean.getId());
        if (objectAttributeBean != null) {
           /* If attribute exist reuse the old id for the new attribute */
           newObjectAttributeBean.setId(objectAttributeBean.getId());
        }
/* Store the object attribute into Insight. */
    try {
        objectTypeAttributeBean = objectFacade.storeObjectAttributeBean(newObjectAttributeBean);
    } catch (Exception vie) {
        log.warn("Could not update object attribute due to validation exception:" + vie.getMessage());
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

def get_object_from_iql_query(schemaid, iql, fieldid) {
    Class iqlFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.IQLFacade"); 
	def iqlFacade = ComponentAccessor.getOSGiComponentInstanceOfType(iqlFacadeClass);
    def objects = iqlFacade.findObjectsByIQLAndSchema(schemaid, iql);
    return objects[0]
    }

final int insightSchemaId = 1;
final int insightUserAttributeIdFqdnManager = 202;
final int insightUserAttributeIdName = 195; 
final int customFieldApproversId = 10007;
final int test_value_id = 208;

def token ="admin";


modify_insight_attribute(test_value_id, "another value");
def fqdn_manager_value = get_field_from_iql_query(insightSchemaId, "objectType=\"Users\" AND \"Token\" IN (\""+token+"\")", insightUserAttributeIdFqdnManager);
log.info(fqdn_manager_value)
def manager_user_name = get_field_from_iql_query(insightSchemaId,"objectType=\"Users\" AND \"FQDN\" IN (\""+fqdn_manager_value+"\")", 204);
log.info(manager_user_name)



insight_manager = get_object_from_iql_query(insightSchemaId,"objectType=\"Users\" AND \"Token\" IN (\""+manager_user_name+"\")",204)

log.info("" + insight_manager.getId())
/*modify_insight_attribute(201,insight_manager)*/
def insight_manager_id =insight_manager.getId()
SetInsightValue(log, object.getId(),201,insight_manager_id)
/*modify_insight_reference_attribute(201,insight_manager)

/*def userSearchService = ComponentAccessor.getComponent(UserSearchService);
JiraAuthenticationContext context = ComponentAccessor.getJiraAuthenticationContext();
ApplicationUser user = context.getLoggedInUser();
JiraServiceContext jsc = new JiraServiceContextImpl(user);
ApplicationUser applicationUser =userSearchService.getUserByName(jsc,test);                                    
log.error(applicationUser);
*/
