/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.cb.mappings

import play.api.libs.json.{Format, Reads, Writes}
import uk.gov.hmrc.cb.utils.EnumUtils

/**
  * Created by chrisianson on 27/05/16.
  */
object Genders extends Enumeration {

  type Gender = Value

  val Male = Value(0, "male")
  val Female= Value(1, "female")
  val Indeterminate = Value(2, "indeterminate")
  val None = Value(3, "none")

  val enumReads: Reads[Gender] = EnumUtils.enumReads(Genders)

  val enumWrites: Writes[Gender] = EnumUtils.enumWrites

  implicit def enumFormats : Format[Gender] = EnumUtils.enumFormat(Genders)
}
