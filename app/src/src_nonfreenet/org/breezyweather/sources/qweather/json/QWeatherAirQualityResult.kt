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

package org.breezyweather.sources.qweather.json

import kotlinx.serialization.Serializable

@Serializable
data class QWeatherAirQualityResult(
    val metadata: QWeatherAirQualityMetadata? = null,
    val indexes: List<QWeatherAirQualityIndex>? = null,
    val pollutants: List<QWeatherAirQualityPollutant>? = null,
)

@Serializable
data class QWeatherAirQualityMetadata(
    val tag: String? = null,
)

@Serializable
data class QWeatherAirQualityIndex(
    val code: String? = null,
    val name: String? = null,
    val aqi: Double? = null,
    val aqiDisplay: String? = null,
    val level: String? = null,
    val category: String? = null,
    val primaryPollutant: QWeatherAirQualityPollutantRef? = null,
)

@Serializable
data class QWeatherAirQualityPollutantRef(
    val code: String? = null,
    val name: String? = null,
    val fullName: String? = null,
)

@Serializable
data class QWeatherAirQualityPollutant(
    val code: String? = null,
    val name: String? = null,
    val fullName: String? = null,
    val concentration: QWeatherAirQualityConcentration? = null,
)

@Serializable
data class QWeatherAirQualityConcentration(
    val value: Double? = null,
    val unit: String? = null,
)
