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

import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.NonFreeNetSource
import org.breezyweather.common.source.WeatherSource

/**
 * QWeather (和风天气) source stub.
 *
 * The actual implementation is in the src_freenet and src_nonfreenet folders.
 * Uses API Key authentication. Free dev plan supports 24h hourly forecast with
 * precipitation data, plus 5m minutely precipitation for China.
 */
abstract class QWeatherServiceStub :
    HttpSource(),
    WeatherSource,
    ConfigurableSource,
    NonFreeNetSource {

    override val id = "qweather"
    override val name = "QWeather"
    override val continent = SourceContinent.ASIA

    protected val weatherAttribution = "和风天气"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.MINUTELY to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.AIR_QUALITY to weatherAttribution
    )
    override val attributionLinks = mapOf(
        weatherAttribution to "https://www.qweather.com/"
    )
}
