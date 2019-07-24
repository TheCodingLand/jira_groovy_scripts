import com.atlassian.jira.component.ComponentAccessor

//GLOBALS :
insightSchemaId = 1
groups_field_id = 439
groupManager = ComponentAccessor.getGroupManager()

// FUNCTION DEFINITIONS :

def get_objects_from_iql_query(iql) {
    Class iqlFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.IQLFacade"); 
	def iqlFacade = ComponentAccessor.getOSGiComponentInstanceOfType(iqlFacadeClass);
    def objects = iqlFacade.findObjectsByIQLAndSchema(insightSchemaId, iql);
    return objects
    }

def get_field_values(object,fieldid) {
    def values = []
    for(objectAttributeBean in object.getObjectAttributeBeans()){
        
        if(objectAttributeBean.getObjectTypeAttributeId() == fieldid){
            for (bean in objectAttributeBean.getObjectAttributeValueBeans()){
                def value = bean.getValue()
                values.push(value)
            }        
        }
	}
    return values
}

boolean SetInsightValue (def log, int InsightObjectId, int InsightAttributeId, def NewValue) {
    //Modified version of the setInsightValue function to handle also array types
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
        
     if (NewValue.getClass() ==  java.util.ArrayList) {
        //def newObjectAttributeValueBean = newAttrib.createObjectAttributeValueBean(); 
        def values = newAttrib.getObjectAttributeValueBeans();
         
        //def attrValues = newAttrib.getObjectAttributeValueBeans(); 
         for (value in NewValue) {
            def newObjectAttributeValueBean = newAttrib.createObjectAttributeValueBean(); 
            //def newObjectAttributeValueBean = newObjectAttributeBean.createObjectAttributeValueBean(); 
            newObjectAttributeValueBean.setValue(attribType,value); //add an asset to the attribute
            values.add(newObjectAttributeValueBean);
         }
            newAttrib.setObjectAttributeValueBeans(values);
            newAttrib = objectFacade.storeObjectAttributeBean(newAttrib);
     }
     else{
     
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
      }
     return true;
}





//SCRIPT START :

//Get the Approvers list : in insight
def approvers_lists = get_objects_from_iql_query("objectType=\"Approver Lists\"")

//For each one
for (approver_list in approvers_lists){
    
    def approvers=[] //approvers array is a list of user names
    
    def group_field_value=null
    
    //GET the Group field value (list of users)
    groups_field_value = get_field_values(approver_list, groups_field_id)
	//Then for each group
    for (group in groups_field_value) {
        log.info(group)
        //get it's user list
        def users = groupManager.getUsersInGroup(group)
        
        for (user in users) {
            username = user.name
            log.info(""+username)
            approvers.push(username)
        }
    }
    //we need the approver_list_id so we can update it with out set value function
    
    def approver_list_id = approver_list.getId()
    SetInsightValue (log, approver_list_id, 254, approvers)
    }
log.info("completed update of approvers list !")
