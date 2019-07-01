
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.JiraServiceContext;

test = "c154213"
def userSearchService = ComponentAccessor.getComponent(UserSearchService);
JiraAuthenticationContext context = ComponentAccessor.getJiraAuthenticationContext();
ApplicationUser user = context.getLoggedInUser();
JiraServiceContext jsc = new JiraServiceContextImpl(user);
ApplicationUser applicationUser =userSearchService.getUserByName(jsc,test);                                    
log.error(applicationUser);
