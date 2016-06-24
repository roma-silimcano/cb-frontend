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

import play.api.libs.json.Json
import uk.gov.hmrc.cb.models.payload.submission.child.Child
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

/**
 * Created by adamconder on 31/05/2016.
 */
class GendersSpec extends UnitSpec with WithFakeApplication {

  "Genders" should {

    "convert to JSON" in {
      val gender = Genders.Male
      Json.toJson(gender) shouldBe Json.parse(
        """
          |"male"
        """.stripMargin)
    }

  }

}
