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

import org.jsoup.Jsoup
import play.api.http.Status
import play.api.test.FakeRequest
import uk.gov.hmrc.cb.controllers.UpdateChildBenefitController
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
/**
  * Created by chrisianson on 04/05/16.
  */
class UpdateChildBenefitControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {
  val endPoint: String = "/update-child-benefit"
  val techDiffEndpoint: String = "/technical-difficulties"

  def mockUpdateChildBenefitController = new UpdateChildBenefitController {
    override protected def authConnector: AuthConnector = mock[AuthConnector]
  }

  val fakeRequestGet = FakeRequest("GET", endPoint)

  s"GET $endPoint" should {

    "return 200" in {
      mockUpdateChildBenefitController
      val result = UpdateChildBenefitController.present(fakeRequestGet)
      status(result) shouldBe Status.OK
    }

    "be able to see the radio button elements" in {
      val result = UpdateChildBenefitController.present(fakeRequestGet)
      val doc = Jsoup.parse(contentAsString(result))
      doc.getElementById("updateChildBenefit-true").attr("type") shouldBe "radio"
      doc.getElementById("updateChildBenefit-false").attr("type") shouldBe "radio"
    }

    "return html content type" in {
      val result = UpdateChildBenefitController.present(fakeRequestGet)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "return valid string" in {
      val result = await(UpdateChildBenefitController.present(fakeRequestGet))
      bodyOf(result).toString.replaceAll("&#x27;", "\'") should include(Messages("cb.update.child.benefit"))
    }
  }

  val fakeRequestPostTrue = FakeRequest("POST", endPoint).withFormUrlEncodedBody(
    ("updateChildBenefit", "true"))
  val fakeRequestPostFalse = FakeRequest("POST", endPoint).withFormUrlEncodedBody(
    ("updateChildBenefit", "false"))
  val fakeRequestPostInvalid = FakeRequest("POST", endPoint).withFormUrlEncodedBody(
    ("updateChildBenefit", ""))

  s"POST $endPoint" should {
    "Submit the form with YES and redirect to the same endpoint" in {
      val result = UpdateChildBenefitController.submit.apply(fakeRequestPostTrue)
      status(result) shouldBe Status.SEE_OTHER
      result.header.headers("Location") should include(endPoint)
    }

    "submit the form with NO and redirect to the technical difficulties endpoint" in {
      val result = UpdateChildBenefitController.submit.apply(fakeRequestPostFalse)
      status(result) shouldBe Status.SEE_OTHER
      result.header.headers("Location") should include(techDiffEndpoint)
    }

    "submit the form with an invalid result and return a BAD REQUEST code" in {
      val result = UpdateChildBenefitController.submit.apply(fakeRequestPostInvalid)
      status(result) shouldBe Status.BAD_REQUEST
    }

    "submit the form with an invalid result and return with an error message" in {
      val result = await(UpdateChildBenefitController.submit.apply(fakeRequestPostInvalid))
      bodyOf(result).toString.replaceAll("&#x27;", "\'") should include(Messages("cb.error.update.child.benefit.required"))
    }

  }
}