# Coding Guidelines

## Project Overview
This is a template workspace for AI coding agent instructions. When a real codebase is added, update these instructions to reflect the specific project's architecture, patterns, and workflows.

## Code Quality Standards

### Naming Conventions
- Use descriptive, consistent names for variables, methods, classes, and files
- Prefer `calculateTotalPrice()` over `calc()` or `getPrice()`
- Avoid abbreviations unless widely accepted (e.g., `url`, `id`, `api`)
- Use consistent casing: `camelCase` for variables/functions, `PascalCase` for classes

### Code Structure
- Each function should have a single responsibility
- Break complex logic into smaller, well-named functions
- Prefer composition over deep inheritance
- Keep functions under 20-30 lines when possible

### Comments and Documentation
- Write comments to explain *why*, not *what*
- Document complex business logic and non-obvious decisions
- Remove outdated or misleading comments during refactoring
- Use inline comments sparingly - prefer self-documenting code

### Error Handling
- Use structured error handling appropriate to the language/framework
- Always handle potential failure cases (network requests, file operations, parsing)
- Log errors with sufficient context for debugging
- Avoid silent failures - make errors visible

## Development Workflow

### Testing
- Write tests for critical business logic and edge cases
- Keep tests focused, fast, and deterministic
- Use descriptive test names that explain the scenario
- Follow the AAA pattern: Arrange, Act, Assert

### Version Control
- Commit logical, atomic changes with clear messages
- Use conventional commit format when applicable
- Never commit secrets, API keys, or sensitive data
- Review changes before committing

## Project-Specific Guidelines

*Update this section when working with a specific codebase:*

### Architecture Patterns
- Document the main architectural approach (MVC, microservices, etc.)
- Explain service boundaries and communication patterns
- Note any domain-specific design patterns in use

### Build and Development
- List essential commands for building, testing, and running locally
- Document environment setup requirements
- Note any non-standard tooling or workflows

### Dependencies and Integration
- Highlight key external dependencies and their purposes
- Document API integration patterns and conventions
- Note any database schema or data flow considerations

---

*Note: This template should be customized based on the actual codebase patterns, conventions, and requirements.*