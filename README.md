# Time-Management-Web-App
Java | Spring Boot | Auth0 | Thymeleaf | DynamoDB | CSS <br /><br />
Project-time management system utilizing an MVC design with the primary function to help clients
get more accurate data about how long it takes for them to finish projects of certain sizes.
Useful for SCRUM teams to keep track of sprint progress and useful for schools to
utilize data to allocate or deallocate more time to students for assignments/projects.
Application enables the user to:

- [x] Sign Up/sign in
- [x] Create Projects that consists of To Do list items with each project and list item utilizing CRUD operations
- [x] Log the amount of time spent on each To Do item with total project time logged
- [x] Mark tasks as complete, updating the projects progress status

Additional Features to be implemented:
- [x] Auth0 for secure authentication and google sign in
- [x] Rich Text Editor for notes
- [x] Email service for email link verification for registration and reset username/password function
- [ ] Project collaboration with other users

## Product Demo:

### Login
![Login](src/main/resources/static/images/auth0Login.png)

### Projects  - click on project name to get to project specific view!
![Projects screen](src/main/resources/static/images/projects.png)

### Create a Project
![Create a project screen](src/main/resources/static/images/createProject.png)

### Project specific view - click on taskRecord name to get to taskRecord view!
![Project view](src/main/resources/static/images/projectView.png)

### Task view
![Task view](src/main/resources/static/images/taskStartedView.png)

### Task Started View
![Task started view](src/main/resources/static/images/taskStartedView.png)

### Edit taskRecord name
![Edit taskRecord name](src/main/resources/static/images/editTaskName.png)

### Create a new taskRecord
![Create a taskRecord screen](src/main/resources/static/images/createTask.png)

### Marked a taskRecord as completed
![Project view with taskRecord marked as complete](src/main/resources/static/images/taskMarkedComplete.png)
