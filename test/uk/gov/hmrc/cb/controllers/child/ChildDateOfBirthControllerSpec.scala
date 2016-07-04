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

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.mockito.Matchers.{eq => mockEq, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.Logger
import play.api.data.Form
import play.api.test.FakeRequest
import uk.gov.hmrc.cb.CBFakeApplication
import uk.gov.hmrc.cb.controllers.session.CBSessionProvider
import uk.gov.hmrc.cb.forms.ChildDateOfBirthForm
import uk.gov.hmrc.cb.forms.ChildDateOfBirthForm.ChildDateOfBirthPageModel
import uk.gov.hmrc.cb.forms.constraints.Constraints
import uk.gov.hmrc.cb.helpers.{Assertions, DateHelpers}
import uk.gov.hmrc.cb.managers.ChildrenManager
import uk.gov.hmrc.cb.service.keystore.CBKeystoreKeys
import uk.gov.hmrc.cb.service.keystore.KeystoreService.ChildBenefitKeystoreService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import play.api.test.Helpers._
import uk.gov.hmrc.cb.models.payload.submission.Payload
import uk.gov.hmrc.cb.models.payload.submission.child.Child

import scala.concurrent.Future

/**
 * Created by adamconder on 15/06/2016.
 */

class ChildDateOfBirthControllerSpec extends UnitSpec with CBFakeApplication with MockitoSugar with Assertions {

  val childIndex = 1
  val childIndex2 = 2

  val mockController = new ChildDateOfBirthController {
    override val cacheClient  = mock[ChildBenefitKeystoreService with CBKeystoreKeys]
    override val childrenService = ChildrenManager.childrenService
    override val authConnector = mock[AuthConnector]
    override val form = ChildDateOfBirthForm.form
  }

  "ChildDateOfBirthController" when {

    implicit lazy val getRequest = FakeRequest("GET", "/child-benefit/children/1/date-of-birth").withSession(CBSessionProvider.generateSessionId())
    def postRequest(form: Form[ChildDateOfBirthPageModel], index : Int) = FakeRequest("POST", s"/child-benefit/children/$index/date-of-birth")
                    .withFormUrlEncodedBody(form.data.toSeq: _*)
                    .withSession(CBSessionProvider.generateSessionId())

    implicit lazy val hc = HeaderCarrier()

    "initialising" should {

      "wire up the dependencies correctly" in {
        ChildDateOfBirthController.authConnector shouldBe a[AuthConnector]
        ChildDateOfBirthController.cacheClient shouldBe a[ChildBenefitKeystoreService]
        ChildDateOfBirthController.form shouldBe a[Form[_]]
      }

    }

    "calling /child-benefit/children/1/date-of-birth" should {

      "respond to GET /child-benefit/children/1/date-of-birth" in {
        val result = route(getRequest)
        result should not be NOT_FOUND
      }

      "respond to POST /child-benefit/children/1/date-of-birth" in {
        val result = route(FakeRequest("POST", "/child-benefit/children/1/date-of-birth"))
        result should not be NOT_FOUND
      }

    }

    "GET" should {

      "redirect to technical-difficulties page when keystore is down" in {
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe SEE_OTHER
        verifyLocation(result, "/technical-difficulties")
      }

      "respond 200 when no children in keystore" in {
        val payload = Some(Payload())
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(payload))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe OK
      }

      "respond 200 when child in keystore with no date of birth" in {
        val payload = Some(Payload(children = List(Child(id = 1, birthCertificateReference = Some("12345")))))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(payload))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe OK
      }

      "respond 200 when child in keystore" in {
        val dob = DateHelpers.dateWithoutTimeZone(DateTime.now)
        val payload = Some(Payload(children = List(Child(id = 1, dob = Some(dob)))))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(payload))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe OK
        bodyOf(result) should include(dob.getDayOfMonth.toString)
        bodyOf(result) should include(dob.getMonthOfYear.toString)
        bodyOf(result) should include(dob.getYear.toString)
      }

      "result 200 when multiple children in keystore" in {
        val dob = DateHelpers.dateWithoutTimeZone(DateTime.now)
        val payload = Some(Payload(children = List(Child(id = 1, dob = Some(dob)), Child(id = 2, dob = Some(dob)))))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(payload))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe OK
      }

    }

    "POST" should {

      "redirect to confirmation when adding a new child to existing children" in {
        val date = DateTime.parse("2010-08-27", Constraints.dateFormatWithoutTimestamp)
        val form = ChildDateOfBirthForm.form.fill(ChildDateOfBirthPageModel(date))
        val request = postRequest(form, childIndex2)
        val load = Some(Payload(children = List(Child(id = 1, dob = Some(date)))))
        val save = Payload(children = List(Child(id = 1, dob = Some(date)), Child(id = 2, dob = Some(date))))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(load))
        when(mockController.cacheClient.savePayload(mockEq(save))(any(), any())).thenReturn(Future.successful(Some(save)))
        val result = await(mockController.post(childIndex2).apply(request))
        status(result) shouldBe SEE_OTHER
        verifyLocation(result, "/children/2/birth-certificate-reference")
      }

      "redirect to confirmation when updating a child" in {
        val date = DateHelpers.dateWithoutTimeZone(DateTime.now)
        val amendedDate = date.minusDays(2)
        val load = Some(Payload(children=List(Child(id = 1, dob = Some(date)))))
        val save = Payload(children=List(Child(id = 1, dob = Some(amendedDate))))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(load))
        when(mockController.cacheClient.savePayload(mockEq(save))(any(), any())).thenReturn(Future.successful(Some(save)))
        val form = ChildDateOfBirthForm.form.bind(Map(
                  "dateOfBirth.day" ->  s"${amendedDate.getDayOfMonth}",
                  "dateOfBirth.month" -> s"${amendedDate.getMonthOfYear}",
                  "dateOfBirth.year" -> s"${amendedDate.getYear}"
                ))
        val request = postRequest(form, childIndex)
        val result = await(mockController.post(childIndex)(request))
        status(result) shouldBe SEE_OTHER
        verifyLocation(result, "/children/1/birth-certificate-reference")
      }

      "redirect to confirmation after adding a child" in {
        val date = DateTime.parse("2010-08-27", Constraints.dateFormatWithoutTimestamp)
        val load = Some(Payload(children = List(Child(id = 1, dob = Some(date)))))
        val save = Payload(children = List(Child(id = 1, dob = Some(date))))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(load))
        when(mockController.cacheClient.savePayload(mockEq(save))(any(), any())).thenReturn(Future.successful(load))
        val form = ChildDateOfBirthForm.form.fill(ChildDateOfBirthPageModel(date))
        val request = postRequest(form, childIndex)
        val result = await(mockController.post(childIndex)(request))
        status(result) shouldBe SEE_OTHER
        verifyLocation(result, "/children/1/birth-certificate-reference")
      }

      "redirect to confirmation after creating a new Payload and adding a new child" in {
        val date = DateTime.parse("2010-08-27", Constraints.dateFormatWithoutTimestamp)
        val load = None
        val save = Payload(children = List(Child(id = 1, dob = Some(date))))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(load))
        when(mockController.cacheClient.savePayload(mockEq(save))(any(), any())).thenReturn(Future.successful(load))
        val form = ChildDateOfBirthForm.form.fill(ChildDateOfBirthPageModel(date))
        val request = postRequest(form, childIndex)
        val result = await(mockController.post(childIndex)(request))
        status(result) shouldBe SEE_OTHER
        verifyLocation(result, "/children/1/birth-certificate-reference")
      }

      "respond with BAD_REQUEST when post is unsuccessful" in {
        val load = Some(Payload(children = List(Child(id = 1))))
        when(mockController.cacheClient.loadPayload()(any(),any())).thenReturn(Future.successful(load))
        val form = ChildDateOfBirthForm.form.bind(Map(
          "dateOfBirth.day" ->  s"32",
          "dateOfBirth.month" -> s"13",
          "dateOfBirth.year" -> s"20102"
        ))
        val request = postRequest(form, childIndex)
        val result = await(mockController.post(childIndex)(request))
        status(result) shouldBe BAD_REQUEST
      }

      "redirect to technical difficulties when keystore is down when fetching children" in {
        val dob = DateHelpers.dateWithoutTimeZone(DateTime.now)
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val form = ChildDateOfBirthForm.form.fill(ChildDateOfBirthPageModel(dob))
        val request = postRequest(form, childIndex)
        val result = await(mockController.post(childIndex)(request))
        status(result) shouldBe SEE_OTHER
        verifyLocation(result, "/technical-difficulties")
      }

      "redirect to technical difficulties when keystore is down when saving" in {
        val date = DateTime.parse("2010-08-27", Constraints.dateFormatWithoutTimestamp)
        val load = Some(Payload(children = List(Child(id = 1, dob = Some(date)))))
        val save = Payload(children = List(Child(id = 1, dob = Some(date))))
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(load))
        when(mockController.cacheClient.savePayload(mockEq(save))(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val form = ChildDateOfBirthForm.form.fill(ChildDateOfBirthPageModel(date))
        val request = postRequest(form, childIndex)
        val result = await(mockController.post(childIndex)(request))
        status(result) shouldBe SEE_OTHER
        verifyLocation(result, "/technical-difficulties")
      }
    }
  }
}
