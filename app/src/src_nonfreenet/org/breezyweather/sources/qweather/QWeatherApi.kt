/*
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.sources.qweather

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.qweather.json.QWeatherDailyResult
import org.breezyweather.sources.qweather.json.QWeatherHourlyResult
import org.breezyweather.sources.qweather.json.QWeatherMinutelyResult
import org.breezyweather.sources.qweather.json.QWeatherNowResult
import org.breezyweather.sources.qweather.json.QWeatherWarningResult
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * QWeather (和风天气) API.
 *
 * Uses API Key authentication via the `key` query parameter.
 * The free dev plan limits: 1000 calls/day, 24h hourly forecast, 7d daily forecast,
 * 5m minutely precipitation (China only), and weather warnings.
 *
 * Default base URL is https://devapi.qweather.com/, but users can configure
 * a custom API Host (e.g. https://abcdefgh.qweatherapi.com/).
 */
interface QWeatherApi {

    @GET("v7/weather/now")
    fun getNow(
        @Query("key") apiKey: String,
        @Query("location") location: String,
        @Query("lang") lang: String,
        @Query("unit") unit: String,
    ): Observable<QWeatherNowResult>

    @GET("v7/weather/{days}")
    fun getDaily(
        @Path("days") days: String,
        @Query("key") apiKey: String,
        @Query("location") location: String,
        @Query("lang") lang: String,
        @Query("unit") unit: String,
    ): Observable<QWeatherDailyResult>

    @GET("v7/weather/{hours}")
    fun getHourly(
        @Path("hours") hours: String,
        @Query("key") apiKey: String,
        @Query("location") location: String,
        @Query("lang") lang: String,
        @Query("unit") unit: String,
    ): Observable<QWeatherHourlyResult>

    @GET("v7/minutely/5m")
    fun getMinutely(
        @Query("key") apiKey: String,
        @Query("location") location: String,
        @Query("lang") lang: String,
    ): Observable<QWeatherMinutelyResult>

    @GET("v7/warning/now")
    fun getWarning(
        @Query("key") apiKey: String,
        @Query("location") location: String,
        @Query("lang") lang: String,
    ): Observable<QWeatherWarningResult>
}
