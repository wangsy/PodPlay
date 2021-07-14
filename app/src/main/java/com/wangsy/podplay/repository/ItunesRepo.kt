package com.wangsy.podplay.repository

import com.wangsy.podplay.service.ItunesService

// 1
class ItunesRepo(private val itunesService: ItunesService) {
    // 2
    suspend fun searchByTerm(term: String) = itunesService.searchPodcastByTerm(term) // 3
}
