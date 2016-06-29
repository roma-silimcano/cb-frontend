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

package uk.gov.hmrc.cb.models.payload.submission

import play.api.libs.json.Json
import uk.gov.hmrc.cb.models.payload.submission.child.Child
import uk.gov.hmrc.cb.models.payload.submission.claimant.Claimant

/**
  * Created by chrisianson on 24/06/16.
  */
case class Payload(children: List[Child] = List(),
                   claimant: Option[Claimant] = None)

object Payload {
  implicit val formats = Json.format[Payload]
}
