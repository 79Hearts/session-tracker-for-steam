package com.steamtimeline.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

// ---------- response models ----------

data class PlayerSummariesResponse(
    val response: PlayerSummariesData
)

data class PlayerSummariesData(
    val players: List<PlayerSummary>
)

data class PlayerSummary(
    val steamid: String,
    val personaname: String,
    val gameid: String?,
    val gameextrainfo: String?,
    @SerializedName("avatar") val avatar: String
)

data class RecentlyPlayedResponse(
    val response: RecentlyPlayedData
)

data class RecentlyPlayedData(
    @SerializedName("total_count") val totalCount: Int,
    val games: List<RecentGame>?
)

data class RecentGame(
    val appid: Long,
    val name: String,
    @SerializedName("playtime_2weeks") val playtime2Weeks: Int,
    @SerializedName("playtime_forever") val playtimeForever: Int
)

// ---------- service interface ----------

interface SteamApiService {

    @GET("ISteamUser/GetPlayerSummaries/v2/")
    suspend fun getPlayerSummaries(
        @Query("key") apiKey: String,
        @Query("steamids") steamId: String
    ): PlayerSummariesResponse

    @GET("IPlayerService/GetRecentlyPlayedGames/v1/")
    suspend fun getRecentlyPlayedGames(
        @Query("key") apiKey: String,
        @Query("steamid") steamId: String,
        @Query("count") count: Int = 10
    ): RecentlyPlayedResponse

    @GET("ISteamUser/ResolveVanityURL/v1/")
    suspend fun resolveVanityUrl(
        @Query("key") apiKey: String,
        @Query("vanityurl") vanityUrl: String
    ): ResolveVanityResponse
}

data class ResolveVanityResponse(
    val response: ResolveVanityData
)

data class ResolveVanityData(
    val steamid: String?,
    val success: Int,
    val message: String?
)
