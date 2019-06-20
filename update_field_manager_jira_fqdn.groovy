import com.atlassian.jira.component.ComponentAccessor;
   
def objectFacade = ComponentAccessor.getOSGiComponentInstanceOfType(ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade"));
def objectTypeAttributeFacade = ComponentAccessor.getOSGiComponentInstanceOfType(ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeAttributeFacade"));
def objectAttributeBeanFactory = ComponentAccessor.getOSGiComponentInstanceOfType(ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.services.model.factory.ObjectAttributeBeanFactory"));
  
def objectTypeAttributeBean = objectTypeAttributeFacade.loadObjectTypeAttributeBean(3333).createMutable() //The id of the attribute
  
  /* Create the new attribute bean based on the value */
        def newObjectAttributeBean = objectAttributeBeanFactory.createObjectAttributeBeanForObject(object, objectTypeAttributeBean, "The Value");
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