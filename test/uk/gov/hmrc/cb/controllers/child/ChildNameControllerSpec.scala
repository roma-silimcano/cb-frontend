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

import org.scalatest.mock.MockitoSugar
import play.api.data.Form
import play.api.mvc.{Result, Action}
import play.api.test.FakeRequest
import uk.gov.hmrc.cb.CBFakeApplication
import uk.gov.hmrc.cb.forms.ChildNameForm
import uk.gov.hmrc.cb.forms.ChildNameForm.ChildNamePageModel
import uk.gov.hmrc.cb.managers.ChildrenManager
import uk.gov.hmrc.cb.managers.ChildrenManager.ChildrenService
import uk.gov.hmrc.cb.models.Child
import uk.gov.hmrc.cb.service.keystore.CBKeystoreKeys
import uk.gov.hmrc.cb.service.keystore.KeystoreService.ChildBenefitKeystoreService
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import play.api.test.Helpers._
import org.mockito.Matchers.{eq => mockEq, _}
import org.mockito.Mockito._

import scala.concurrent.Future

/**
 * Created by adamconder on 31/05/2016.
 */
class ChildNameControllerSpec extends UnitSpec with CBFakeApplication with MockitoSugar {

  implicit val getRequest = FakeRequest("GET", "/child-benefit/children/1/name")
  def postRequest(form: Form[ChildNamePageModel], index : Int) = FakeRequest("POST", s"/child-benefit/children/$index/name").withFormUrlEncodedBody(form.data.toSeq: _*)
  implicit val hc = HeaderCarrier()

  val childIndex = 1
  val childIndex2 = 2

  val mockController = new ChildNameController {
    override val authConnector = mock[AuthConnector]
    override val cacheClient = mock[ChildBenefitKeystoreService with CBKeystoreKeys]
    override val childrenService = ChildrenManager.childrenService
  }

  def redirectLocation(response : Result) = response.header.headers.get("Location").get

  "ChildNameController" when {

    "initialising" should {

      "wire up the dependencies correctly" in {
        ChildNameController.authConnector shouldBe a[AuthConnector]
        ChildNameController.cacheClient shouldBe a[ChildBenefitKeystoreService]
      }

    }

    "calling /child-benefit/children/1/name" should {

      "respond to GET /child-benefit/children/1/name" in {
        val result = route(FakeRequest(GET, "/child-benefit/children/1/name"))
        status(result.get) should not be NOT_FOUND
      }

      "respond to POST /child-benefit/children/1/name" in {
        val result = route(FakeRequest(POST, "/child-benefit/children/1/name"))
        status(result.get) should not be NOT_FOUND
      }

    }

    "get" should {

      "redirect to technical difficulties when keystore is down" in {
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe SEE_OTHER
      }

      "respond 200 when no children in keystore" in {
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(None))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe OK
      }

      "respond 200 when child in keystore" in {
        val children = Some(List(Child(id = 1)))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe OK
      }

      "respond 200 when multiple children in keystore" in {
        val children = Some(List(Child(id = 1), Child(id = 2)))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe OK
      }

      "redirect to technical difficulties when out of bounds exception" in {
        val children = Some(List(Child(id = 2)))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe "/child-benefit/technical-difficulties"
      }

      "redirect to technical difficulties when out of bounds exception - empty list" in {
        val children = Some(List())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe "/child-benefit/technical-difficulties"
      }

    }

    "post" should {

      "redirect to technical difficulties when keystore is down when saving" in {
        val children = Some(List(Child(id = 1)))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.saveChildren(mockEq(children.get))(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val form = ChildNameForm.form.fill(ChildNamePageModel("Adam", "Conder"))
        val request = postRequest(form, childIndex)
        val result = await(mockController.post(childIndex)(request))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe "/child-benefit/technical-difficulties"
      }

      "redirect to technical difficulties when keystore is down when fetching children" in {
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val form = ChildNameForm.form.fill(ChildNamePageModel("Adam", "Conder"))
        val request = postRequest(form, childIndex)
        val result = await(mockController.post(childIndex)(request))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe "/child-benefit/technical-difficulties"
      }

      "respond BAD_REQUEST when POST is unsuccessful" in {
        val children = Some(List(Child(id = 1)))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val form = ChildNameForm.form.fill(ChildNamePageModel("", "@£%!@£@£"))
        val request = postRequest(form, childIndex)
        val result = await(mockController.post(childIndex)(request))
        status(result) shouldBe BAD_REQUEST
      }

      "redirect to confirmation - No children" in {
        val children = Some(List(Child(id = 1, firstname = Some("Adam"), surname = Some("Conder"))))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(None))
        when(mockController.cacheClient.saveChildren(mockEq(children.get))(any(), any())).thenReturn(Future.successful(children))
        val form = ChildNameForm.form.fill(ChildNamePageModel("Adam", "Conder"))
        val request = postRequest(form, childIndex)
        val result = await(mockController.post(childIndex)(request))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe "/child-benefit/confirmation"
      }

      "redirect to confirmation when changing a child" in {
        val load = Some(List(Child(id = 1, firstname = Some("Adam"), surname = Some("Conder"))))
        val save = Some(List(Child(id = 1, firstname = Some("Adam"), surname = Some("Fenwick"))))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(load))
        when(mockController.cacheClient.saveChildren(mockEq(save.get))(any(), any())).thenReturn(Future.successful(save))
        val form = ChildNameForm.form.fill(ChildNamePageModel("Adam", "Fenwick"))
        val request = postRequest(form, childIndex)
        val result = await(mockController.post(childIndex)(request))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe "/child-benefit/confirmation"
      }

      "redirect to confirmation when there is no change" in {
        val form = ChildNameForm.form.fill(ChildNamePageModel("Adam", "Conder"))
        val request = postRequest(form, childIndex)

        val load = Some(List(Child(id = 1, firstname = Some("Adam"), surname = Some("Conder"))))
        val save = Some(List(Child(id = 1, firstname = Some("Adam"), surname = Some("Conder"))))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(load))
        when(mockController.cacheClient.saveChildren(mockEq(save.get))(any(), any())).thenReturn(Future.successful(save))
        val result = await(mockController.post(childIndex)(request))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe "/child-benefit/confirmation"
      }

      "redirect to confirmation when adding a new child to existing children" in {
        val form = ChildNameForm.form.fill(ChildNamePageModel("David", "Conder"))
        val request = postRequest(form, childIndex2)

        val load = Some(List(Child(id = 1, firstname = Some("Adam"), surname = Some("Conder"))))
        val save = Some(List(Child(id = 1, firstname = Some("Adam"), surname = Some("Conder")), Child(id = 2, firstname = Some("David"), surname = Some("Conder"))))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(load))
        when(mockController.cacheClient.saveChildren(mockEq(save.get))(any(), any())).thenReturn(Future.successful(save))
        val result = await(mockController.post(childIndex2).apply(request))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe "/child-benefit/confirmation"
      }

      "redirect to technical difficulties when adding a new child to existing children when exception saving to keystore" in {
        val form = ChildNameForm.form.fill(ChildNamePageModel("David", "Conder"))
        val request = postRequest(form, childIndex2)

        val load = Some(List(Child(id = 1, firstname = Some("Adam"), surname = Some("Conder"))))
        val save = Some(List(Child(id = 1, firstname = Some("Adam"), surname = Some("Conder")), Child(id = 2, firstname = Some("David"), surname = Some("Conder"))))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(load))
        when(mockController.cacheClient.saveChildren(mockEq(save.get))(any(), any())).thenReturn(Future.failed(new RuntimeException()))
        val result = await(mockController.post(childIndex2).apply(request))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe "/child-benefit/technical-difficulties"
      }

    }

  }

}
