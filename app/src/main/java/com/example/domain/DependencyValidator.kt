package com.example.domain

import com.example.data.MissionEntity

/**
 * Deterministic Directed Acyclic Graph (DAG) validator for Mission dependencies.
 * Guarantees that adding `candidateDependencyId` as a prerequisite of `missionId`
 * will never introduce a self-dependency or circular dependency chain.
 */
object DependencyValidator {

    fun wouldCreateCircularDependency(
        missionId: Long,
        candidateDependencyId: Long,
        allMissions: List<MissionEntity>
    ): Boolean {
        if (missionId == candidateDependencyId) return true

        val adjacency = allMissions.associate { it.id to it.parsedDependencies().toMutableSet() }.toMutableMap()
        val currentDeps = adjacency.getOrPut(missionId) { mutableSetOf() }
        currentDeps.add(candidateDependencyId)

        // Check if `missionId` is reachable starting from `candidateDependencyId`
        val visited = mutableSetOf<Long>()
        val stack = ArrayDeque<Long>()
        stack.add(candidateDependencyId)

        while (stack.isNotEmpty()) {
            val node = stack.removeLast()
            if (node == missionId) return true
            if (visited.add(node)) {
                val nextNodes = adjacency[node] ?: emptySet()
                for (next in nextNodes) {
                    stack.add(next)
                }
            }
        }
        return false
    }
}
