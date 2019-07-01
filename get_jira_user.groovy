

test = "c154213"
def userSearchService = ComponentAccessor.getComponent(UserSearchService);
JiraAuthenticationContext context = ComponentAccessor.getJiraAuthenticationContext();
ApplicationUser user = context.getLoggedInUser();
JiraServiceContext jsc = new JiraServiceContextImpl(user);
ApplicationUser applicationUser =userSearchService.getUserByName(jsc,test);                                    
log.error(applicationUser);
