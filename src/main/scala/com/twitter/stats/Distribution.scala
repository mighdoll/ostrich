/*
 * Copyright 2009 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twitter.stats

import scala.collection.Map
import scala.collection.immutable
import scala.util.Sorting
import com.twitter.json.{Json, JsonSerializable}

/**
 * A set of data points summarized into a histogram, mean, min, and max.
 * Distributions are immutable.
 */
case class Distribution(count: Int, maximum: Int, minimum: Int, histogram: Option[Histogram],
                        mean: Double)
extends JsonSerializable {
  def average = mean

  def this(count: Int, maximum: Int, minimum: Int, mean: Double) =
    this(count, maximum, minimum, None, mean)

  def toJson() = {
    val out: Map[String, Any] = toMap ++ (histogram match {
      case None => immutable.Map.empty[String, Any]
      case Some(h) => immutable.Map[String, Any]("histogram" -> h.get(false))
    })
    Json.build(out).toString
  }

  override def equals(other: Any) = other match {
    case t: Distribution =>
      t.count == count && t.maximum == maximum && t.minimum == minimum && t.mean == mean
    case _ => false
  }

  override def toString = {
    val out = toMap
    out.keys.toSeq.sorted.map { key => "%s=%d".format(key, out(key)) }.mkString("(", ", ", ")")
  }

  def toMapWithoutHistogram = {
    Map[String, Long]("count" -> count, "maximum" -> maximum, "minimum" -> minimum,
                      "average" -> average.toLong)
  }

  def toMap: Map[String, Long] = {
    toMapWithoutHistogram ++ (histogram match {
      case None => Map.empty[String, Long]
      case Some(h) => Map[String, Long]("p25" -> h.getPercentile(0.25),
                                        "p50" -> h.getPercentile(0.5),
                                        "p75" -> h.getPercentile(0.75),
                                        "p90" -> h.getPercentile(0.9),
                                        "p99" -> h.getPercentile(0.99),
                                        "p999" -> h.getPercentile(0.999),
                                        "p9999" -> h.getPercentile(0.9999))
    })
  }
}
