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

package uk.gov.hmrc.cb.controllers

import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.cb.CBFakeApplication
import uk.gov.hmrc.cb.controllers.session.CBSessionProvider
import uk.gov.hmrc.cb.helpers.Assertions
import uk.gov.hmrc.cb.service.keystore.KeystoreService.ChildBenefitKeystoreService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by chrisianson on 04/05/16.
  */
class UpdateChildBenefitControllerSpec extends UnitSpec with CBFakeApplication with MockitoSugar with Assertions {

  "UpdateChildBenefitController" when {

    val endPoint: String = "/update-child-benefit"
    val resultEndpoint : String = "/children/1/name"
    val techDiffEndpoint: String = "/technical-difficulties"

    val mockUpdateChildBenefitController = new UpdateChildBenefitController {
      override val authConnector: AuthConnector = mock[AuthConnector]
      override val cacheClient = mock[ChildBenefitKeystoreService]
    }

    "initialising" should {

      "wire up the dependencies correctly" in {
        UpdateChildBenefitController.authConnector shouldBe a[AuthConnector]
      }

    }

    s"GET $endPoint" should {

      "return 200" in {
        val fakeRequestGet = FakeRequest(GET, endPoint).withSession(CBSessionProvider.generateSessionId())
        val result = mockUpdateChildBenefitController.get(fakeRequestGet)
        status(result) shouldBe Status.OK
      }

      "be able to see the radio button elements" in {
        val fakeRequestGet = FakeRequest(GET, endPoint).withSession(CBSessionProvider.generateSessionId())
        val result = mockUpdateChildBenefitController.get(fakeRequestGet)
        val doc = Jsoup.parse(contentAsString(result))
        doc.getElementById("updateChildBenefit-true").attr("type") shouldBe "radio"
        doc.getElementById("updateChildBenefit-false").attr("type") shouldBe "radio"
      }

      "return html content type" in {
        val fakeRequestGet = FakeRequest(GET, endPoint).withSession(CBSessionProvider.generateSessionId())
        val result = mockUpdateChildBenefitController.get(fakeRequestGet)
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }

      "return valid string" in {
        val fakeRequestGet = FakeRequest(GET, endPoint).withSession(CBSessionProvider.generateSessionId())
        val result = await(mockUpdateChildBenefitController.get(fakeRequestGet))
        bodyOf(result).toString.replaceAll("&#x27;", "\'") should include(Messages("cb.update.child.benefit"))
      }
    }

    s"POST $endPoint" should {

      "Submit the form with YES and redirect to the same endpoint" in {
        val fakeRequestPostTrue = FakeRequest("POST", endPoint).withFormUrlEncodedBody(
          ("updateChildBenefit", "true")).withSession(CBSessionProvider.generateSessionId())
        val result = mockUpdateChildBenefitController.post.apply(fakeRequestPostTrue)
        status(result) shouldBe Status.SEE_OTHER
        verifyLocation(result, resultEndpoint)
      }

      "post the form with NO and redirect to the technical difficulties endpoint" in {
        val fakeRequestPostFalse = FakeRequest("POST", endPoint).withFormUrlEncodedBody(
          ("updateChildBenefit", "false")).withSession(CBSessionProvider.generateSessionId())
        val result = mockUpdateChildBenefitController.post.apply(fakeRequestPostFalse)
        status(result) shouldBe Status.SEE_OTHER
        verifyLocation(result, techDiffEndpoint)
      }

      "post the form with an invalid result and return a BAD REQUEST code" in {
        val fakeRequestPostInvalid = FakeRequest("POST", endPoint).withFormUrlEncodedBody(
          ("updateChildBenefit", "")).withSession(CBSessionProvider.generateSessionId())
        val result = mockUpdateChildBenefitController.post.apply(fakeRequestPostInvalid)
        status(result) shouldBe Status.BAD_REQUEST
      }

      "post the form with an invalid result and return with an error message" in {
        val fakeRequestPostInvalid = FakeRequest("POST", endPoint).withFormUrlEncodedBody(
          ("updateChildBenefit", "")).withSession(CBSessionProvider.generateSessionId())
        val result = await(mockUpdateChildBenefitController.post.apply(fakeRequestPostInvalid))
        bodyOf(result).toString.replaceAll("&#x27;", "\'") should include(Messages("cb.error.update.child.benefit.required"))
      }

    }
  }

}
