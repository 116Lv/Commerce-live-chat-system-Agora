# GitHub Project Status Automation Design

## Goal

Move GitHub Project issue cards automatically when pull request activity shows that work has entered review, returned to implementation, or finished.

The project is the user project at `https://github.com/users/116Lv/projects/2`.

## Linking Rule

Pull requests identify their related issue by including a closing keyword in the PR body:

```md
Closed #12
```

The automation treats the keyword case-insensitively and supports GitHub closing keywords such as `closed`, `closes`, `fixes`, and `resolves`, but the team convention is `Closed #<issue-number>`.

## Workflow Events

Create a new GitHub Actions workflow at `.github/workflows/project-status.yml`.

The workflow listens to:

- `pull_request`: `opened`, `edited`, `reopened`, `ready_for_review`, `converted_to_draft`, `closed`
- `pull_request_review`: `submitted`

## Status Transitions

The workflow parses the PR body, finds referenced issues, finds those issues in Project 2, and updates the `Status` field.

| Event | Project Status |
| --- | --- |
| PR opened, edited, reopened, ready for review | `In Review` |
| PR converted to draft | `In Progress` |
| Review submitted with changes requested | `In Progress` |
| Review approved | `In Review` |
| PR merged | `Done` |

If a PR is closed without merge, the workflow leaves the issue status unchanged.

## Authentication

The workflow uses `secrets.PROJECT_TOKEN`, not the default `GITHUB_TOKEN`, because this is a user project and project access is outside the repository-scoped token.

The token must be stored as a repository Actions secret named `PROJECT_TOKEN`.

## Error Handling

If the PR body does not include a matching `Closed #<number>` reference, the workflow exits without changing project data.

If an issue is not already present in Project 2, the workflow adds it before setting the status.

If the configured Status option is missing from the project, the workflow fails loudly so the configuration problem is visible in Actions.

## Verification

Verification should include:

- YAML syntax and repository status inspection.
- A dry run where possible by checking generated script logic.
- Manual GitHub verification after `PROJECT_TOKEN` is added: create a test issue, open a PR containing `Closed #<issue-number>`, and confirm the project card moves to `In Review`.
