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

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.cb.CBFakeApplication
import uk.gov.hmrc.cb.controllers.session.CBSessionProvider
import uk.gov.hmrc.cb.helpers.Assertions
import uk.gov.hmrc.cb.models.payload.submission.Payload
import uk.gov.hmrc.cb.models.payload.submission.claimant.Claimant
import uk.gov.hmrc.cb.service.keystore.CBKeystoreKeys
import uk.gov.hmrc.cb.service.keystore.KeystoreService.ChildBenefitKeystoreService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

/**
 * Created by adamconder on 05/05/2016.
 */
class SubmissionConfirmationSpec extends UnitSpec with CBFakeApplication with MockitoSugar with Assertions {

  val mockController = new SubmissionConfirmationController {
    override val authConnector = mock[AuthConnector]
    override val cacheClient = mock[ChildBenefitKeystoreService with CBKeystoreKeys]
  }

  running(fakeApplication) {
    "SubmissionController" when {

      def mockSubmissionConfirmationController = new SubmissionConfirmationController {
        override val authConnector : AuthConnector = mock[AuthConnector]
        override val cacheClient = mock[ChildBenefitKeystoreService]
      }

      implicit lazy val getRequest = FakeRequest("GET", "/child-benefit/confirmation").withSession(CBSessionProvider.generateSessionId())

      "initialising" should {

        "wire up the dependencies correctly" in {
          SubmissionConfirmationController.authConnector shouldBe a[AuthConnector]
        }
      }

      "GET /confirmation" should {

        "respond 200 when claimant in keystore" in {
          val payload = Some(Payload(children = Nil, claimant = Some(Claimant(firstName = "Chris", lastName = "Smith"))))
          when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(payload))
          val result = await(mockController.get()(getRequest))
          status(result) shouldBe OK
        }

        "include firstname when claimants in keystore" in {
          val payload = Some(Payload(children = Nil, claimant = Some(Claimant(firstName = "Chris", lastName = "Smith"))))
          when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(payload))
          val result = await(mockController.get()(getRequest))
          bodyOf(result) should include("Chris")
        }

        "redirect to the initial controller when claimant doesn't exist" in {
          val payload = Some(Payload(children = Nil, claimant = None))
          when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(payload))
          val result = await(mockController.get()(getRequest))
          verifyLocation(result, "/update-child-benefit")
        }

        "redirect to technical difficulties when retrieving claimant and keystore is down" in {
          when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.failed(new RuntimeException))
          val result = await(mockController.get()(getRequest))
          status(result) shouldBe SEE_OTHER
          verifyLocation(result, "/technical-difficulties")
        }

        "redirect to the initial controller when payload doesn't exist" in {
          val payload = None
          when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.successful(payload))
          val result = await(mockController.get()(getRequest))
          verifyLocation(result, "/update-child-benefit")
        }
      }
    }
  }
}
