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

package uk.gov.hmrc.cb

import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.cb.controllers.TechnicalDifficultiesController
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
/**
 * Created by andrew on 03/05/16.
 */
class TechnicalDifficultiesControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {
  val fakeRequest = FakeRequest("GET","/technical-difficulties")

  object testChildBenefitController extends TechnicalDifficultiesController {

  }

  "GET /technical-difficulties" should {
    "return an InternalServerError" in {
      val result = testChildBenefitController.get(fakeRequest)
      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return html content type" in {
      val result = testChildBenefitController.get(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "return valid string" in {
      val result = await(testChildBenefitController.get(fakeRequest))
      bodyOf(result).toString.replaceAll("&#x27;", "\'") should include(Messages("cb.technical.difficulties.heading"))
    }
  }
}
