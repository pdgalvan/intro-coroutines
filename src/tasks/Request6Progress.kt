package tasks

import contributors.*

suspend fun loadContributorsProgress(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    var userList = emptyList<User>()
    repos.mapIndexed { index, repo ->
        val users =service
            .getRepoContributors(req.org, repo.name)
            .also { logUsers(repo, it) }
            .bodyList()

        userList = (userList + users).aggregate()
        updateResults(userList, index == repos.lastIndex)
    }
}
