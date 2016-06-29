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

package uk.gov.hmrc.cb.controllers.claimant

import org.mockito.Matchers.{eq => mockEq, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.cb.CBFakeApplication
import uk.gov.hmrc.cb.controllers.session.CBSessionProvider
import uk.gov.hmrc.cb.forms.ClaimantNameForm.ClaimantNamePageModel
import uk.gov.hmrc.cb.managers.ClaimantManager.ClaimantService
import uk.gov.hmrc.cb.service.keystore.CBKeystoreKeys
import uk.gov.hmrc.cb.service.keystore.KeystoreService.ChildBenefitKeystoreService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.test.UnitSpec
import play.api.data.Form
import uk.gov.hmrc.cb.forms.ClaimantNameForm
import uk.gov.hmrc.cb.helpers.Assertions
import uk.gov.hmrc.cb.managers.ClaimantManager
import uk.gov.hmrc.cb.models.payload.submission.Payload
import uk.gov.hmrc.cb.models.payload.submission.claimant.Claimant

import scala.concurrent.Future

/**
  * Created by chrisianson on 24/06/16.
  */
class ClaimantNameControllerSpec extends UnitSpec with CBFakeApplication with MockitoSugar with Assertions {

  "ClaimantNameController" when {

    val mockController = new ClaimantNameController {
      override val authConnector = mock[AuthConnector]
      override val cacheClient = mock[ChildBenefitKeystoreService with CBKeystoreKeys]
      override val claimantService = ClaimantManager.claimantService
      override val form = ClaimantNameForm.form
    }

    implicit lazy val getRequest = FakeRequest("GET", "/child-benefit/claimant/name").withSession(CBSessionProvider.generateSessionId())

    def postRequest(form: Form[ClaimantNamePageModel]) = FakeRequest("POST", "/child-benefit/claimant/name")
      .withFormUrlEncodedBody(form.data.toSeq: _*)
      .withSession(CBSessionProvider.generateSessionId())

    "initialising" should {
      "wire up dependencies correctly" in {
        ClaimantNameController.authConnector shouldBe a[AuthConnector]
        ClaimantNameController.cacheClient shouldBe a[ChildBenefitKeystoreService]
        ClaimantNameController.claimantService shouldBe a[ClaimantService]
        ClaimantNameController.form shouldBe a[Form[ClaimantNamePageModel]]
      }
    }

    "calling /claimant/name" should {

      "respond to GET request /child-benefit/claimant/name" in {
        val result = route(getRequest)
        result should not be NOT_FOUND
      }

      "respond to POST request /child-benefit/claimant/name" in {
        val result = route(getRequest)
        result should not be NOT_FOUND
      }
    }

    /* GET */
    "get" should {

      "redirect to technical difficulties when keystore is down" in {
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.get()(getRequest))
        status(result) shouldBe SEE_OTHER
      }

      "respond 200 when no claimant in keystore" in {
        val payload = Some(Payload(children = Nil))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(payload))
        val result = await(mockController.get()(getRequest))
        status(result) shouldBe OK
      }

      "respond 200 when no payload in keystore" in {
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(None))
        val result = await(mockController.get()(getRequest))
        status(result) shouldBe OK
      }

      "respond 200 when claimant in keystore" in {
        val payload = Some(Payload(children = Nil, claimant = Some(Claimant(firstName = "Chris", lastName = "Smith", None, None))))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(payload))
        val result = await(mockController.get()(getRequest))
        status(result) shouldBe OK
        bodyOf(result) should include("Chris")
      }
    }

    /* POST */
    "post" should {

      "redirect to confirmation when adding a claimant name when there is no payload" in {
        val save = Payload(children = Nil, claimant = Some(Claimant(firstName = "Chris", lastName = "Smith", None, None)))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(None))
        when(mockController.cacheClient.savePayload(mockEq(save))(any(), any())).thenReturn(Future.successful(Some(save)))
        val form = ClaimantNameForm.form.fill(ClaimantNamePageModel("Chris", "Smith"))
        val request = postRequest(form)
        val result = await(mockController.post()(request))
        status(result) shouldBe SEE_OTHER
        verifyLocation(result, "/confirmation")
      }

      "redirect to confirmation when adding a claimant name" in {
        val load = Some(Payload(children = Nil, claimant = Some(Claimant(firstName = "Chris", lastName = "Smith", None, None))))
        val save = Payload(children = Nil, claimant = Some(Claimant(firstName = "Chris", lastName = "Smith", None, None)))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(load))
        when(mockController.cacheClient.savePayload(mockEq(save))(any(), any())).thenReturn(Future.successful(Some(save)))
        val form = ClaimantNameForm.form.fill(ClaimantNamePageModel("Chris", "Smith"))
        val request = postRequest(form)
        val result = await(mockController.post()(request))
        status(result) shouldBe SEE_OTHER
        verifyLocation(result, "/confirmation")
      }

      "redirect to confirmation when changing a claimant name" in {
        val load = Some(Payload(children = Nil, claimant = Some(Claimant(firstName = "Chris", lastName = "Smith", None, None))))
        val save = Payload(children = Nil, claimant = Some(Claimant(firstName = "Adam", lastName = "Smith", None, None)))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(load))
        when(mockController.cacheClient.savePayload(mockEq(save))(any(), any())).thenReturn(Future.successful(Some(save)))
        val form = ClaimantNameForm.form.fill(ClaimantNamePageModel("Adam", "Smith"))
        val request = postRequest(form)
        val result = await(mockController.post()(request))
        status(result) shouldBe SEE_OTHER
        verifyLocation(result, "/confirmation")
      }

      "redirect to confirmation when there is no change" in {
        val load = Some(Payload(children = Nil, claimant = Some(Claimant(firstName = "Chris", lastName = "Smith", None, None))))
        val save = Payload(children = Nil, claimant = Some(Claimant(firstName = "Chris", lastName = "Smith", None, None)))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(load))
        when(mockController.cacheClient.savePayload(mockEq(save))(any(), any())).thenReturn(Future.successful(Some(save)))
        val form = ClaimantNameForm.form.fill(ClaimantNamePageModel("Chris", "Smith"))
        val request = postRequest(form)
        val result = await(mockController.post()(request))
        status(result) shouldBe SEE_OTHER
        verifyLocation(result, "/confirmation")
      }

      "redirect to technical difficulties when retrieving claimant and keystore is down" in {
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val form = ClaimantNameForm.form.fill(ClaimantNamePageModel("Chris", "Smith"))
        val request = postRequest(form)
        val result = await(mockController.post()(request))
        status(result) shouldBe SEE_OTHER
        verifyLocation(result, "/technical-difficulties")
      }

      "redirect to technical difficulties when adding a claimant name and keystore is down" in {
        val load = Some(Payload(children = Nil, claimant = Some(Claimant(firstName = "Chris", lastName = "Smith", None, None))))
        val save = Payload(children = Nil, claimant = Some(Claimant(firstName = "Chris", lastName = "Smith", None, None)))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(load))
        when(mockController.cacheClient.savePayload(mockEq(save))(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val form = ClaimantNameForm.form.fill(ClaimantNamePageModel("Chris", "Smith"))
        val request = postRequest(form)
        val result = await(mockController.post().apply(request))
        status(result) shouldBe SEE_OTHER
        verifyLocation(result, "/technical-difficulties")
      }

      "respond with BAD_REQUEST when post is unsuccessful" in {
        val claimant = Some(Payload(children = Nil, claimant = Some(Claimant(firstName = "Chris", lastName = "Smith", None, None))))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(claimant))
        val form = ClaimantNameForm.form.fill(ClaimantNamePageModel("Chris", "@£%!@£@£"))
        val request = postRequest(form)
        val result = await(mockController.post()(request))
        status(result) shouldBe BAD_REQUEST
      }
    }
  }
}
