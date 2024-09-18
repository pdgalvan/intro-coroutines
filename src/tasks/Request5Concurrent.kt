package tasks

import contributors.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun loadContributorsConcurrent(
    service: GitHubService,
    req: RequestData
): List<User> = coroutineScope {

    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    val users: List<Deferred<List<User>>> = repos.map { repo ->
            async {
                service
                    .getRepoContributors(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
            }
        }


    users.awaitAll().flatten().aggregate()

}