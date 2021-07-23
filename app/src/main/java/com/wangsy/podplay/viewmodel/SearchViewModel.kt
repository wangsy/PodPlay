package com.wangsy.podplay.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.wangsy.podplay.model.Podcast
import com.wangsy.podplay.repository.ItunesRepo
import com.wangsy.podplay.service.PodcastResponse
import com.wangsy.podplay.util.DateUtils

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    var iTunesRepo: ItunesRepo? = null
    var livePodcastSummaryData: LiveData<List<PodcastSummaryViewData>>? = null

    data class PodcastSummaryViewData(
        var name: String? = "",
        var lastUpdated: String? = "",
        var imageUrl: String? = "",
        var feedUrl: String? = "")

    // 1
    suspend fun searchPodcasts(term: String): List<PodcastSummaryViewData> {
        // 2
        val results = iTunesRepo?.searchByTerm(term)

        // 3
        if (results != null && results.isSuccessful) {
            // 4
            val podcasts = results.body()?.results
            // 5
            if (!podcasts.isNullOrEmpty()) {
                // 6
                return podcasts.map { podcast ->
                    itunesPodcastToPodcastSummaryView(podcast)
                }
            }
        }
        // 7
        return emptyList()
    }

    private fun itunesPodcastToPodcastSummaryView(itunesPodcast: PodcastResponse.ItunesPodcast):
            PodcastSummaryViewData {
        return PodcastSummaryViewData(
            itunesPodcast.collectionCensoredName,
            DateUtils.jsonDateToShortDate(itunesPodcast.releaseDate),
            itunesPodcast.artworkUrl30,
            itunesPodcast.feedUrl)
    }
}
