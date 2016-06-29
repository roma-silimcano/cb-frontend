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

package uk.gov.hmrc.cb.controllers.child

import org.mockito.Matchers.{eq => mockEq, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.data.Form
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.cb.CBFakeApplication
import uk.gov.hmrc.cb.controllers.session.CBSessionProvider
import uk.gov.hmrc.cb.forms.ChildNameForm
import uk.gov.hmrc.cb.forms.ChildNameForm.ChildNamePageModel
import uk.gov.hmrc.cb.helpers.Assertions
import uk.gov.hmrc.cb.managers.ChildrenManager
import uk.gov.hmrc.cb.models.payload.submission.Payload
import uk.gov.hmrc.cb.models.payload.submission.child.Child
import uk.gov.hmrc.cb.service.keystore.CBKeystoreKeys
import uk.gov.hmrc.cb.service.keystore.KeystoreService.ChildBenefitKeystoreService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

/**
 * Created by adamconder on 31/05/2016.
 */
class ChildNameControllerSpec extends UnitSpec with CBFakeApplication with MockitoSugar with Assertions {


  val childIndex = 1
  val childIndex2 = 2

  val mockController = new ChildNameController {
    override val cacheClient  = mock[ChildBenefitKeystoreService with CBKeystoreKeys]
    override val childrenService = ChildrenManager.childrenService
    override val authConnector = mock[AuthConnector]
  }

  "ChildNameController" when {

    implicit lazy val getRequest = FakeRequest("GET", "/child-benefit/children/1/name").withSession(CBSessionProvider.generateSessionId())
    def postRequest(form: Form[ChildNamePageModel], index : Int) = FakeRequest("POST", s"/child-benefit/children/$index/name")
      .withFormUrlEncodedBody(form.data.toSeq: _*)
      .withSession(CBSessionProvider.generateSessionId())

    implicit lazy val hc = HeaderCarrier()

    "initialising" should {

      "wire up the dependencies correctly" in {
        ChildNameController.authConnector shouldBe a[AuthConnector]
        ChildNameController.cacheClient shouldBe a[ChildBenefitKeystoreService]
      }

    }

    "calling /child-benefit/children/1/name" should {

      "respond to GET /child-benefit/children/1/name" in {
        val result = route(getRequest)
        result should not be NOT_FOUND
      }

      "respond to POST /child-benefit/children/1/name" in {
        val result = route(FakeRequest(POST, "/child-benefit/children/1/name"))
        result should not be NOT_FOUND
      }

    }

    "get" should {

      "redirect to technical difficulties when keystore is down" in {
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe SEE_OTHER
      }

      "respond 200 when no children in keystore" in {
        val payload = Some(Payload())
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(payload))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe OK
      }

      "respond 200 when child in keystore" in {
        val payload = Some(Payload(children = List(Child(id = 1, firstname = Some("Adam"), surname = Some("Conder")))))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(payload))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe OK
        bodyOf(result) should include("Adam")
      }

      "respond 200 when child in keystore with no name" in {
        val payload = Some(Payload(children = List(Child(id = 1))))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(payload))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe OK
      }

      "respond 200 when multiple children in keystore" in {
        val payload = Some(Payload(List(Child(id = 1, firstname = Some("Adam"), surname = Some("Conder")), Child(id = 2, firstname = Some("Chris"), surname = Some("I'anson")))))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(payload))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe OK
        bodyOf(result) should include("Adam")
        bodyOf(result) should not include "Chris"
      }

    }

    "post" should {

      "redirect to technical difficulties when keystore is down when saving" in {
        val payload = Payload(children = List(Child(id = 1)))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(Some(payload)))
        when(mockController.cacheClient.savePayload(mockEq(payload))(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val form = ChildNameForm.form.fill(ChildNamePageModel("Adam", "Conder"))
        val request = postRequest(form, childIndex)
        val result = await(mockController.post(childIndex)(request))
        status(result) shouldBe SEE_OTHER
        verifyLocation(result, "/technical-difficulties")
      }

      /*"redirect to technical difficulties when keystore is down when fetching children" in {
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val form = ChildNameForm.form.fill(ChildNamePageModel("Adam", "Conder"))
        val request = postRequest(form, childIndex)
        val result = await(mockController.post(childIndex)(request))
        status(result) shouldBe SEE_OTHER
        verifyLocation(result, "/technical-difficulties")
      }*/

      "respond BAD_REQUEST when POST is unsuccessful" in {
        val payload = Some(Payload(children = List(Child(id = 1))))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(payload))
        val form = ChildNameForm.form.fill(ChildNamePageModel("", "@£%!@£@£"))
        val request = postRequest(form, childIndex)
        val result = await(mockController.post(childIndex)(request))
        status(result) shouldBe BAD_REQUEST
      }

      "redirect to confirmation - No children" in {
        val payload = Payload(children = List(Child(id = 1, firstname = Some("Adam"), surname = Some("Conder"))))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(Some(payload)))
        when(mockController.cacheClient.savePayload(mockEq(payload))(any(), any())).thenReturn(Future.successful(Some(payload)))
        val form = ChildNameForm.form.fill(ChildNamePageModel("Adam", "Conder"))
        val request = postRequest(form, childIndex)
        val result = await(mockController.post(childIndex)(request))
        status(result) shouldBe SEE_OTHER
        verifyLocation(result, "/children/1/date-of-birth")
      }

      "redirect to confirmation when changing a child" in {
        val load = Payload(children = List(Child(id = 1, firstname = Some("Adam"), surname = Some("Conder"))))
        val save = Payload(children = List(Child(id = 1, firstname = Some("Adam"), surname = Some("Fenwick"))))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(Some(load)))
        when(mockController.cacheClient.savePayload(mockEq(save))(any(), any())).thenReturn(Future.successful(Some(save)))
        val form = ChildNameForm.form.fill(ChildNamePageModel("Adam", "Fenwick"))
        val request = postRequest(form, childIndex)
        val result = await(mockController.post(childIndex)(request))
        status(result) shouldBe SEE_OTHER
        verifyLocation(result, "/children/1/date-of-birth")
      }

      "redirect to confirmation when there is no change" in {
        val form = ChildNameForm.form.fill(ChildNamePageModel("Adam", "Conder"))
        val request = postRequest(form, childIndex)

        val load = Payload(children = List(Child(id = 1, firstname = Some("Adam"), surname = Some("Conder"))))
        val save = Payload(children = List(Child(id = 1, firstname = Some("Adam"), surname = Some("Conder"))))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(Some(load)))
        when(mockController.cacheClient.savePayload(mockEq(save))(any(), any())).thenReturn(Future.successful(Some(save)))
        val result = await(mockController.post(childIndex)(request))
        status(result) shouldBe SEE_OTHER
        verifyLocation(result, "/children/1/date-of-birth")
      }

      "redirect to confirmation when adding a new child to existing children" in {
        val form = ChildNameForm.form.fill(ChildNamePageModel("David", "Conder"))
        val request = postRequest(form, childIndex2)

        val load = Payload(children = List(Child(id = 1, firstname = Some("Adam"), surname = Some("Conder"))))
        val save = Payload(children = List(Child(id = 1, firstname = Some("Adam"), surname = Some("Conder")), Child(id = 2, firstname = Some("David"), surname = Some("Conder"))))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(Some(load)))
        when(mockController.cacheClient.savePayload(mockEq(save))(any(), any())).thenReturn(Future.successful(Some(save)))
        val result = await(mockController.post(childIndex2).apply(request))
        status(result) shouldBe SEE_OTHER
        verifyLocation(result, "/children/2/date-of-birth")
      }

      "redirect to technical difficulties when adding a new child to existing children when exception saving to keystore" in {
        val form = ChildNameForm.form.fill(ChildNamePageModel("David", "Conder"))
        val request = postRequest(form, childIndex2)

        val load = Payload(children = List(Child(id = 1, firstname = Some("Adam"), surname = Some("Conder"))))
        val save = Payload(children = List(Child(id = 1, firstname = Some("Adam"), surname = Some("Conder")), Child(id = 2, firstname = Some("David"), surname = Some("Conder"))))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(Some(load)))
        when(mockController.cacheClient.savePayload(mockEq(save))(any(), any())).thenReturn(Future.failed(new RuntimeException()))
        val result = await(mockController.post(childIndex2).apply(request))
        status(result) shouldBe SEE_OTHER
        verifyLocation(result, "/technical-difficulties")
      }
    }
  }

}
