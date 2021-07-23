package com.wangsy.podplay.repository

import androidx.lifecycle.LiveData
import com.wangsy.podplay.db.PodcastDao
import com.wangsy.podplay.model.Episode
import com.wangsy.podplay.model.Podcast
import com.wangsy.podplay.service.FeedService
import com.wangsy.podplay.service.RssFeedResponse
import com.wangsy.podplay.service.RssFeedService
import com.wangsy.podplay.util.DateUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PodcastRepo(private var feedService: RssFeedService, private var podcastDao: PodcastDao) {
    suspend fun getPodcast(feedUrl: String): Podcast? {
        val podcastLocal = podcastDao.loadPodcast(feedUrl)
        if (podcastLocal != null) {
            podcastLocal.id?.let {
                podcastLocal.episodes = podcastDao.loadEpisodes(it)
                return podcastLocal
            }
        }

        var podcast: Podcast? = null
        val feedResponse = feedService.getFeed(feedUrl)
        if (feedResponse != null) {
            podcast = rssResponseToPodcast(feedUrl, "", feedResponse)
        }
        return podcast
    }

    fun save(podcast: Podcast) {
        GlobalScope.launch {
            // 1
            val podcastId = podcastDao.insertPodcast(podcast)
            // 2
            for (episode in podcast.episodes) {
                // 3
                episode.podcastId = podcastId
                podcastDao.insertEpisode(episode)
            }
        }
    }

    fun getAll(): LiveData<List<Podcast>> {
        return podcastDao.loadPodcasts()
    }

    fun delete(podcast: Podcast) {
        GlobalScope.launch {
            podcastDao.deletePodcast(podcast)
        }
    }

    private fun rssResponseToPodcast(feedUrl: String, imageUrl: String, rssResponse: RssFeedResponse): Podcast? {
        // 1
        val items = rssResponse.episodes ?: return null
        // 2
        val description = if (rssResponse.description == "")
            rssResponse.summary else rssResponse.description
        // 3
        return Podcast(null, feedUrl, rssResponse.title, description, imageUrl,
            rssResponse.lastUpdated, episodes = rssItemsToEpisodes(items))
    }

    private fun rssItemsToEpisodes(episodeResponses: List<RssFeedResponse.EpisodeResponse>): List<Episode> {
        return episodeResponses.map {
            Episode(
                it.guid ?: "",
                null,
                it.title ?: "",
                it.description ?: "",
                it.url ?: "",
                it.type ?: "",
                DateUtils.xmlDateToDate(it.pubDate),
                it.duration ?: ""
            )
        }
    }

}
