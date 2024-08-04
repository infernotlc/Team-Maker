package com.yusuf.data.repository.team

import com.yusuf.domain.model.firebase.PlayerData
import com.yusuf.domain.repository.team.TeamBalancerRepository
import com.yusuf.domain.util.RootResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class TeamBalancerRepositoryImpl @Inject constructor() : TeamBalancerRepository {
    override fun createBalancedTeams(players: List<PlayerData>): Flow<RootResult<Pair<List<PlayerData>, List<PlayerData>>>> = flow {
        emit(RootResult.Loading)

        try {
            if (players.isEmpty()) {
                throw IllegalArgumentException("Number of players must be greater than zero")
            }
            if (players.size % 2 != 0) {
                throw IllegalArgumentException("Number of players must be even")
            }
            val teamSize = players.size / 2
            val sortedPlayers = players.sortedByDescending { it.totalSkillRating }
            val teamFirst = mutableListOf<PlayerData>()
            val teamSecond = mutableListOf<PlayerData>()

            for (player in sortedPlayers) {
                // && teamFirst.size < teamSize is used to make sure two teams have the same size
                if (teamFirst.sumOf { it.totalSkillRating } < teamSecond.sumOf { it.totalSkillRating } && teamFirst.size < teamSize) {
                    teamFirst.add(player)
                } else {
                    teamSecond.add(player)
                }
            }
            emit(RootResult.Success(teamFirst to teamSecond))
        }catch (e: Exception) {
            emit(RootResult.Error(e.message ?: "Something went wrong"))
        }
        // IO dispatcher is used because this operation is CPU bound and doesn't require main thread
    }.flowOn(Dispatchers.IO)
}