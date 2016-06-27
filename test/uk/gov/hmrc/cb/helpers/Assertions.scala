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

package uk.gov.hmrc.cb.helpers

import play.api.mvc.Result

/**
 * Created by adamconder on 27/06/2016.
 */
trait Assertions {
  self: uk.gov.hmrc.play.test.UnitSpec =>

  def verifyLocation(response : Result, endpoint: String) = {
    val location = response.header.headers.get("Location").get
    location.splitAt(12)._2 should include(endpoint)
  }

}
