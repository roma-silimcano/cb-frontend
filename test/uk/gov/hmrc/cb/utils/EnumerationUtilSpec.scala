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

package uk.gov.hmrc.cb.utils

import play.api.data.validation.ValidationError
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.cb.mappings.Genders
import uk.gov.hmrc.cb.mappings.Genders.Gender
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}

/**
 * Created by adamconder on 27/05/2016.
 */

class EnumerationUtilSpec extends UnitSpec with WithFakeApplication {

  "EnumerationUtil" should {

    "return a JsError when it cannot parse json object" in {
      val json = Json.parse(
        """
          |{
          | "enum" : "something"
          |}
        """.stripMargin)

      json.validate[Gender] shouldBe JsError(ValidationError("String value expected"))
    }

    "return a JsSuccess when it can parse json object Male" in {
      val json = Json.parse(
        """
          |"male"
        """.stripMargin)

      json.validate[Gender] shouldBe JsSuccess(Genders.Male)
    }

    "return a JsSuccess when it can parse json object Female" in {
      val json = Json.parse(
        """
          |"female"
        """.stripMargin)

      json.validate[Gender] shouldBe JsSuccess(Genders.Female)
    }

    "return a JsSuccess when it can parse json object Indeterminate" in {
      val json = Json.parse(
        """
          |"indeterminate"
        """.stripMargin)

      json.validate[Gender] shouldBe JsSuccess(Genders.Indeterminate)
    }

    "return a JsSuccess when it can parse json object None" in {
      val json = Json.parse(
        """
          |"none"
        """.stripMargin)

      json.validate[Gender] shouldBe JsSuccess(Genders.None)
    }

    "return a JsError when it cannot parse json string" in {
      val json = Json.parse(
        """
          |"something"
        """.stripMargin)

      json.validate[Gender] shouldBe JsError(ValidationError("Enumeration expected of type: 'class uk.gov.hmrc.cb.mappings.Genders$', but it does not appear to contain the value: 'something'"))
    }

  }

}
