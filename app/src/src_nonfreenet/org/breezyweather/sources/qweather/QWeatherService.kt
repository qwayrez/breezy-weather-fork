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

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.utils.ISO8601Utils
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.qweather.json.QWeatherAirQualityPollutant
import org.breezyweather.sources.qweather.json.QWeatherAirQualityResult
import org.breezyweather.sources.qweather.json.QWeatherDaily
import org.breezyweather.sources.qweather.json.QWeatherDailyResult
import org.breezyweather.sources.qweather.json.QWeatherHourly
import org.breezyweather.sources.qweather.json.QWeatherHourlyResult
import org.breezyweather.sources.qweather.json.QWeatherMinutelyResult
import org.breezyweather.sources.qweather.json.QWeatherNow
import org.breezyweather.sources.qweather.json.QWeatherNowResult
import org.breezyweather.sources.qweather.json.QWeatherWarning
import org.breezyweather.sources.qweather.json.QWeatherWarningColor
import org.breezyweather.sources.qweather.json.QWeatherWarningResult
import org.breezyweather.unit.distance.Distance.Companion.kilometers
import org.breezyweather.unit.pollutant.PollutantConcentration
import org.breezyweather.unit.pollutant.PollutantConcentration.Companion.microgramsPerCubicMeter
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import org.breezyweather.unit.ratio.Ratio.Companion.percent
import org.breezyweather.unit.speed.Speed.Companion.kilometersPerHour
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named

/**
 * QWeather (和风天气) implementation.
 *
 * Uses API Key authentication. Free dev plan limits:
 * - 1000 calls/day
 * - 7d daily forecast
 * - 168h hourly forecast (includes precipitation + precipitation probability)
 * - 5m minutely precipitation (China only, 2 hours coverage)
 * - Weather warnings (China only)
 */
class QWeatherService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : QWeatherServiceStub() {

    override val privacyPolicyUrl = "https://www.qweather.com/en/terms/privacy-policy.html"

    private val mApi by lazy {
        client
            .baseUrl(getApiHostOrDefault())
            .build()
            .create(QWeatherApi::class.java)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val apiKey = getApiKeyOrDefault()
        if (apiKey.isEmpty()) {
            return Observable.error(IllegalStateException("QWeather API key is not configured"))
        }

        // QWeather location is "longitude,latitude" with up to 2 decimals
        val locationParam = "${formatCoord(location.longitude)},${formatCoord(location.latitude)}"
        // Warning and air quality endpoints use /{lat}/{lon} path params (latitude first!)
        val latParam = formatCoord(location.latitude)
        val lonParam = formatCoord(location.longitude)
        val lang = getLangCode(context)
        val unit = "m"
        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()

        // QWeather requires separate endpoints for daily/hourly/now/minutely/warning/airquality.
        // We only issue requests for requested features and aggregate failures.
        val now = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getNow(apiKey, locationParam, lang, unit).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(QWeatherNowResult())
            }
        } else {
            Observable.just(QWeatherNowResult())
        }
        val daily = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getDaily("7d", apiKey, locationParam, lang, unit).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(QWeatherDailyResult())
            }
        } else {
            Observable.just(QWeatherDailyResult())
        }
        val hourly = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getHourly("168h", apiKey, locationParam, lang, unit).onErrorResumeNext {
                // Don't overwrite FORECAST failure set by daily above
                if (SourceFeature.FORECAST !in failedFeatures) {
                    failedFeatures[SourceFeature.FORECAST] = it
                }
                Observable.just(QWeatherHourlyResult())
            }
        } else {
            Observable.just(QWeatherHourlyResult())
        }
        val minutely = if (SourceFeature.MINUTELY in requestedFeatures) {
            mApi.getMinutely(apiKey, locationParam, lang).onErrorResumeNext {
                failedFeatures[SourceFeature.MINUTELY] = it
                Observable.just(QWeatherMinutelyResult())
            }
        } else {
            Observable.just(QWeatherMinutelyResult())
        }
        val warning = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getWarning(latParam, lonParam, apiKey, lang).onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(QWeatherWarningResult())
            }
        } else {
            Observable.just(QWeatherWarningResult())
        }
        val airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
            mApi.getAirQuality(latParam, lonParam, apiKey, lang).onErrorResumeNext {
                failedFeatures[SourceFeature.AIR_QUALITY] = it
                Observable.just(QWeatherAirQualityResult())
            }
        } else {
            Observable.just(QWeatherAirQualityResult())
        }

        return Observable.zip(now, daily, hourly, minutely, warning, airQuality) {
                nowResult: QWeatherNowResult,
                dailyResult: QWeatherDailyResult,
                hourlyResult: QWeatherHourlyResult,
                minutelyResult: QWeatherMinutelyResult,
                warningResult: QWeatherWarningResult,
                airQualityResult: QWeatherAirQualityResult,
            ->
            // QWeather v7 endpoints return code "200" on success; anything else means the call failed.
            // Surface as failedFeature if the response is not successful but didn't throw.
            if (nowResult.code != null && nowResult.code != "200" && SourceFeature.CURRENT in requestedFeatures) {
                failedFeatures[SourceFeature.CURRENT] = IllegalStateException("QWeather code ${nowResult.code}")
            }
            if (dailyResult.code != null && dailyResult.code != "200" && SourceFeature.FORECAST in requestedFeatures) {
                failedFeatures[SourceFeature.FORECAST] = IllegalStateException("QWeather code ${dailyResult.code}")
            }
            if (hourlyResult.code != null && hourlyResult.code != "200" && SourceFeature.FORECAST !in failedFeatures &&
                SourceFeature.FORECAST in requestedFeatures
            ) {
                failedFeatures[SourceFeature.FORECAST] = IllegalStateException("QWeather code ${hourlyResult.code}")
            }
            if (minutelyResult.code != null && minutelyResult.code != "200" &&
                SourceFeature.MINUTELY in requestedFeatures
            ) {
                failedFeatures[SourceFeature.MINUTELY] = IllegalStateException("QWeather code ${minutelyResult.code}")
            }
            // Warning and air quality endpoints (v1) don't return a "code" field.
            // They return metadata.zeroResult on success. HTTP errors are caught by onErrorResumeNext.

            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyList(dailyResult.daily, location)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyList(hourlyResult.hourly, location)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(nowResult.now)
                } else {
                    null
                },
                minutelyForecast = if (SourceFeature.MINUTELY in requestedFeatures) {
                    getMinutelyList(minutelyResult, location)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(warningResult)
                } else {
                    null
                },
                airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    getAirQuality(airQualityResult)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    private fun getCurrent(now: QWeatherNow?): CurrentWrapper? {
        if (now == null) return null
        return CurrentWrapper(
            weatherText = now.text,
            weatherCode = getWeatherCode(now.icon),
            temperature = TemperatureWrapper(
                temperature = now.temp?.toDoubleOrNull()?.celsius,
                feelsLike = now.feelsLike?.toDoubleOrNull()?.celsius
            ),
            wind = Wind(
                degree = now.wind360?.toDoubleOrNull(),
                speed = now.windSpeed?.toDoubleOrNull()?.kilometersPerHour
            ),
            relativeHumidity = now.humidity?.toDoubleOrNull()?.percent,
            pressure = now.pressure?.toDoubleOrNull()?.hectopascals,
            visibility = now.vis?.toDoubleOrNull()?.kilometers,
            cloudCover = now.cloud?.toDoubleOrNull()?.percent,
            dewPoint = now.dew?.toDoubleOrNull()?.celsius
        )
    }

    private fun getDailyList(
        dailyForecast: List<QWeatherDaily>?,
        location: Location,
    ): List<DailyWrapper> {
        if (dailyForecast.isNullOrEmpty()) return emptyList()
        return dailyForecast.map { day ->
            val date = parseQWeatherDate(day.fxDate, location.timeZone) ?: Date()
            DailyWrapper(
                date = date,
                day = HalfDayWrapper(
                    weatherText = day.textDay,
                    weatherCode = getWeatherCode(day.iconDay),
                    temperature = TemperatureWrapper(
                        temperature = day.tempMax?.toDoubleOrNull()?.celsius
                    ),
                    wind = Wind(
                        degree = day.wind360Day?.toDoubleOrNull(),
                        speed = day.windSpeedDay?.toDoubleOrNull()?.kilometersPerHour
                    )
                ),
                night = HalfDayWrapper(
                    weatherText = day.textNight,
                    weatherCode = getWeatherCode(day.iconNight),
                    temperature = TemperatureWrapper(
                        temperature = day.tempMin?.toDoubleOrNull()?.celsius
                    ),
                    wind = Wind(
                        degree = day.wind360Night?.toDoubleOrNull(),
                        speed = day.windSpeedNight?.toDoubleOrNull()?.kilometersPerHour
                    )
                ),
                uV = day.uvIndex?.toIntOrNull()?.let { UV(index = it.toDouble()) }
            )
        }
    }

    private fun getHourlyList(
        hourlyForecast: List<QWeatherHourly>?,
        location: Location,
    ): List<HourlyWrapper> {
        if (hourlyForecast.isNullOrEmpty()) return emptyList()
        return hourlyForecast.map { hour ->
            HourlyWrapper(
                date = parseIso8601(hour.fxTime, location.timeZone) ?: Date(),
                weatherText = hour.text,
                weatherCode = getWeatherCode(hour.icon),
                temperature = TemperatureWrapper(
                    temperature = hour.temp?.toDoubleOrNull()?.celsius
                ),
                // QWeather precip for hourly is the accumulated precipitation for that hour (mm).
                precipitation = Precipitation(
                    total = hour.precip?.toDoubleOrNull()?.millimeters
                ),
                precipitationProbability = hour.pop?.toDoubleOrNull()?.let {
                    PrecipitationProbability(total = it.percent)
                },
                wind = Wind(
                    degree = hour.wind360?.toDoubleOrNull(),
                    speed = hour.windSpeed?.toDoubleOrNull()?.kilometersPerHour
                ),
                relativeHumidity = hour.humidity?.toDoubleOrNull()?.percent,
                pressure = hour.pressure?.toDoubleOrNull()?.hectopascals,
                cloudCover = hour.cloud?.toDoubleOrNull()?.percent,
                dewPoint = hour.dew?.toDoubleOrNull()?.celsius
            )
        }
    }

    private fun getMinutelyList(
        result: QWeatherMinutelyResult,
        location: Location,
    ): List<Minutely> {
        val minutelyList = result.minutely
        if (minutelyList.isNullOrEmpty()) return emptyList()
        return minutelyList.map { m ->
            // fxTime is the timestamp of the item itself; items are 5 minutes apart.
            val date = parseIso8601(m.fxTime, location.timeZone) ?: Date()
            Minutely(
                date = date,
                minuteInterval = 5,
                // QWeather minutely precip is 5-minute accumulated precipitation in mm.
                // Convert to precipitation intensity (mm/h): precip_mm / (5/60) = precip_mm * 12
                precipitationIntensity = m.precip?.toDoubleOrNull()?.times(12)?.millimeters
            )
        }
    }

    private fun getAlertList(result: QWeatherWarningResult): List<Alert> {
        val alertList = result.alerts
        if (alertList.isNullOrEmpty()) return emptyList()
        return alertList
            .filter {
                // Exclude cancelled alerts; keep new/update/alert and null messageType
                val messageTypeCode = it.messageType?.code
                messageTypeCode == null ||
                    !messageTypeCode.equals("cancel", ignoreCase = true)
            }
            .map { w ->
                val severity = getAlertSeverity(w.severity, w.color?.code)
                Alert(
                    alertId = w.id ?: Objects.hash(w.headline, w.issuedTime, w.eventType?.name).toString(),
                    startDate = parseIso8601(w.onsetTime ?: w.effectiveTime, null),
                    endDate = parseIso8601(w.expireTime, null),
                    headline = w.headline,
                    description = w.description,
                    instruction = w.instruction,
                    source = w.senderName,
                    severity = severity,
                    color = getAlertColor(w.color) ?: Alert.colorFromSeverity(severity)
                )
            }
    }

    private fun getAlertSeverity(severity: String?, colorCode: String?): AlertSeverity {
        // severity field uses English terms (Minor/Moderate/Severe/Extreme)
        severity?.lowercase()?.let {
            when (it) {
                "extreme" -> return AlertSeverity.EXTREME
                "severe" -> return AlertSeverity.SEVERE
                "moderate" -> return AlertSeverity.MODERATE
                "minor" -> return AlertSeverity.MINOR
            }
        }
        // Fallback: color code (blue/yellow/orange/red) or Chinese color names
        colorCode?.lowercase()?.let {
            when {
                it.contains("red") || it.contains("红") -> return AlertSeverity.EXTREME
                it.contains("orange") || it.contains("橙") || it.contains("橘") -> return AlertSeverity.SEVERE
                it.contains("yellow") || it.contains("黄") -> return AlertSeverity.MODERATE
                it.contains("blue") || it.contains("蓝") -> return AlertSeverity.MINOR
            }
        }
        return AlertSeverity.UNKNOWN
    }

    @ColorInt
    private fun getAlertColor(color: QWeatherWarningColor?): Int? {
        if (color == null) return null
        // Prefer explicit RGBA values from the API
        if (color.red != null && color.green != null && color.blue != null) {
            val alpha = ((color.alpha ?: 1f) * 255).toInt().coerceIn(0, 255)
            return Color.argb(alpha, color.red, color.green, color.blue)
        }
        // Fallback: color code string
        val code = color.code ?: return null
        return when {
            code.contains("红", true) || code.equals("red", true) -> Color.rgb(215, 48, 42)
            code.contains("橙", true) || code.contains("橘", true) ||
                code.equals("orange", true) -> Color.rgb(249, 138, 30)
            code.contains("黄", true) || code.equals("yellow", true) -> Color.rgb(250, 237, 36)
            code.contains("蓝", true) || code.equals("blue", true) -> Color.rgb(51, 100, 255)
            else -> null
        }
    }

    private fun getAirQuality(result: QWeatherAirQualityResult): AirQualityWrapper? {
        val pollutants = result.pollutants
        if (pollutants.isNullOrEmpty()) return null
        val pollutantMap = pollutants.associateBy { it.code }
        val current = AirQuality(
            pM25 = getPollutantConcentration(pollutantMap["pm2p5"]),
            pM10 = getPollutantConcentration(pollutantMap["pm10"]),
            sO2 = getPollutantConcentration(pollutantMap["so2"]),
            nO2 = getPollutantConcentration(pollutantMap["no2"]),
            o3 = getPollutantConcentration(pollutantMap["o3"]),
            cO = getPollutantConcentration(pollutantMap["co"])
        )
        return if (current.isValid) {
            AirQualityWrapper(current = current)
        } else {
            null
        }
    }

    private fun getPollutantConcentration(pollutant: QWeatherAirQualityPollutant?): PollutantConcentration? {
        val value = pollutant?.concentration?.value ?: return null
        val unit = pollutant.concentration?.unit ?: ""
        return when {
            unit.contains("mg", ignoreCase = true) -> (value * 1000).microgramsPerCubicMeter
            else -> value.microgramsPerCubicMeter
        }
    }

    private fun getWeatherCode(icon: String?): WeatherCode? {
        if (icon.isNullOrEmpty()) return null
        val code = icon.toIntOrNull() ?: return null
        return when (code) {
            // Clear / clouds
            100, 150 -> WeatherCode.CLEAR
            101, 102, 103, 151, 152, 153 -> WeatherCode.PARTLY_CLOUDY
            104 -> WeatherCode.CLOUDY
            // Rain
            300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313,
            314, 315, 316, 317, 318, 350, 351, 399,
            -> {
                when (code) {
                    302, 303, 304 -> WeatherCode.THUNDERSTORM
                    313 -> WeatherCode.SLEET
                    else -> WeatherCode.RAIN
                }
            }
            // Snow
            400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 456, 457, 499 -> {
                when (code) {
                    404, 405, 406 -> WeatherCode.SLEET
                    else -> WeatherCode.SNOW
                }
            }
            // Fog / haze / dust
            500, 501, 509, 510, 514, 515 -> WeatherCode.FOG
            502, 511, 512, 513 -> WeatherCode.HAZE
            503, 504, 507, 508 -> WeatherCode.WIND
            // Hot / cold
            900, 901 -> WeatherCode.CLEAR
            else -> null
        }
    }

    private fun formatCoord(value: Double): String {
        // QWeather requires at most 2 decimal places
        return String.format(Locale.US, "%.2f", value)
    }

    private fun getLangCode(context: Context): String {
        // QWeather supports many languages; map Android locale to QWeather lang code
        val locale = context.currentLocale
        return when {
            locale.language.equals("zh", ignoreCase = true) -> {
                if (locale.country.equals("TW", ignoreCase = true) ||
                    locale.country.equals("HK", ignoreCase = true) ||
                    locale.country.equals("MO", ignoreCase = true)
                ) {
                    "zh-hant"
                } else {
                    "zh"
                }
            }
            locale.language.equals("en", ignoreCase = true) -> "en"
            else -> locale.language
        }
    }

    private fun parseIso8601(value: String?, timeZone: TimeZone?): Date? {
        if (value.isNullOrEmpty()) return null
        return try {
            ISO8601Utils.parse(value)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseQWeatherDate(dateStr: String?, timeZone: TimeZone): Date? {
        if (dateStr.isNullOrEmpty()) return null
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                this.timeZone = timeZone
            }.parse(dateStr)
        } catch (_: Exception) {
            null
        }
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    private var apikey: String
        set(value) {
            config.edit().putString("apikey", value).apply()
        }
        get() = config.getString("apikey", null) ?: ""
    private var apiHost: String
        set(value) {
            config.edit().putString("apiHost", value).apply()
        }
        get() = config.getString("apiHost", null) ?: ""

    private fun getApiKeyOrDefault(): String {
        return apikey.ifEmpty { BuildConfig.QWEATHER_KEY }
    }
    private fun getApiHostOrDefault(): String {
        return apiHost.ifEmpty { QWEATHER_DEFAULT_API_HOST }
    }

    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

    override val isRestricted
        get() = apikey.isEmpty()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_qweather_api_key,
                summary = { c, content ->
                    content.ifEmpty {
                        c.getString(R.string.settings_source_default_value)
                    }
                },
                content = apikey,
                onValueChanged = {
                    apikey = it
                }
            ),
            EditTextPreference(
                titleId = R.string.settings_weather_source_qweather_api_host,
                summary = { _, content ->
                    content.ifEmpty {
                        QWEATHER_DEFAULT_API_HOST
                    }
                },
                content = apiHost,
                placeholder = QWEATHER_DEFAULT_API_HOST,
                regex = Regex("^https://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}/$"),
                regexError = context.getString(R.string.settings_weather_source_qweather_api_host_invalid),
                onValueChanged = {
                    apiHost = it
                }
            )
        )
    }

    companion object {
        // Free dev plan default API host. Users with paid plans can override via API Host preference.
        private const val QWEATHER_DEFAULT_API_HOST = "https://devapi.qweather.com/"
    }
}
