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

import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.cb.CBFakeApplication
import uk.gov.hmrc.cb.service.keystore.KeystoreService.ChildBenefitKeystoreService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by adamconder on 05/05/2016.
 */
class SubmissionConfirmationSpec extends UnitSpec with MockitoSugar with CBFakeApplication {

  val mockSubmissionConfirmationController = new SubmissionConfirmationController {
    override val authConnector : AuthConnector = mock[AuthConnector]
    override val cacheClient = mock[ChildBenefitKeystoreService]
  }

  "SubmissionController" when {

      "initialising" should {

        "wire up the dependencies correctly" in {
          SubmissionConfirmationController.authConnector shouldBe a[AuthConnector]
          SubmissionConfirmationController.cacheClient shouldBe a[ChildBenefitKeystoreService]
        }

      }

      "GET /confirmation" should {

        "not respond with NOT_FOUND" ignore {
          val result = route(FakeRequest(GET, "/child-benefit/confirmation"))
          await(status(result.get)) shouldBe Status.OK
        }

        "return HTML" ignore {
          val result = route(FakeRequest(GET, "/child-benefit/confirmation"))
          await(contentType(result.get)) shouldBe Some("text/html")
          await(charset(result.get)) shouldBe Some("utf-8")
        }

        "return confirmation template" in {
          val result = mockSubmissionConfirmationController.get()(FakeRequest(GET, "/child-benefit/confirmation"))
          await(status(result)) shouldBe Status.OK
        }
      }
    }

}
