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
data class QWeatherWarning(
    val id: String? = null,
    val senderName: String? = null,
    val issuedTime: String? = null,
    val messageType: QWeatherWarningMessageType? = null,
    val eventType: QWeatherWarningEventType? = null,
    val urgency: String? = null,
    val severity: String? = null,
    val certainty: String? = null,
    val icon: String? = null,
    val color: QWeatherWarningColor? = null,
    val effectiveTime: String? = null,
    val onsetTime: String? = null,
    val expireTime: String? = null,
    val headline: String? = null,
    val description: String? = null,
    val criteria: String? = null,
    val instruction: String? = null,
    val responseTypes: List<String>? = null,
)

@Serializable
data class QWeatherWarningMessageType(
    val code: String? = null,
    val supersedes: List<String>? = null,
)

@Serializable
data class QWeatherWarningEventType(
    val name: String? = null,
    val code: String? = null,
)

@Serializable
data class QWeatherWarningColor(
    val code: String? = null,
    val red: Int? = null,
    val green: Int? = null,
    val blue: Int? = null,
    val alpha: Float? = null,
)
