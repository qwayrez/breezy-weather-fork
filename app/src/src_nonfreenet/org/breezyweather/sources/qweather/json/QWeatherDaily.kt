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
data class QWeatherDaily(
    val fxDate: String? = null,
    val sunrise: String? = null,
    val sunset: String? = null,
    val moonrise: String? = null,
    val moonset: String? = null,
    val moonPhase: String? = null,
    val moonPhaseIcon: String? = null,
    val tempMax: String? = null,
    val tempMin: String? = null,
    val iconDay: String? = null,
    val textDay: String? = null,
    val iconNight: String? = null,
    val textNight: String? = null,
    val wind360Day: String? = null,
    val windDirDay: String? = null,
    val windScaleDay: String? = null,
    val windSpeedDay: String? = null,
    val wind360Night: String? = null,
    val windDirNight: String? = null,
    val windScaleNight: String? = null,
    val windSpeedNight: String? = null,
    val humidity: String? = null,
    val precip: String? = null,
    val pressure: String? = null,
    val vis: String? = null,
    val cloud: String? = null,
    val uvIndex: String? = null,
)
