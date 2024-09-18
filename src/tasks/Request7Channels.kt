package tasks

import contributors.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

suspend fun loadContributorsChannels(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) = coroutineScope {
    val channel = Channel<List<User>>()
    val userList = mutableListOf<User>()
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    repos.forEach {  repo ->
        launch {
            service
                .getRepoContributors(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
                .also { users: List<User> -> channel.send(users) }
        }

    }
    repeat(repos.size) {
        val users = channel.receive()
        userList += users
        updateResults(userList.aggregate(), it == repos.lastIndex)
    }

}

