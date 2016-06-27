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

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.cb.CBFakeApplication
import uk.gov.hmrc.cb.controllers.session.CBSessionProvider
import uk.gov.hmrc.cb.forms.ClaimantNameForm.ClaimantNamePageModel
import uk.gov.hmrc.cb.service.keystore.CBKeystoreKeys
import uk.gov.hmrc.cb.service.keystore.KeystoreService.ChildBenefitKeystoreService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.test.UnitSpec
import play.api.data.Form

import scala.concurrent.Future

/**
  * Created by chrisianson on 24/06/16.
  */
class ClaimantNameControllerSpec extends UnitSpec with CBFakeApplication with MockitoSugar {

  "ClaimantNameController" when {

    val mockController = new ClaimantNameController {
      override val authConnector = mock[AuthConnector]
      override val cacheClient = mock[ChildBenefitKeystoreService with CBKeystoreKeys]
    }

    implicit lazy val getRequest = FakeRequest("GET", "/child-benefit/claimant/name").withSession(CBSessionProvider.generateSessionId())

    def postRequest(form: Form[ClaimantNamePageModel], index : Int) = FakeRequest("POST", "/child-benefit/claimant/name")
      .withFormUrlEncodedBody(form.data.toSeq: _*)
      .withSession(CBSessionProvider.generateSessionId())

    "initialising" should {
      "wire up dependencies correctly" in {
        ClaimantNameController.authConnector shouldBe a[AuthConnector]
      }
    }

    /* GET */

    "calling /claimant/name" should {

      "respond to GET request /child-benefit/claimant/name" in {
        val result = route(getRequest)
        status(result.get) should not be NOT_FOUND
      }

      "respond to POST request /child-benefit/claimant/name" in {
        val result = route(getRequest)
        status(result.get) should not be NOT_FOUND
      }
    }

    "get" should {

      "redirect to technical difficulties when keystore is down" in {
        when(mockController.cacheClient.loadPayload()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.get()(getRequest))
        status(result) shouldBe SEE_OTHER
      }
    }
  }

  /* POST */

}
