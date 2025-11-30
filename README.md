# Git Server

A GitHub-like backend service based on SpringBoot and JGit, providing comprehensive Git repository management and Git protocol support.

## Features

### Core Git Repository Management
- **Repository CRUD Operations**: Create, read, update, and delete Git repositories
- **Branch Management**: Create, list, and delete branches
- **Commit Operations**: View commit history, get commit details
- **File Operations**: Read, create, update, and delete files in repositories

### Git Protocol Support
- **HTTP Git Server**: Smart HTTP protocol support for Git operations
- **Clone Support**: Clone repositories via HTTP
- **Push/Pull Operations**: Full push and pull support via Git protocol

### User Management
- **User Registration**: Create new user accounts
- **Authentication**: HTTP Basic Authentication
- **User Profiles**: Manage user profiles and settings
- **SSH Key Management**: Add, list, and delete SSH keys for secure Git operations

### Issue Management
- **Create Issues**: Open new issues for bug reports or feature requests
- **Update Issues**: Modify issue title, description, labels, and assignees
- **Close/Reopen Issues**: Manage issue lifecycle
- **Filter Issues**: Filter issues by state (open/closed)

### Pull Request Management
- **Create Pull Requests**: Open PRs to propose changes between branches
- **Update Pull Requests**: Modify PR title, description, labels, and reviewers
- **Merge Pull Requests**: Merge changes into the base branch
- **View Diff**: Get the diff between head and base branches

### Code Review
- **Submit Reviews**: Approve, request changes, or comment on pull requests
- **Review Comments**: Add inline comments on specific lines of code
- **Dismiss Reviews**: Dismiss reviews when changes are made

### Repository Permissions
- **Collaborators**: Add, update, and remove collaborators from repositories
- **Permission Levels**: READ, WRITE, and ADMIN permission levels
- **Access Control**: Fine-grained access control for repository operations

### Repository Insights
- **Commit Statistics**: Total commits, commits in last week/month
- **Branch Statistics**: Total branches, default branch
- **Issue Statistics**: Open and closed issue counts
- **Pull Request Statistics**: Open, closed, and merged PR counts
- **Contributor Statistics**: Total unique contributors

### API Features
- **RESTful API**: Complete REST API for all operations
- **OpenAPI/Swagger Documentation**: Interactive API documentation
- **Validation**: Request validation and error handling

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **JGit 6.8.0** - Eclipse JGit for Git operations
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database operations
- **H2 Database** - Embedded database (configurable)
- **Lombok** - Reduce boilerplate code
- **SpringDoc OpenAPI** - API documentation

## Project Structure

```
src/main/java/com/gitserver/
├── GitServerApplication.java          # Main application class
├── config/                            # Configuration classes
│   ├── SecurityConfig.java            # Security configuration
│   ├── OpenApiConfig.java             # OpenAPI configuration
│   └── DataInitializer.java           # Data initialization
├── controller/                        # REST controllers
│   ├── RepositoryController.java      # Repository API
│   ├── BranchController.java          # Branch API
│   ├── CommitController.java          # Commit API
│   ├── FileController.java            # File API
│   ├── UserController.java            # User API
│   ├── IssueController.java           # Issue API
│   ├── PullRequestController.java     # Pull Request API
│   ├── ReviewController.java          # Code Review API
│   ├── SshKeyController.java          # SSH Key API
│   ├── PermissionController.java      # Repository Permission API
│   └── InsightsController.java        # Repository Insights API
├── service/                           # Business logic
│   ├── RepositoryService.java         # Repository operations
│   ├── BranchService.java             # Branch operations
│   ├── CommitService.java             # Commit operations
│   ├── FileService.java               # File operations
│   ├── UserService.java               # User operations
│   ├── IssueService.java              # Issue operations
│   ├── PullRequestService.java        # Pull Request operations
│   ├── ReviewService.java             # Code Review operations
│   ├── SshKeyService.java             # SSH Key operations
│   ├── PermissionService.java         # Permission operations
│   └── InsightsService.java           # Repository Insights
├── git/                               # JGit integration
│   ├── JGitService.java               # JGit operations
│   └── GitHttpServerConfig.java       # Git HTTP server
├── entity/                            # JPA entities
│   ├── GitRepository.java             # Repository entity
│   ├── User.java                      # User entity
│   ├── Issue.java                     # Issue entity
│   ├── PullRequest.java               # Pull Request entity
│   ├── Review.java                    # Code Review entity
│   ├── ReviewComment.java             # Review Comment entity
│   ├── SshKey.java                    # SSH Key entity
│   └── RepositoryPermission.java      # Permission entity
├── repository/                        # JPA repositories
│   ├── GitRepositoryJpaRepository.java
│   ├── UserRepository.java
│   ├── IssueRepository.java
│   ├── PullRequestRepository.java
│   ├── ReviewRepository.java
│   ├── ReviewCommentRepository.java
│   ├── SshKeyRepository.java
│   └── RepositoryPermissionRepository.java
├── dto/                               # Data transfer objects
│   ├── CreateRepositoryRequest.java
│   ├── RepositoryResponse.java
│   ├── BranchInfo.java
│   ├── CommitInfo.java
│   ├── FileInfo.java
│   ├── FileContent.java
│   └── ...
├── exception/                         # Exception handling
│   ├── GlobalExceptionHandler.java
│   ├── RepositoryNotFoundException.java
│   └── ...
└── security/                          # Security components
    └── CustomUserDetailsService.java
```

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Build and Run

```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run
```

The server will start at `http://localhost:8080`

### Default Admin User
- Username: `admin`
- Password: `admin123`

## API Documentation

Once the server is running, access the API documentation at:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

## API Endpoints

### Repository API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/repos` | Create a new repository |
| GET | `/api/repos` | Get all repositories |
| GET | `/api/repos/public` | Get public repositories |
| GET | `/api/repos/owner/{owner}` | Get repositories by owner |
| GET | `/api/repos/{owner}/{name}` | Get a repository |
| PUT | `/api/repos/{owner}/{name}` | Update a repository |
| DELETE | `/api/repos/{owner}/{name}` | Delete a repository |

### Branch API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/repos/{owner}/{repo}/branches` | Get all branches |
| GET | `/api/repos/{owner}/{repo}/branches/{branch}` | Get a branch |
| POST | `/api/repos/{owner}/{repo}/branches` | Create a branch |
| DELETE | `/api/repos/{owner}/{repo}/branches/{branch}` | Delete a branch |

### Commit API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/repos/{owner}/{repo}/commits` | Get commits |
| GET | `/api/repos/{owner}/{repo}/commits/{commitId}` | Get a commit |

### File API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/repos/{owner}/{repo}/contents` | Get root directory contents |
| GET | `/api/repos/{owner}/{repo}/contents/{path}` | Get file/directory contents |
| PUT | `/api/repos/{owner}/{repo}/contents/{path}` | Create or update a file |
| DELETE | `/api/repos/{owner}/{repo}/contents/{path}` | Delete a file |

### User API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users/register` | Register a new user |
| GET | `/api/users` | Get all users |
| GET | `/api/users/{username}` | Get a user |
| GET | `/api/users/me` | Get current user |
| PUT | `/api/users/{username}` | Update a user |
| DELETE | `/api/users/{username}` | Delete a user |

### SSH Key API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users/ssh-keys` | Add a new SSH key |
| GET | `/api/users/ssh-keys` | Get all SSH keys for current user |
| GET | `/api/users/ssh-keys/{keyId}` | Get an SSH key by ID |
| DELETE | `/api/users/ssh-keys/{keyId}` | Delete an SSH key |

### Issue API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/repos/{owner}/{repo}/issues` | Create a new issue |
| GET | `/api/repos/{owner}/{repo}/issues` | Get all issues for a repository |
| GET | `/api/repos/{owner}/{repo}/issues/{issueNumber}` | Get an issue by number |
| PATCH | `/api/repos/{owner}/{repo}/issues/{issueNumber}` | Update an issue |
| PUT | `/api/repos/{owner}/{repo}/issues/{issueNumber}/close` | Close an issue |
| PUT | `/api/repos/{owner}/{repo}/issues/{issueNumber}/reopen` | Reopen an issue |

### Pull Request API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/repos/{owner}/{repo}/pulls` | Create a new pull request |
| GET | `/api/repos/{owner}/{repo}/pulls` | Get all pull requests |
| GET | `/api/repos/{owner}/{repo}/pulls/{prNumber}` | Get a pull request |
| PATCH | `/api/repos/{owner}/{repo}/pulls/{prNumber}` | Update a pull request |
| PUT | `/api/repos/{owner}/{repo}/pulls/{prNumber}/merge` | Merge a pull request |
| PUT | `/api/repos/{owner}/{repo}/pulls/{prNumber}/close` | Close a pull request |
| GET | `/api/repos/{owner}/{repo}/pulls/{prNumber}/diff` | Get PR diff |

### Code Review API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/repos/{owner}/{repo}/pulls/{prNumber}/reviews` | Create a review |
| GET | `/api/repos/{owner}/{repo}/pulls/{prNumber}/reviews` | Get all reviews |
| GET | `/api/repos/{owner}/{repo}/pulls/{prNumber}/reviews/{reviewId}` | Get a review |
| PUT | `/api/repos/{owner}/{repo}/pulls/{prNumber}/reviews/{reviewId}/dismiss` | Dismiss a review |
| POST | `/api/repos/{owner}/{repo}/pulls/{prNumber}/reviews/{reviewId}/comments` | Add comment |
| GET | `/api/repos/{owner}/{repo}/pulls/{prNumber}/reviews/{reviewId}/comments` | Get comments |

### Collaborators API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/repos/{owner}/{repo}/collaborators` | Add a collaborator |
| GET | `/api/repos/{owner}/{repo}/collaborators` | Get all collaborators |
| GET | `/api/repos/{owner}/{repo}/collaborators/{username}` | Get a collaborator |
| PUT | `/api/repos/{owner}/{repo}/collaborators/{username}` | Update permissions |
| DELETE | `/api/repos/{owner}/{repo}/collaborators/{username}` | Remove collaborator |

### Insights API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/repos/{owner}/{repo}/insights` | Get repository insights |

## Git Protocol Support

The server supports Git Smart HTTP protocol for clone, push, and pull operations:

```bash
# Clone a repository
git clone http://localhost:8080/git/{owner}/{repo}.git

# With authentication
git clone http://username:password@localhost:8080/git/{owner}/{repo}.git
```

## Configuration

Configuration can be done via `application.properties`:

```properties
# Server port
server.port=8080

# Git repositories base path
git.repositories.base-path=./repositories

# Default branch name
git.repositories.default-branch=main

# Default admin credentials
spring.security.user.name=admin
spring.security.user.password=admin123
```

## Usage Examples

### Create a Repository

```bash
curl -X POST http://localhost:8080/api/repos \
  -H "Content-Type: application/json" \
  -u admin:admin123 \
  -d '{
    "name": "my-project",
    "description": "My new project",
    "isPrivate": false,
    "initReadme": true
  }'
```

### List Branches

```bash
curl http://localhost:8080/api/repos/admin/my-project/branches
```

### Get File Content

```bash
curl http://localhost:8080/api/repos/admin/my-project/contents/README.md
```

### Clone Repository

```bash
git clone http://admin:admin123@localhost:8080/git/admin/my-project.git
```

## License

MIT License
