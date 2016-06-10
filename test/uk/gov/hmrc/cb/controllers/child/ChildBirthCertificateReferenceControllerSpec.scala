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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.cb.CBFakeApplication
import uk.gov.hmrc.cb.controllers.session.CBSessionProvider
import uk.gov.hmrc.cb.forms.ChildBirthCertificateReferenceForm
import uk.gov.hmrc.cb.forms.ChildBirthCertificateReferenceForm.ChildBirthCertificateReferencePageModel
import uk.gov.hmrc.cb.managers.ChildrenManager
import uk.gov.hmrc.cb.models.Child
import uk.gov.hmrc.cb.service.keystore.CBKeystoreKeys
import uk.gov.hmrc.cb.service.keystore.KeystoreService.ChildBenefitKeystoreService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

/**
  * Created by chrisianson on 06/06/16.
  */
class ChildBirthCertificateReferenceControllerSpec extends UnitSpec with CBFakeApplication with MockitoSugar {

  val childIndex = 1
  val childIndex2 = 2

  val mockController = new ChildBirthCertificateReferenceController {
    override val cacheClient = mock[ChildBenefitKeystoreService with CBKeystoreKeys]
    override val childrenService =  ChildrenManager.childrenService
    override val authConnector = mock[AuthConnector]
  }

  "ChildBirthCertificateReference" when {

    implicit lazy val getRequest = FakeRequest("GET", "/child-benefit/children/1/birth-certificate-reference").withSession(CBSessionProvider.generateSessionId())
    def postRequest(form: Form[ChildBirthCertificateReferencePageModel], index : Int) = FakeRequest("POST", s"/child-benefit/children/$index/birth-certificate-reference")
      .withSession(CBSessionProvider.generateSessionId())
      .withFormUrlEncodedBody(form.data.toSeq: _*)
    implicit lazy val hc = HeaderCarrier()

    "initialising" should {

      "wire up the dependencies correctly" in {

        ChildBirthCertificateReferenceController.authConnector shouldBe a[AuthConnector]
        ChildBirthCertificateReferenceController.cacheClient shouldBe a[ChildBenefitKeystoreService]
      }
    }

    "calling /child-benefit/children/1/birth-certificate-reference" should {

      "respond to GET /child-benefit/children/1/birth-certificate-reference" in {
        val result = route(getRequest)
        result should not be NOT_FOUND
      }

      "respond to POST /child-benefit/children/1/birth-certificate-reference" in {
        val result = route(FakeRequest("POST", "/child-benefit/children/1/birth-certificate-reference"))
        result should not be NOT_FOUND
      }
    }

    "GET" should {

      "redirect to technical-difficulties page when keystore is down" in {
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe SEE_OTHER
      }

      "respond 200 when no children in keystore" in {
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(Nil))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe OK
      }

      "respond 200 when child in keystore with no birthCertificateReference" in {
        val children = List(Child(id = 1))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe OK
      }

      "respond 200 when child in keystore" in {
        val children = List(Child(id = 1, birthCertificateReference = Some("12345")))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe OK
        bodyOf(result) should include("12345")
      }

      "respond 200 when multiple children in keystore" in {
        val children = List(Child(id = 1, birthCertificateReference = Some("12345")), Child(id = 2, birthCertificateReference = Some("12345")))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe OK
      }

    "redirect to technical difficulties when out of bounds exception" in {
        val children = List(Child(id = 2))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe "/child-benefit/technical-difficulties"
      }
    }

    "POST" should {

      "redirect to confirmation when adding a new child to existing children" in {
        val form = ChildBirthCertificateReferenceForm.form.fill(ChildBirthCertificateReferencePageModel("123456789"))
        val request = postRequest(form, childIndex2)

        val load = List(Child(id = 1, birthCertificateReference = Some("123456789")))
        val save = List(Child(id = 1, birthCertificateReference = Some("123456789")), Child(id = 2, birthCertificateReference = Some("123456789")))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(load))
        when(mockController.cacheClient.saveChildren(mockEq(save))(any(), any())).thenReturn(Future.successful(save))
        val result = await(mockController.post(childIndex2).apply(request))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe "/child-benefit/confirmation"
      }

      "redirect to confirmation when updating a child" in {
        val load = List(Child(id = 1, birthCertificateReference = Some("111111111")))
        val save = List(Child(id = 1, birthCertificateReference = Some("123456789")))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(load))
        when(mockController.cacheClient.saveChildren(mockEq(save))(any(), any())).thenReturn(Future.successful(save))
        val form = ChildBirthCertificateReferenceForm.form.fill(ChildBirthCertificateReferencePageModel("123456789"))
        val request = postRequest(form, childIndex)
        val result = await(mockController.post(childIndex)(request))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe "/child-benefit/confirmation"
      }

      "redirect to confirmation - No children" in {
        val children = List(Child(id = 1, birthCertificateReference = Some("123456789")))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(Nil))
        when(mockController.cacheClient.saveChildren(mockEq(children))(any(), any())).thenReturn(Future.successful(children))
        val form = ChildBirthCertificateReferenceForm.form.fill(ChildBirthCertificateReferencePageModel("123456789"))
        val request = postRequest(form, childIndex)
        val result = await(mockController.post(childIndex)(request))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe "/child-benefit/confirmation"
      }

      "respond with BAD_REQUEST when post is unsuccessful" in {
        val children = List(Child(id = 1))
        when(mockController.cacheClient.loadChildren()(any(),any())).thenReturn(Future.successful(children))
        val form = ChildBirthCertificateReferenceForm.form.fill(ChildBirthCertificateReferencePageModel("abcdef"))
        val request = postRequest(form, childIndex)
        val result = await(mockController.post(childIndex)(request))
        status(result) shouldBe BAD_REQUEST
      }

      "redirect to technical difficulties when keystore is down when fetching children" in {
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val form = ChildBirthCertificateReferenceForm.form.fill(ChildBirthCertificateReferencePageModel("123456789"))
        val request = postRequest(form, childIndex)
        val result = await(mockController.post(childIndex)(request))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe "/child-benefit/technical-difficulties"
      }

      "redirect to technical difficulties when keystore is down when saving" in {
        val children = List(Child(id = 1, birthCertificateReference = Some("123456789")))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.saveChildren(mockEq(children))(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val form = ChildBirthCertificateReferenceForm.form.fill(ChildBirthCertificateReferencePageModel("123456789"))
        val request = postRequest(form, childIndex)
        val result = await(mockController.post(childIndex)(request))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe "/child-benefit/technical-difficulties"
      }

    }
  }
}
